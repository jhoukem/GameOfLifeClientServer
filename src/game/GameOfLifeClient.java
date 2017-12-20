package game;



import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import client.ClientListener;
import model.GridModel;
import networkcontrollers.ClientGridController;
import server.ServerListener;
import utils.Constants;
import utils.Timer;
import view.CommandPanel;
import view.GridView;

/**
 * This class act as the game manager, it holds all the objects to make the game run.
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
	private GridView gridView;
	// This panel allow a client to send some commands to a server.
	private CommandPanel commandPanel;
	// This panel contains the connect button and the grid view panel.
	private JPanel viewPanel;
	// This button is used to connect to a server.
	private JButton connect;

	// This thread run a client listener.
	private Thread clientListenerThread;
	// This runnable listen on the network for server messages.
	private ClientListener clientListener;

	// This class handle all the updates from the network.
	private ClientGridController clientController;

	// This socket allow the client to communicate with the server.
	private SocketChannel clientSocket;

	private Timer timer = new Timer();

	public GameOfLifeClient() {

		gridModel = new GridModel();
		clientController = new ClientGridController(gridModel, timer);
		initGraphics();

//		askConnection();
				initNetwork("");
		start();
	}

	private void askConnection() {
		String ip = JOptionPane.showInputDialog(null, "Server IP");
		if(ip != null && !ip.isEmpty()){
			initNetwork(ip);
		}
	}

	private void initGraphics() {

		// Init the buttons.
		connect = new JButton("Connection");
		connect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				askConnection();
			}
		});

		// To avoid my button being streched.
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(connect);

		// Initialize the panels
		this.gridView = new GridView(gridModel);
		this.commandPanel = new CommandPanel();
		this.viewPanel = new JPanel();
		this.viewPanel.setLayout(new BorderLayout());
		this.viewPanel.add(gridView, BorderLayout.CENTER);
		this.viewPanel.add(buttonPanel, BorderLayout.SOUTH);

		commandPanel.setBorder(BorderFactory.createEmptyBorder(Constants.PANEL_TOP_PADDING, 5, Constants.PANEL_BOTTOM_PADDING, 5));
		viewPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, Constants.PANEL_BOTTOM_PADDING, 0));

		// Set the layout.
		this.setLayout(new BorderLayout());
		this.add(viewPanel, BorderLayout.CENTER);
		this.add(commandPanel, BorderLayout.WEST);
		this.setSize(Constants.WIDTH, Constants.HEIGHT);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	private void initNetwork(String ip) {
		initSocket(ip);
		initNetworkListener();
	}

	private void initSocket(String ip) {
		try {
			// A previous socket existed.
			if(clientSocket != null){
				clientSocket.close();
			}
			clientSocket = SocketChannel.open(new InetSocketAddress("127.0.0.1" , ServerListener.SERVER_PORT));
			commandPanel.setSocket(clientSocket);
			System.out.println("Connected to the server: "+clientSocket.socket().getRemoteSocketAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initNetworkListener() {

		clientListener = new ClientListener(clientSocket, clientController);
		clientListenerThread = new Thread(clientListener);
		clientListenerThread.start();
	}

	public void start(){
		while(true){

			if(isConnected()){

//				timer.updateTimer();
//				if(timer.isTimerOver(gridModel.getUpdateRate())){
//					gridView.repaint();
//					timer.resetTimer();
//					gridModel.update();
//				}
			}
			clientController.processPendingCommands();
		}
	}

	private boolean isConnected() {
		return clientSocket != null && clientSocket.isConnected();
	}

}
