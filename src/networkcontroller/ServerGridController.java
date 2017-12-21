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
	protected void processGridReset() {
		super.processGridReset();
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

		BitSet bs = gridModel.getWorldSnapShot();

		Iterator<SelectionKey> iterator = selector.keys().iterator();

		// Iterate through all the connected clients.
		while (iterator.hasNext()) {
			SelectionKey selectionKey = iterator.next();
			if(selectionKey.channel() instanceof ServerSocketChannel){
				continue;
			}
			SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
			try {
				// Create the snapshot message.
				byte[] code = Constants.GRID_SNAPSHOT.getBytes();
				byte[] snapshot = bs.toByteArray();
				byte[] toSend = UtilsFunctions.concatArray(code, snapshot);

				int byteSend = clientChannel.write(ByteBuffer.wrap(toSend));

				if(Constants.DEBUG_BITSET){
					System.out.println("[Server] send "+byteSend+" bytes");
					System.out.println("[Server] bitSetCardinality = "+bs.cardinality());
				}

				return toSend;
			} catch (IOException e) {
				selectionKey.cancel();
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Forge the initialization message that contains the all the current grid state.
	 * 
	 * @return The initialization message sent to the client on first connection.
	 */
	public byte[] getInitializationMessage() {

		byte[] code = Constants.GRID_INITIALIZATION.getBytes();

		byte[] gridSize = new String(""+gridModel.getCurrentGridSize()).getBytes();
		byte[] byteToReadForGridSize = {(byte) gridSize.length};

		byte[] gridUpdateRate = new String(""+(int)gridModel.getUpdateRate()).getBytes();
		byte[] byteToReadForUpdateRate = {(byte) gridUpdateRate.length};

		byte[] minCellRequirement = new String(""+gridModel.getMinimumCellRequirement()).getBytes();
		byte[] maxCellRequirement = new String(""+gridModel.getMaximumCellRequirement()).getBytes();

		byte[] snapshot = gridModel.getWorldSnapShot().toByteArray();

		if(DEBUG){
			System.out.println("bytetoReadForGridSize size = "+ byteToReadForGridSize.length);
			System.out.println("byte to read value = "+ byteToReadForGridSize[0]);
			System.out.println("grid size length = "+gridSize.length);
		}

		return UtilsFunctions.concatArray(code, byteToReadForGridSize, gridSize,
				byteToReadForUpdateRate, gridUpdateRate,
				minCellRequirement, maxCellRequirement,
				snapshot);
	}

	public void setSelector(Selector selector) {
		this.selector = selector;
	}

}