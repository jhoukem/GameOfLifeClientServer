package view;

import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.GridModel;
import utils.Constants;

/**
 * This class allow a client to send command to a server.
 * 
 * @author Jean-Hugo
 */
public class CommandPanel extends JPanel implements ActionListener, ChangeListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// This helps to keep the code readable.
	private static final int GRID_SIZE_SLIDER_INDEX = 0;
	private static final int GRID_UPDATE_RATE_SLIDER_INDEX = 0;
	private static final int LOW_INTERVAL_SLIDER_INDEX = 0;
	private static final int HIGHT_INTERVAL_SLIDER_INDEX = 1;
	private static final int CELL_APPARITION_PERCENTAGE_SLIDER_INDEX = 0;

	// The client socket which is connected to the server.
	private SocketChannel clientSocket;

	// Used to reset the game.
	private JButton reset;

	// Used to control the grid size.
	private LabeledSlicerPanel gridSize;
	// Used to control the game update speed.
	private LabeledSlicerPanel gridUpdateRate;
	// Used to control the neighbors count interval for the cell to survive.
	private LabeledSlicerPanel cellNeighborsToSurvive;
	// Used to control the apparition percentage for the cells on reset.
	private LabeledSlicerPanel apparitionPercentageOnReset;

	/** 
	 * Useful to know if the update is coming from the client or from the server.
	 * Because any update a client make is echoed back to him.
	 */
	private boolean onServerUpdate = false;

	// Whether a slicer is updating another one.
	private boolean concurrentModification = false;

	public CommandPanel() {
		initGui();
	}

	/**
	 * Set up the necessary graphic component for the client to send command to the server.
	 */
	private void initGui() {

		createComponents();
		setUpLayout();
	}

	/**
	 * Create all the graphic component needed.
	 */
	private void createComponents() {

		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(10, new JLabel("10"));
		labelTable.put(50, new JLabel("50"));
		labelTable.put(100, new JLabel("100"));


		gridSize = new LabeledSlicerPanel("Grid size", this);
		gridSize.addSlicer(GridModel.DEFAULT_GRID_SIZE, GridModel.MAXIMUM_GRID_SIZE, labelTable, 10, 0);


		labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(Constants.MINIMUM_UPDATE_RATE, new JLabel("Min(100 ms)"));
		labelTable.put(Constants.MAXIMUM_UPDATE_RATE, new JLabel("Max(5 sec)"));

		gridUpdateRate = new LabeledSlicerPanel("Grid update rate", this);
		gridUpdateRate.addSlicer(Constants.MINIMUM_UPDATE_RATE, Constants.MAXIMUM_UPDATE_RATE, labelTable, 0, 0);


		cellNeighborsToSurvive = new LabeledSlicerPanel("Cell requiered neighbors to survive", this);
		cellNeighborsToSurvive.addSlicer("Low interval", Constants.MINIMUM_CELL_NEIGHBORS,
				Constants.MAXIMUM_CELL_NEIGHBORS, null, 1, 0);
		cellNeighborsToSurvive.addSlicer("Hight interval", Constants.MINIMUM_CELL_NEIGHBORS,
				Constants.MAXIMUM_CELL_NEIGHBORS, null, 1, 0);

		apparitionPercentageOnReset = new LabeledSlicerPanel("Chance for a cell to be alive on grid reset (in %)", null);
		apparitionPercentageOnReset.addSlicer(0, 100, null, 20, 0);

		reset = new JButton("Reset");
		reset.addActionListener(this);
	}

	/**
	 * Arrange the graphic component in the correct order.
	 */
	private void setUpLayout() {

		Panel controlPanel = new Panel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

		controlPanel.add(gridSize);
		controlPanel.add(gridUpdateRate);
		controlPanel.add(cellNeighborsToSurvive);
		controlPanel.add(apparitionPercentageOnReset);


		this.setLayout(new BorderLayout());
		this.add(controlPanel, BorderLayout.CENTER);

		// To avoid my button being stretched.
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(reset);
		this.add(buttonPanel, BorderLayout.SOUTH);
	}


	/**
	 * Listener for the JSliders
	 */
	@Override
	public void stateChanged(ChangeEvent ce) {

		if(onServerUpdate){
			return;
		}

		if(ce.getSource().equals(gridSize.getSlider(GRID_SIZE_SLIDER_INDEX)) && !(gridSize.isSliderAdjusting(GRID_SIZE_SLIDER_INDEX))){
			String cmd = Constants.CHANGE_GRID_SIZE_COMMAND + ":" + gridSize.getSliderValue(GRID_SIZE_SLIDER_INDEX);
			send(cmd);
		} else if(ce.getSource().equals(gridUpdateRate.getSlider(GRID_UPDATE_RATE_SLIDER_INDEX)) &&
				!gridUpdateRate.isSliderAdjusting(GRID_UPDATE_RATE_SLIDER_INDEX)){
			String cmd = Constants.CHANGE_GRID_UPDATE_RATE_COMMAND + ":" + gridUpdateRate.getSliderValue(GRID_UPDATE_RATE_SLIDER_INDEX);
			send(cmd);
		} else if(!concurrentModification && ce.getSource().equals(cellNeighborsToSurvive.getSlider(LOW_INTERVAL_SLIDER_INDEX)) &&
				!cellNeighborsToSurvive.isSliderAdjusting(LOW_INTERVAL_SLIDER_INDEX)){

			// The minimum cannot be > to the maximum.
			if(cellNeighborsToSurvive.getSliderValue(LOW_INTERVAL_SLIDER_INDEX) > cellNeighborsToSurvive.getSliderValue(HIGHT_INTERVAL_SLIDER_INDEX)){
				concurrentModification = true;
				cellNeighborsToSurvive.setSliderValue(HIGHT_INTERVAL_SLIDER_INDEX, cellNeighborsToSurvive.getSliderValue(LOW_INTERVAL_SLIDER_INDEX));
			}

			String cmd = Constants.CHANGE_GRID_CELL_REQUIREMENT_COMMAND + ":"+cellNeighborsToSurvive.getSliderValue(LOW_INTERVAL_SLIDER_INDEX)+
					":"+cellNeighborsToSurvive.getSliderValue(HIGHT_INTERVAL_SLIDER_INDEX);
			send(cmd);

			concurrentModification = false;
		} else if(!concurrentModification && ce.getSource().equals(cellNeighborsToSurvive.getSlider(HIGHT_INTERVAL_SLIDER_INDEX)) &&
				!cellNeighborsToSurvive.isSliderAdjusting(HIGHT_INTERVAL_SLIDER_INDEX)){

			// The maximum cannot be < to the minimum.
			if(cellNeighborsToSurvive.getSliderValue(HIGHT_INTERVAL_SLIDER_INDEX) < cellNeighborsToSurvive.getSliderValue(LOW_INTERVAL_SLIDER_INDEX)){
				concurrentModification = true;
				cellNeighborsToSurvive.setSliderValue(LOW_INTERVAL_SLIDER_INDEX, cellNeighborsToSurvive.getSliderValue(HIGHT_INTERVAL_SLIDER_INDEX));
			}
			String cmd = Constants.CHANGE_GRID_CELL_REQUIREMENT_COMMAND + ":"+cellNeighborsToSurvive.getSliderValue(LOW_INTERVAL_SLIDER_INDEX)+
					":"+cellNeighborsToSurvive.getSliderValue(HIGHT_INTERVAL_SLIDER_INDEX);
			send(cmd);

			concurrentModification = false;
		}
	}

	/**
	 * Listener for the button.
	 */
	@Override
	public void actionPerformed(ActionEvent ap) {
		if(onServerUpdate){
			return;
		}

		if(ap.getSource().equals(reset)){
			String cmd = Constants.RESET_GRID_COMMAND + ":" +
					apparitionPercentageOnReset.getSliderValue(CELL_APPARITION_PERCENTAGE_SLIDER_INDEX);
			send(cmd);
		}

	}

	/**
	 * Send the given command to the server.
	 * 
	 * @param command The command to send.
	 */
	private void send(String command) {

		// Not connected yet.
		if(clientSocket == null){
			return;
		}

		try {
			ByteBuffer buffer = ByteBuffer.wrap(command.getBytes());
			clientSocket.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setSocket(SocketChannel socket){
		this.clientSocket = socket;
	}

	/**
	 * When a command is received from the server to update the grid size, it has to be reflected on the JSlider.
	 * 
	 * @param newGridSize the new size for the grid.
	 */
	public void setCurrentGridSize(int newGridSize) {
		onServerUpdate = true;
		// If the client is not currently using this slicer.
		if(!gridSize.isSliderAdjusting(GRID_SIZE_SLIDER_INDEX)){
			gridSize.setSliderValue(GRID_SIZE_SLIDER_INDEX, newGridSize);
		}
		onServerUpdate = false;
	}

	/**
	 * When a command is received from the server to update the grid update rate, it has to be reflected on the JSlider.
	 * 
	 * @param newUpdateRate the update rate for the grid.
	 */
	public void setCurrentUpdateRate(int newUpdateRate) {
		onServerUpdate = true;
		// If the client is not currently using this slicer.
		if(!gridUpdateRate.isSliderAdjusting(GRID_UPDATE_RATE_SLIDER_INDEX)){
			gridUpdateRate.setSliderValue(GRID_UPDATE_RATE_SLIDER_INDEX, newUpdateRate);
		}
		onServerUpdate = false;
	}

	/**
	 * When a command is received from the server to update the grid cells requirement, it has to be reflected on the JSliders.
	 * 
	 * @param min the minimal value interval for a cell to survive.
	 * @param max the maximal value interval for a cell to survive.
	 */
	public void setCellRequirement(int min, int max) {
		onServerUpdate = true;

		if(GridModel.cellRequirementCorrect(min, max)){
			// If the client is not currently using this slicer.
			if(!cellNeighborsToSurvive.isSliderAdjusting(LOW_INTERVAL_SLIDER_INDEX)){
				cellNeighborsToSurvive.setSliderValue(LOW_INTERVAL_SLIDER_INDEX, min);
			}
			// If the client is not currently using this slicer.
			if(!cellNeighborsToSurvive.isSliderAdjusting(HIGHT_INTERVAL_SLIDER_INDEX)){
				cellNeighborsToSurvive.setSliderValue(HIGHT_INTERVAL_SLIDER_INDEX, max);
			}
		} else {
			System.err.println("Wrong data received cell requirement is not correct. Min = "+min+" Max = "+max );
		}
		onServerUpdate = false;
	}

	/**
	 * When a command is received from the server to update the grid cell apparition percentage on reset,
	 * it has to be reflected on the JSlider.
	 * 
	 * @param appationPercentage the new percentage of cell apparition on reset.
	 */
	public void setApparitionPercentage(int appationPercentage) {
		// If the client is not currently using this slicer.
		if(!apparitionPercentageOnReset.isSliderAdjusting(CELL_APPARITION_PERCENTAGE_SLIDER_INDEX)){
			apparitionPercentageOnReset.setSliderValue(CELL_APPARITION_PERCENTAGE_SLIDER_INDEX, appationPercentage);
		}
	}
}
