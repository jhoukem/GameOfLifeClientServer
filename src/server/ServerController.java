package server;

import java.util.ArrayList;

import model.GridModel;

public class ServerController {

	private GridModel gridModel;
	private ArrayList<GameUpdate> gameUpdates = new ArrayList<GameUpdate>();


	public ServerController(GridModel gridModel) {
		this.gridModel = gridModel;
	}

	public void processGameUpdate(){
		
		synchronized(gameUpdates){
			for(GameUpdate update : gameUpdates){
				update.processOn(gridModel);
			}
			gameUpdates.clear();
		}
		
	}

	public void addGameUpdate(GameUpdate update){
		gameUpdates.add(update);
	}

	public void processMessage(String msg) {
		// message received from the server parse it and add it to the update list.
	}

}
