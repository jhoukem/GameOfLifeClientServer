package server;

import model.GridModel;
import utils.UtilsFunctions;
import view.GridView;

public class GameOfLifeServer {

	private GridModel gridModel = new GridModel();
	private Thread serverListener;
	private ServerController serverController;

	// The class that allow to visualize the simulation.
	private GridView gridView;

	public GameOfLifeServer() {

		serverController = new ServerController(gridModel);
		gridView = new GridView(gridModel);
		serverListener = new Thread(new ServerListener(serverController));
		serverListener.start();
		start();
	}


	public void start(){
		while(true){
//			gridView.displayGridAscii();
			UtilsFunctions.sleep(1);
			gridModel.update();
		}
	}

}
