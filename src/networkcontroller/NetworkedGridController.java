package networkcontroller;

import java.util.ArrayList;

import model.GridModel;
import utils.Constants;

/**
 * An abstract class to set up the base of listening and updating a grid from network update.
 * 
 * @author Jean-Hugo
 */
public abstract class NetworkedGridController {

	protected static final int DATA = 1;
	protected static final int TYPE = 0;

	// The game model.
	protected GridModel gridModel;
	// Command received from clients that still need to be processed.
	protected ArrayList<byte[]> pendingCommands = new ArrayList<byte[]>();
	

	public NetworkedGridController(GridModel gridModel) {
		this.gridModel = gridModel;
	}

	/**
	 * Add the given byte array to the commands that will be processed later.
	 * 
	 * @param message the server command to add.
	 */
	public void addPendingCommand(byte[] message){
		synchronized (pendingCommands) {
			pendingCommands.add(message);
		}
	}

	/**
	 * Process all the commands send by the client if there is any.
	 * 
	 * @return Whether the clients need to be updated about the game state
	 */
	public boolean processPendingCommands() {

		boolean needUpdate = false;

		synchronized (pendingCommands) {

			for(byte[] message : pendingCommands){
				// When a command has been received, an update is necessary.
				needUpdate = true;
				processCommand(message);
			}
			pendingCommands.clear();
		}

		return needUpdate;
	}

	/**
	 * Update the grid according to the given command.
	 * TODO check for malformed commands ?
	 * 
	 * @param message the server command to process.
	 */
	protected void processCommand(byte[] message) {

		// Remove any trailing space.
		String messageString = new String(message).trim();

		String[] command = messageString.split(":");

		switch (command[TYPE]) {
		case Constants.CHANGE_GRID_SIZE_COMMAND:
			if(command.length > 1){
				processGridSizeChange(Integer.parseInt(command[DATA]));
			}
			break;
		case Constants.CHANGE_GRID_UPDATE_RATE_COMMAND:
			if(command.length > 1){
				processGridUpdateRate(Integer.parseInt(command[DATA]));
			}
			break;
		case Constants.RESET_GRID_COMMAND:
			processGridReset();
			break;
		case Constants.CHANGE_GRID_CELL_REQUIREMENT_COMMAND:
			processGridCellRequirement(Integer.parseInt(command[DATA]), 
					Integer.parseInt(command[DATA+1]));
			break;
		default:
			break;
		}
	}

	protected void processGridCellRequirement(int min, int max) {
		gridModel.setCellRequirement(min, max);
	}

	protected void processGridUpdateRate(int newUpdateRate) {
		gridModel.setUpdateRate(newUpdateRate);
	}

	protected void processGridReset() {
		gridModel.resetGrid();
	}

	protected void processGridSizeChange(int newSize) {
		gridModel.setCurrentSize(newSize);
	}

}
