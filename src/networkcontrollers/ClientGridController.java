package networkcontrollers;

import java.util.BitSet;

import model.GridModel;
import utils.Constants;
import utils.UtilsFunctions;
import view.CommandPanel;

public class ClientGridController extends NetworkedGridController{

	// Allow to easily switch debug log on/off.
	private final static boolean DEBUG = false;

	// The command panel used by the client to command the server.
	private CommandPanel commandPanel;
	// The the previous snapshot received. Used for testing purposes.
	private BitSet lastSnapshotReceived;

	public ClientGridController(GridModel gridModel, CommandPanel commandPanel) {
		super(gridModel);
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

	/**
	 * Initialize the game with the server data.
	 * 
	 * @param msg the data send by the server wrapped in a String.
	 */
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

		// Set the interval for a cell to survive.
		int min = Integer.parseInt(new String(command, offset++, 1));
		int max = Integer.parseInt(new String(command, offset++, 1));
		gridModel.setCellRequirement(min, max);
		commandPanel.setCellRequirement(min, max);

		// Initialize the grid tab.
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

	@Override
	protected void processGridReset() {
		// Do nothing on client.
	}


	/**
	 * Parse the server data and fill the grid with it.
	 * 
	 * @param msg
	 */
	private void processWorldSnapshot(String msg) {

		// Remove the first byte (message code).
		String snapshot = new String(msg.getBytes(), 1, msg.getBytes().length-1);

		BitSet bs = BitSet.valueOf(snapshot.getBytes());
		this.lastSnapshotReceived = bs;

		if(Constants.DEBUG_BITSET){
			System.out.println("[CLIENT] string snapshot = "+ snapshot);
			System.out.println("[CLIENT] bitSetCardinality = "+bs.cardinality());
		}


		if(DEBUG){
			UtilsFunctions.displayBitField(bs, "On receive");
		}

		gridModel.populateWithSnapshot(bs);
		gridModel.incrementCycle();
	}

	/**
	 * Return whether the current message concern a world initialization.
	 * 
	 * @param msg
	 * @return
	 */
	private boolean isWorldInit(String msg) {
		String code = new String(msg.getBytes(), 0, 1);
		return code.equals(Constants.GRID_INITIALIZATION);
	}

	/**
	 * Return whether the current message concern a world snapshot.
	 * 
	 * @param msg
	 * @return
	 */
	private boolean isWorldSnapshot(String msg) {
		String code = new String(msg.getBytes(), 0, 1);
		return code.equals(Constants.GRID_SNAPSHOT);
	}

	/**
	 * Return the last saved snapshot and delete its reference in this class so another call to this method
	 * will return null until a new snapshot is received. (Used for test purposes).
	 * 
	 * @return
	 */
	public BitSet popLastSnapshotReceived() {
		// Store the last snapshot received.
		BitSet bs = lastSnapshotReceived;
		// Delete the previous snapshot so it cannot be retrieved later (useful for test purpose).
		lastSnapshotReceived = null;
		return bs;
	}

}
