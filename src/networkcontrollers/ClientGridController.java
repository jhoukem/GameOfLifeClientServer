package networkcontrollers;

import java.util.BitSet;

import model.GridModel;
import utils.Constants;
import utils.Timer;
import utils.UtilsFunctions;
import view.CommandPanel;

public class ClientGridController extends NetworkedGridController{

	// Allow to easily switch debug log on/off.
	private final static boolean DEBUG = false;

	// The command panel used by the client to command the server.
	private CommandPanel commandPanel;

	public ClientGridController(GridModel gridModel, Timer timer, CommandPanel commandPanel) {
		super(gridModel, timer);
		this.commandPanel = commandPanel;
	}


	@Override
	public boolean processPendingCommands() {

		// When a command has been received, an update is necessary.
		boolean needUpdate = !pendingCommands.isEmpty();

		synchronized (pendingCommands) {

			for(String msg : pendingCommands){

				if(isWorldSnapshot(msg)){
					processWorldSnapshot(msg);
				} else if(isWorldInit(msg)){
					processWorldInit(msg);
				} else {
					processCommand(msg);
				}

			}
			pendingCommands.clear();
		}

		return needUpdate;
	}

	private void processWorldInit(String msg) {

		byte[] command = msg.getBytes();
		int offset = 0;

		// This size allow us to know how many byte are used to store the grid size.
		byte sizeToReadGridSize = command[1];

		offset = 2;
		// Set the grid size.
		int currentGridSize = Integer.parseInt(new String(command, offset, sizeToReadGridSize));

		// Increase the offset by the number of byte read.
		offset += sizeToReadGridSize;

		gridModel.setCurrentSize(currentGridSize);

		// Update the GUI.
		commandPanel.setCurrentGridSize(currentGridSize);




		// Set the grid update rate.
		// This size allow us to know how many byte are used to store the grid size.
		byte sizeToReadUpdateRate = command[offset];
		// Increase the offset by the number of byte read.
		offset += 1;

		int currentUpdateRate = Integer.parseInt(new String(command, offset, sizeToReadUpdateRate));
		// Increase the offset by the number of byte read.
		offset += sizeToReadUpdateRate;
		gridModel.setCurrentSize(currentGridSize);
		commandPanel.setCurrentUpdateRate(currentUpdateRate);

		int min = Integer.parseInt(new String(command, offset++, 1));
		int max = Integer.parseInt(new String(command, offset++, 1));
		gridModel.setCellRequirement(min, max);
		commandPanel.setCellRequirement(min, max);
		
		String snapshot = new String(command, offset, command.length - offset);
		processWorldSnapshot(snapshot);
	}

	@Override
	protected void processGridSizeChange(int newSize) {
		super.processGridSizeChange(newSize);
		commandPanel.setCurrentGridSize(newSize);
	}
	
	@Override
	protected void processGridUpdateRate(int newUpdateRate) {
		super.processGridUpdateRate(newUpdateRate);
		commandPanel.setCurrentUpdateRate(newUpdateRate);
	}
	
	@Override
	protected void processGridCellRequirement(int min, int max) {
		super.processGridCellRequirement(min, max);
		commandPanel.setCellRequirement(min, max);
	}
	
	private boolean isWorldInit(String msg) {
		String code = new String(msg.getBytes(), 0, 1);
		return code.equals(Constants.GRID_INITIALIZATION);
	}


	private void processWorldSnapshot(String msg) {
		// Trim the byte representing the code.
		byte[] bytes = new String(msg.getBytes(), 1, msg.getBytes().length-1).getBytes();
		BitSet bs = BitSet.valueOf(bytes);

		if(DEBUG){
			UtilsFunctions.displayBitField(bs, "On receive");
		}

		gridModel.populateWithSnapshot(bs);
		gridModel.incrementCycle();
	}

	private boolean isWorldSnapshot(String msg) {
		String code = new String(msg.getBytes(), 0, 1);
		return code.equals(Constants.GRID_SNAPSHOT);
	}
	
}
