package game;



import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import client.ClientListener;
import model.GridModel;
import networkcontroller.ClientGridController;
import server.ServerListener;
import utils.Constants;
import view.ClientGridView;
import view.CommandPanel;

/**
 * This class act as the game manager, it holds all the objects to make the game run. Connect to a server and display
 * its state.
 * @author Jean-Hugo
 *
 */
public class GameOfLifeClient extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// The grid that hold the game simulation.
	private GridModel gridModel;
	// The class that allow to visualize the simulation.
	private ClientGridView clientGridView;

	// Values for the GUI.
	private static final int PANEL_TOP_PADDING = 50;
	private static final int PANEL_BOTTOM_PADDING = 50;
	// This panel allow a client to send some commands to a server.
	private CommandPanel commandPanel;
	// This panel contains the connect button and the grid view panel.
	private JPanel viewPanel;
	// This button is used to connect to a server.
	private JButton connect;
	// This label display the current game cycle.
	private JLabel cycleLabel;

	// This thread run a client listener.
	private Thread clientListenerThread;
	// This class handle all the updates from the network.
	private ClientGridController clientController;
	// This socket allow the client to communicate with the server.
	private SocketChannel clientSocket;

	/**
	 * By default the client open a window.
	 */
	public GameOfLifeClient(){
		this(true);
	}

	/**
	 * @param visible Whether the game should open a JFrame. Useful for testing purpose.
	 */
	public GameOfLifeClient(boolean visible) {

		gridModel = new GridModel();
		initGraphics(visible);
		clientController = new ClientGridController(gridModel, commandPanel, cycleLabel);

		// If the window is open ask for a server ip.
		if(visible){
			askConnection();
		}
	}

	/**
	 * Initialize the graphic components Panel/Buttons etc...
	 * 
	 * @param visible whether to display the JFrame.
	 */
	private void initGraphics(boolean visible) {

		// Init the buttons.
		connect = new JButton("Connection");
		connect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				askConnection();
			}
		});
		// To avoid my button being stretched.
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(connect);

		cycleLabel = new JLabel();
		cycleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		// Initialize the panels
		this.clientGridView = new ClientGridView(gridModel);
		this.commandPanel = new CommandPanel();
		this.viewPanel = new JPanel();
		this.viewPanel.setLayout(new BorderLayout());
		this.viewPanel.add(cycleLabel, BorderLayout.NORTH);
		this.viewPanel.add(clientGridView, BorderLayout.CENTER);
		this.viewPanel.add(buttonPanel, BorderLayout.SOUTH);

		commandPanel.setBorder(BorderFactory.createEmptyBorder(PANEL_TOP_PADDING, 5, PANEL_BOTTOM_PADDING, 5));
		viewPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, PANEL_BOTTOM_PADDING, 0));

		// Set the layout.
		this.setLayout(new BorderLayout());
		this.add(viewPanel, BorderLayout.CENTER);
		this.add(commandPanel, BorderLayout.WEST);
		this.setSize(Constants.WIDTH, Constants.HEIGHT);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(visible);
	}


	/**
	 * Pop up a window that ask for a server ip to connect to and set up the connection if an ip is given.
	 */
	private void askConnection() {
		String ip = JOptionPane.showInputDialog(this, "Server IP", "Connection", JOptionPane.DEFAULT_OPTION);
		if(ip != null && !ip.isEmpty()){
			connectTo(ip);
		}
	}

	/**
	 * Connect the client to the server with the given ip and set up the client listeners.
	 * 
	 * @param ip the server ip address.
	 */
	public void connectTo(String ip) {
		initSocket(ip);
		if(isConnected()){
			initNetworkListener();

		}
	}

	/**
	 * Create the client socket. The actual connection process happens here.
	 * 
	 * @param ip the server ip address.
	 */
	private void initSocket(String ip) {
		try {
			// If a previous socket existed we close it.
			if(clientSocket != null){
				clientSocket.close();
			}
			clientSocket = SocketChannel.open(new InetSocketAddress(ip , ServerListener.SERVER_PORT));
			commandPanel.setSocket(clientSocket);
			clientGridView.setClientSocket(clientSocket);
			System.out.println("Connected to the server: "+clientSocket.socket().getRemoteSocketAddress());
		} 
		// Catch all exceptions here since SocketChannel open throw an exception.
		catch (Exception e) {
			if(isVisible()) {
				JOptionPane.showMessageDialog(this, "Connection failed", "Error", JOptionPane.ERROR_MESSAGE);
			} else {
				// If the game is not visible then it is running as a test and thus we want to log errors.
				e.printStackTrace();
			}
		}
	}

	/**
	 * Set up the thread that will listen for server updates.
	 */
	private void initNetworkListener() {
		// No need to keep a reference since the runnable will stop when the socket is closed.
		clientListenerThread = new Thread(new ClientListener(clientSocket, clientController));
		clientListenerThread.start();
	}

	/**
	 * Keep processing the server commands such has snapshot, grid parameters etc...
	 */
	public void start(){
		while(true){
			clientController.processPendingCommands();
		}
	}

	/**
	 * @return Whether the client is currently connected to a server.
	 */
	private boolean isConnected() {
		return clientSocket != null && clientSocket.isConnected();
	}

	public ClientGridController getClientController(){
		return clientController;
	}

}
