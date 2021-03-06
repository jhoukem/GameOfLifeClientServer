package game;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import model.GridModel;
import networkcontroller.ServerGridController;
import server.ServerListener;
import utils.Constants;
import utils.Timer;
import view.GridView;

/**
 * This class act as the game manager, it holds all the objects to make the game run. Run a server so client can connect to it and display
 * its game state.
 * 
 * @author Jean-Hugo
 */
public class GameOfLifeServer extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// The grid that hold the game simulation.
	private GridModel gridModel;
	// The class that allow to visualize the simulation.
	private GridView gridView;
	// This thread run a server listener.
	private Thread serverListener;
	// This class handle all the updates from the network.
	private ServerGridController serverController;
	// Used to know when to perform an update without blocking the current thread.
	private Timer timer = new Timer();

	/**
	 * By default the server open a window.
	 */
	public GameOfLifeServer() {
		this(true);
	}

	/**
	 * @param visible Whether the game should open a JFrame. Useful for testing purpose.
	 */
	public GameOfLifeServer(boolean visible) {

		gridModel = new GridModel();
		serverController = new ServerGridController(gridModel, timer);
		initGraphics(visible);

		serverListener = new Thread(new ServerListener(serverController));
		serverListener.start();
	}

	/**
	 * Initialize the graphic components Panel/Buttons etc...
	 * 
	 * @param visible whether to display the JFrame.
	 */
	private void initGraphics(boolean visible) {
		gridView = new GridView(gridModel);
		this.setLayout(new BorderLayout());
		this.add(gridView, BorderLayout.CENTER);
		this.setSize(Constants.WIDTH, Constants.HEIGHT);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(visible);
	}


	/**
	 * Keep processing the client commands such has grid parameters, update periodically the game state
	 * and send the game state to the clients.
	 */
	public void start(){

		// Whether the game state has changed and need to be sent to the players.
		boolean needUpdate = true;
		
		while(true){
			timer.updateTimer();
			if(timer.isTimerOver(gridModel.getUpdateRate())){
				timer.resetTimer();
				gridModel.update();
				needUpdate = true;
			}

			// Process the commands sent by the clients. The result tell us whether we should send an update to the players.
			boolean needUpdate2 = serverController.processPendingCommands();

			needUpdate = needUpdate || needUpdate2;

			if(needUpdate){
				needUpdate = false;
				serverController.sendWorldSnapShotToClients();
			}
		}
	}

	public GridModel getModel(){
		return gridModel;
	}
	
	public ServerGridController getServerGridController(){
		return serverController;
	}

}
