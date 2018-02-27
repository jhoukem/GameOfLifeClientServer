package networkcontroller;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

import javax.swing.JLabel;

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

	private static final String LABEL_CONSTANT = "Cycle ";
	// The command panel used by the client to command the server.
	private CommandPanel commandPanel;
	// The the previous snapshot message received. Used for testing purposes.
	private byte[] lastSnapshotMessageReceived;
	// This is the label displayed on top of the grid view. It should be updated accordingly.
	private JLabel cycleLabel;

	public ClientGridController(GridModel gridModel, CommandPanel commandPanel, JLabel cycleLabel) {
		super(gridModel);
		this.commandPanel = commandPanel;
		this.cycleLabel = cycleLabel;
	}

	@Override
	public boolean processPendingCommands() {
		boolean needUpdate = false;

		synchronized (pendingCommands) {
			for(byte[] message : pendingCommands){

				// When a command has been received, an update is necessary.
				needUpdate = true;

				if(isWorldSnapshot(message)){
					this.lastSnapshotMessageReceived = message;
					// Skip the message code.
					processGridSnapshot(Arrays.copyOfRange(message, Short.BYTES, message.length));
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

		ByteBuffer buffer = ByteBuffer.wrap(message);
		
		// Skip the first 2 byte (message code).
		buffer.position(Short.BYTES);
		
		short currentGridSize = buffer.getShort();
		int currentUpdateRate =  buffer.getInt();
		int min = buffer.getShort();
		int max = buffer.getShort();
		int apparitionPercentage = buffer.getShort();
		int cycle = buffer.getInt();

		// Update the model accordingly.
		gridModel.setCurrentSize(currentGridSize);
		gridModel.setCellRequirement(min, max);
		gridModel.setCellApparitionPercentage(apparitionPercentage);
		gridModel.setCurrentCycle(cycle);

		// Update the GUI.
		commandPanel.setCurrentGridSize(currentGridSize);
		commandPanel.setCurrentUpdateRate(currentUpdateRate);
		commandPanel.setCellRequirement(min, max);
		commandPanel.setApparitionPercentage(apparitionPercentage);
		updateLabelCycle();
		
		
		// Initialize the grid tab.
		byte[] snapshot = Arrays.copyOfRange(message, buffer.position(), message.length);
		processGridSnapshot(snapshot);
	}

	private void updateLabelCycle() {
		cycleLabel.setText(LABEL_CONSTANT + gridModel.getCycle());
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
	protected void processGridReset(int appationPercentage) {
		gridModel.setCellApparitionPercentage(appationPercentage);
		gridModel.setCurrentCycle(0);
		updateLabelCycle();
		commandPanel.setApparitionPercentage(appationPercentage);
	}


	/**
	 * Parse the server message, get the server snapshot and fill the grid with it.
	 * 
	 * @param snapShot the data send by the server.
	 */
	private void processGridSnapshot(byte[] snapShot) {

		BitSet bitField = BitSet.valueOf(snapShot);

		gridModel.populateWithSnapshot(bitField);
		gridModel.incrementCycle();
		updateLabelCycle();

		if(Constants.DEBUG_BITSET){
			System.out.println("[CLIENT] bitSetCardinality = "+bitField.cardinality());
		}
	}

	/**
	 * @param message the data send by the server.
	 * @return whether the current message concern a world initialization
	 */
	private boolean isWorldInit(byte[] message) {
		return getCodeFromMessage(message) == Constants.GRID_INITIALIZATION;
	}

	/**
	 * @param message the data send by the server.
	 * @return whether the current message concern a world snapshot.
	 */
	private boolean isWorldSnapshot(byte[] message) {
		return getCodeFromMessage(message) == Constants.GRID_SNAPSHOT;
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
