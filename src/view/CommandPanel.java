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
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.GridModel;
import utils.Constants;

public class CommandPanel extends JPanel implements ActionListener, ChangeListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// The client socket which is conencted to the server.
	private SocketChannel clientSocket;

	// Used to control the grid size.
	private JSlider gridSizeSlider;
	// Used to control the game update speed.
	private JSlider gridUpdateRateSlider;
	// Used to reset the game.
	private JButton reset;


	private JSlider minimumNeighborsSlider;
	private JSlider maximumNeighborsSlider;

	public CommandPanel() {
		initGui();
	}

	/**
	 * Set up the necessary component for the client to send command to the server.
	 */
	private void initGui() {

		createComponents();

		setUpLayout();
	}

	private void setUpLayout() {

		Panel controlPanel = new Panel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		controlPanel.add(gridSizeSlider);
		controlPanel.add(gridUpdateRateSlider);


		Panel panel = new Panel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(minimumNeighborsSlider);
		panel.add(maximumNeighborsSlider);

		controlPanel.add(panel);
		this.setLayout(new BorderLayout());

		this.add(controlPanel, BorderLayout.CENTER);

		// To avoid my button being streched.
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(reset);
		this.add(buttonPanel, BorderLayout.SOUTH);
	}

	private void createComponents() {
		gridSizeSlider = new JSlider(GridModel.DEFAULT_GRID_SIZE, GridModel.MAXIMUM_GRID_SIZE);

		Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
		table.put(10, new JLabel("10"));
		table.put(50, new JLabel("50"));
		table.put(100, new JLabel("100"));

		gridSizeSlider.setLabelTable(table);
		gridSizeSlider.setMajorTickSpacing(10);
		gridSizeSlider.setPaintTicks(true);
		gridSizeSlider.setPaintLabels(true);
		gridSizeSlider.addChangeListener(this);

		gridUpdateRateSlider = new JSlider(Constants.MINIMUM_UPDATE_RATE, Constants.MAXIMUM_UPDATE_RATE);

		table = new Hashtable<Integer, JLabel>();
		table.put(100, new JLabel("Min(100 ms)"));
		table.put(5000, new JLabel("Max(5 sec)"));

		gridUpdateRateSlider.setLabelTable(table);
		gridUpdateRateSlider.setPaintLabels(true);
		gridUpdateRateSlider.addChangeListener(this);

		minimumNeighborsSlider = new JSlider(Constants.MINIMUM_CELL_NEIGHBORS, Constants.MAXIMUM_CELL_NEIGHBORS);
		minimumNeighborsSlider.setMajorTickSpacing(5);
		minimumNeighborsSlider.setMinorTickSpacing(1);
		minimumNeighborsSlider.setPaintLabels(true);
		minimumNeighborsSlider.setPaintTicks(true);
		minimumNeighborsSlider.addChangeListener(this);

		maximumNeighborsSlider = new JSlider(Constants.MINIMUM_CELL_NEIGHBORS, Constants.MAXIMUM_CELL_NEIGHBORS);
		maximumNeighborsSlider.setMajorTickSpacing(5);
		maximumNeighborsSlider.setMinorTickSpacing(1);
		maximumNeighborsSlider.setPaintLabels(true);
		maximumNeighborsSlider.setPaintTicks(true);
		maximumNeighborsSlider.addChangeListener(this);

		reset = new JButton("Reset");
		reset.addActionListener(this);
	}

	@Override
	public void stateChanged(ChangeEvent ce) {

		if(ce.getSource().equals(gridSizeSlider) && !gridSizeSlider.getValueIsAdjusting()){
			String cmd = Constants.CHANGE_GRID_SIZE_COMMAND + ":" + gridSizeSlider.getValue();
			send(cmd);
		} else if(ce.getSource().equals(gridUpdateRateSlider) && !gridUpdateRateSlider.getValueIsAdjusting()){
			String cmd = Constants.CHANGE_GRID_UPDATE_RATE_COMMAND + ":" + gridUpdateRateSlider.getValue();
			send(cmd);
		} else if(ce.getSource().equals(minimumNeighborsSlider) && !minimumNeighborsSlider.getValueIsAdjusting()){

			// The minimum cannot be > to the max.
			if(minimumNeighborsSlider.getValue() > maximumNeighborsSlider.getValue()){
				maximumNeighborsSlider.setValue(minimumNeighborsSlider.getValue());
			}

			String cmd = Constants.CHANGE_GRID_CELL_REQUIREMENT_COMMAND + ":"+minimumNeighborsSlider.getValue()+":"+maximumNeighborsSlider.getValue();
			send(cmd);
		} else if(ce.getSource().equals(maximumNeighborsSlider) && !maximumNeighborsSlider.getValueIsAdjusting()){

			// The minimum cannot be > to the max.
			if(maximumNeighborsSlider.getValue() < minimumNeighborsSlider.getValue()){
				minimumNeighborsSlider.setValue(maximumNeighborsSlider.getValue());
			}
			String cmd = Constants.CHANGE_GRID_CELL_REQUIREMENT_COMMAND + ":"+minimumNeighborsSlider.getValue()+":"+maximumNeighborsSlider.getValue();
			send(cmd);
		}
	}

	@Override
	public void actionPerformed(ActionEvent ap) {

		if(ap.getSource().equals(reset)){
			String cmd = Constants.RESET_GRID_COMMAND;
			send(cmd);
		}

	}

	private void send(String cmd) {
		
		// Not connected yet.
		if(clientSocket == null){
			return;
		}

		try {
			ByteBuffer buffer = ByteBuffer.wrap(cmd.getBytes());
			clientSocket.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setSocket(SocketChannel socket){
		this.clientSocket = socket;
	}
}
