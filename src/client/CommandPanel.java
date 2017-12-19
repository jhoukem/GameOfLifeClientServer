package client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.GridModel;
import utils.Constants;

public class CommandPanel extends JPanel implements ChangeListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	// Used to write to the server.
	private PrintWriter out;
	// The client socket which is conencted to the server.
	private Socket socket;
	
	private JSlider gridSizeSlider;
	private JSlider gridUpdateRateSlider;
	private JButton reset;
	

	public CommandPanel(Socket socket) {
		this.socket = socket;
		//initNetworkObjects();
		initGui();
	}

	private void initNetworkObjects() {
		try {
			out = new PrintWriter(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		this.setLayout(new BorderLayout());
		
		this.add(controlPanel, BorderLayout.CENTER);
		this.add(reset, BorderLayout.SOUTH);
		this.setBorder(BorderFactory.createEmptyBorder(50, 5, 50, 5));
	}

	private void createComponents() {
		gridSizeSlider = new JSlider(GridModel.DEFAULT_SIZE, GridModel.MAXIMUM_GRID_SIZE);
		
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
	    
	    reset = new JButton("Reset");
	}

	@Override
	public void stateChanged(ChangeEvent ce) {

		if(ce.getSource().equals(gridSizeSlider) && !gridSizeSlider.getValueIsAdjusting()){
			String cmd = "SIZE:" + gridSizeSlider.getValue();
			System.out.println("command send = "+cmd);
			//out.write(cmd);
			//out.flush();
		} else if(ce.getSource().equals(gridUpdateRateSlider) && !gridUpdateRateSlider.getValueIsAdjusting()){
			String cmd = "UPDATE_RATE:" + gridUpdateRateSlider.getValue();
			System.out.println("command send = "+cmd);
			//out.write(cmd);
			//out.flush();
		}
		
		
	}


}
