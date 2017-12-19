package client;



import java.awt.BorderLayout;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;

import model.GridModel;
import server.ServerListener;
import utils.UtilsFunctions;
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
	
	// The game windows width.
	private static final int WIDTH = 700;
	// The game windows height.
	private static final int HEIGHT = 700;
	
	// The grid that hold the game simulation.
	private GridModel gridModel;
	// The class that allow to visualize the simulation.
	private GridView gridView;
	// This panel allow a client to send some commands to a server.
	private CommandPanel commandPanel;
	
	// This thread listen on the network for server messages.
	private Thread clientListener;
	// This class handle all the updates from the network.
	private ClientController clientController;
	
	// In order to stop the client if the server disconnect.
	private boolean running = true;
	
	// This socket allow the client to communicate with the server.
	private Socket socket;
	
	public GameOfLifeClient() {
		
//		initSocket();
		
		gridModel = new GridModel();
		clientController = new ClientController(gridModel);
		
		initGraphics();
//		initNetworkListener();
		
	}

	private void initSocket() {
		try {
			socket = new Socket("127.0.0.1" , ServerListener.SERVER_PORT);
			System.out.println("Connected to the server: "+socket.getRemoteSocketAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initGraphics() {
		
		// Initialize the panels
		this.gridView = new GridView(gridModel);
		this.commandPanel = new CommandPanel(socket);
		
		// Set the layout.
		this.setLayout(new BorderLayout());
		this.add(gridView, BorderLayout.CENTER);
		this.add(commandPanel, BorderLayout.WEST);
		this.setSize(WIDTH, HEIGHT);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	
	private void initNetworkListener() {
		clientListener = new Thread(new ClientListener(socket, clientController));
		clientListener.start();
	}

	public void start(){
		while(running){
			gridView.repaint();
			UtilsFunctions.sleep(1);
			gridModel.update();
		}
	}

}
