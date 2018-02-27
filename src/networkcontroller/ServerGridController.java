package networkcontroller;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.BitSet;
import java.util.Iterator;

import model.GridModel;
import utils.Constants;
import utils.Timer;
import utils.UtilsFunctions;

/**
 * This class is used by both the server game and the server listener. It can store the update received from the clients 
 * to process them later.
 * 
 * @author Jean-Hugo
 */
public class ServerGridController extends NetworkedGridController{

	// Allow to easily switch debug log on/off.
	private final static boolean DEBUG = false;

	// Used to retrieve connected players.
	private Selector selector;
	// The timer used to calculate when the game need to be updated.
	protected Timer timer;

	public ServerGridController(GridModel gridModel, Timer timer) {
		super(gridModel);
		this.timer = timer;
	}
	
	@Override
	protected void processSettingCell(int cellPosition) {
		gridModel.setCell(cellPosition);
	}
	
	@Override
	protected void processGridReset(int appationPercentage) {
		super.processGridReset(appationPercentage);
		timer.resetTimer();
		// Server side a reset trigger a randomized world.
		gridModel.populateRandomly();
	}

	/**
	 * Send a world snapshot to the connected client.
	 * 
	 * @return The message representing the world current state send to the clients.
	 */
	public byte[] sendWorldSnapShotToClients() {

		if(clientsConnected()){

			BitSet bs = gridModel.getWorldSnapShot();

			// Create the snapshot message.
			byte[] code = ByteBuffer.allocate(Short.BYTES).putShort(Constants.GRID_SNAPSHOT).array();
			byte[] snapshot = bs.toByteArray();
			byte[] toSend = UtilsFunctions.concatArray(code, snapshot);

			if(Constants.DEBUG_BITSET){
				System.out.println("[Server] bitSetCardinality = "+bs.cardinality());
			}
			
			sendToClients(toSend);
			return toSend;
		} else {
			return null;
		}
	}

	/**
	 * If there is more than one channel registered then there is clients that are connected.
	 * 
	 * @return true if there is a least a client connected false otherwise.
	 */
	private boolean clientsConnected() {
		return selector.keys().size() > 1;
	}

	private void sendToClients(byte[] toSend) {
		
		Iterator<SelectionKey> iterator = selector.keys().iterator();

		// Iterate through all the connected clients.
		while (iterator.hasNext()) {
			SelectionKey selectionKey = iterator.next();
			// If it is the server channel continue.
			if(selectionKey.channel() instanceof ServerSocketChannel){
				continue;
			}
			// Valid client channel.
			SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
			try {

				int byteSend = clientChannel.write(ByteBuffer.wrap(toSend));

				if(Constants.DEBUG_BITSET){
					System.out.println("[Server] send "+byteSend+" bytes");
				}
			} catch (IOException e) {
				selectionKey.cancel();
			}
		}
	}

	/**
	 * Forge the initialization message that contains the all the current grid state.
	 * 
	 * @return The initialization message sent to the client on first connection.
	 */
	public byte[] getInitializationMessage() {

		
		// Add the message code.
		byte[] code = ByteBuffer.allocate(Short.BYTES).putShort(Constants.GRID_INITIALIZATION).array();

		// Only allocate 1 byte because the size can never be bigger than 100.
		byte[] gridSize = ByteBuffer.allocate(Short.BYTES).putShort((short)gridModel.getCurrentGridSize()).array();
		
		// The update rate only allocate 2 because we only need 13 bit (2 bytes) to store the update rate (which is 0 to 5000).
		byte[] gridUpdateRate = ByteBuffer.allocate(Integer.BYTES).putInt((int)gridModel.getUpdateRate()).array();
		byte[] minCellRequirement = ByteBuffer.allocate(Short.BYTES).putShort((short)gridModel.getMinimumCellRequirement()).array();
		byte[] maxCellRequirement = ByteBuffer.allocate(Short.BYTES).putShort((short)gridModel.getMaximumCellRequirement()).array();
		byte[] apparitionPercentage = ByteBuffer.allocate(Short.BYTES).putShort((short)gridModel.getApparitionPercentage()).array();
		byte[] currentCycle = ByteBuffer.allocate(Integer.BYTES).putInt(gridModel.getCycle()).array();
		
		byte[] snapshot = gridModel.getWorldSnapShot().toByteArray();

		if(DEBUG){
			System.out.println("grid size length = "+gridSize.length);
		}

		return UtilsFunctions.concatArray(code, gridSize,
				gridUpdateRate,
				minCellRequirement, maxCellRequirement,
				apparitionPercentage,
				currentCycle,
				snapshot);
	}

	public void setSelector(Selector selector) {
		this.selector = selector;
	}

}
