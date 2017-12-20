package networkcontrollers;

import java.util.ArrayList;

import model.GridModel;
import utils.Constants;
import utils.Timer;

public abstract class NetworkedGridController {

	protected static final int DATA = 1;
	protected static final int TYPE = 0;

	// The game model.
	protected GridModel gridModel;
	// Command received from clients that still need to be processed.
	protected ArrayList<String> pendingCommands = new ArrayList<String>();
	// The timer used to calculate when the game need to be updated.
	protected Timer timer;

	public NetworkedGridController(GridModel gridModel, Timer timer) {
		this.timer = timer;
		this.gridModel = gridModel;
	}

	public void addPendingCommand(String msg){
		synchronized (pendingCommands) {
			pendingCommands.add(msg);
		}
	}

	public boolean processPendingCommands() {

		boolean needUpdate = false;

		synchronized (pendingCommands) {

			for(String msg : pendingCommands){
				// When a command has been received, an update is necessary.
				needUpdate = true;
				processCommand(msg);
			}
			pendingCommands.clear();
		}

		return needUpdate;
	}

	protected void processCommand(String msg) {

		// Remove any trailing space.
		msg = msg.trim();

		String [] command = msg.split(":");

		switch (command[TYPE]) {
		case Constants.CHANGE_GRID_SIZE_COMMAND:
			if(command.length > 1){
				processGridSizeChange(command[DATA]);
			}
			break;
		case Constants.CHANGE_GRID_UPDATE_RATE_COMMAND:
			if(command.length > 1){
				processGridUpdateRate(command[DATA]);
			}
			break;
		case Constants.RESET_GRID_COMMAND:
			processGridReset();
			break;
		case Constants.CHANGE_GRID_CELL_REQUIREMENT_COMMAND:
			processGridCellRequirement(command);
			break;
		default:
			break;
		}
	}

	private void processGridCellRequirement(String[] command) {
		int min = Integer.parseInt(command[1]);
		int max = Integer.parseInt(command[2]);
		gridModel.setCellRequirement(min, max);

	}

	private void processGridUpdateRate(String command) {
		int newUpdateRate = Integer.parseInt(command);
		gridModel.setUpdateRate(newUpdateRate);
	}

	protected void processGridReset() {
		gridModel.resetGrid();
		timer.resetTimer();
	}

	protected void processGridSizeChange(String command) {
		int newSize = Integer.parseInt(command);
		gridModel.setCurrentSize(newSize);
	}

}