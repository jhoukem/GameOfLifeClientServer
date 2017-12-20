package game;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import model.GridModel;
import networkcontrollers.ServerGridController;
import server.ServerListener;
import utils.Constants;
import utils.Timer;
import view.GridView;

public class GameOfLifeServer extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// The game state.
	private GridModel gridModel = new GridModel();
	private Thread serverListener;
	private ServerGridController serverController;

	// The class that allow to visualize the simulation.
	private GridView gridView;

	private Timer timer = new Timer();
	// Whether the game state has changed and need to be sent to the players.
	private boolean needUpdate = true;
	
	// Whether the server should open a window.
	private boolean isRunningGui = true;
	
	public GameOfLifeServer() {

		serverController = new ServerGridController(gridModel, timer);
		
		initGraphics();
		
		serverListener = new Thread(new ServerListener(serverController));
		serverListener.start();
		start();
	}


	private void initGraphics() {
		gridView = new GridView(gridModel);
		this.setLayout(new BorderLayout());
		this.add(gridView, BorderLayout.CENTER);
		this.setSize(Constants.WIDTH, Constants.HEIGHT);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(isRunningGui);
	}


	public void start(){

		while(true){
			timer.updateTimer();
			
			if(timer.isTimerOver(gridModel.getUpdateRate())){
				timer.resetTimer();
				gridModel.update();
				needUpdate = true;
			}

			// Process the commands sent by the clients. The result tell us whether we should send an update to the players.
			needUpdate = needUpdate ? needUpdate : serverController.processPendingCommands();
			
			if(needUpdate){
				needUpdate = false;
				serverController.sendWorldSnapShot();
			}
		}
	}

}
