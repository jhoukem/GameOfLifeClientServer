package networkcontroller;

import java.util.Arrays;
import java.util.BitSet;

import model.GridModel;
import utils.Constants;
import view.CommandPanel;

/**
 * This class is used by both the client game and the client listener. It can store the update received from the server 
 * to process them later.
 * 
 * @author Jean-Hugo
 */
public class ClientGridController extends NetworkedGridController{

	// The command panel used by the client to command the server.
	private CommandPanel commandPanel;
	// The the previous snapshot message received. Used for testing purposes.
	private byte[] lastSnapshotMessageReceived;
	
	public ClientGridController(GridModel gridModel, CommandPanel commandPanel) {
		super(gridModel);
		this.commandPanel = commandPanel;
	}

	@Override
	public boolean processPendingCommands() {

		// When a command has been received, an update is necessary.
		boolean needUpdate = !pendingCommands.isEmpty();

		synchronized (pendingCommands) {

			for(byte[] message : pendingCommands){

				if(isWorldSnapshot(message)){
					processWorldSnapshot(message);
				} else if(isWorldInit(message)){
					processWorldInit(message);
				} else {
					processCommand(message);
				}

			}
			pendingCommands.clear();
		}

		return needUpdate;
	}

	/**
	 * Initialize the game with the server data by reading parameter from a byte array.
	 * 
	 * @param message the data send by the server.
	 */
	private void processWorldInit(byte[] message) {

		int offset = 0;

		// This size allow us to know how many byte are used to store the grid size.
		byte sizeToReadGridSize = message[1];

		offset = 2;
		// Set the grid size.
		int currentGridSize = Integer.parseInt(new String(message, offset, sizeToReadGridSize));

		// Increase the offset by the number of byte read.
		offset += sizeToReadGridSize;

		gridModel.setCurrentSize(currentGridSize);

		// Update the GUI.
		commandPanel.setCurrentGridSize(currentGridSize);

		// Set the grid update rate.
		// This size allow us to know how many byte are used to store the grid size.
		byte sizeToReadUpdateRate = message[offset];
		// Increase the offset by the number of byte read.
		offset += 1;

		int currentUpdateRate = Integer.parseInt(new String(message, offset, sizeToReadUpdateRate));
		// Increase the offset by the number of byte read.
		offset += sizeToReadUpdateRate;
		gridModel.setCurrentSize(currentGridSize);
		commandPanel.setCurrentUpdateRate(currentUpdateRate);

		// Set the interval for a cell to survive.
		int min = Integer.parseInt(new String(message, offset++, 1));
		int max = Integer.parseInt(new String(message, offset++, 1));
		gridModel.setCellRequirement(min, max);
		commandPanel.setCellRequirement(min, max);

		// Initialize the grid tab.
		byte[] snapshot = Arrays.copyOfRange(message, offset, message.length);
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
	 * Parse the server message, get the server snapshot and fill the grid with it.
	 * 
	 * @param message the data send by the server.
	 */
	private void processWorldSnapshot(byte[] message) {

		this.lastSnapshotMessageReceived = message;
		
		// Remove the message code to only keep the snapshot.
		byte[] snapshotByte = Arrays.copyOfRange(message, 1, message.length);
		BitSet bitField = BitSet.valueOf(snapshotByte);

		gridModel.populateWithSnapshot(bitField);
		gridModel.incrementCycle();
		
		if(Constants.DEBUG_BITSET){
			System.out.println("[CLIENT] bitSetCardinality = "+bitField.cardinality());
		}
	}

	/**
	 * @param message the data send by the server.
	 * @return whether the current message concern a world initialization
	 */
	private boolean isWorldInit(byte[] message) {
		String code = new String(message, 0, 1);
		return code.equals(Constants.GRID_INITIALIZATION);
	}

	/**
	 * @param message the data send by the server.
	 * @return whether the current message concern a world snapshot.
	 */
	private boolean isWorldSnapshot(byte[] message) {
		String code = new String(message, 0, 1);
		return code.equals(Constants.GRID_SNAPSHOT);
	}

	/**
	 * @return the last saved snapshot and delete its reference in this class so another call to this method
	 * will return null until a new snapshot is received. (Used for test purposes).
	 */
	public byte[] popLastSnapshotMessageReceived() {
		// Store the last snapshot received.
		byte[] bs = lastSnapshotMessageReceived;
		// Delete the previous snapshot messqge so it cannot be retrieved later (useful for test purpose).
		lastSnapshotMessageReceived = null;
		return bs;
	}

}
