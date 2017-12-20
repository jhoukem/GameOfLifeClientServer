package networkcontrollers;

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

public class ServerGridController extends NetworkedGridController{

	// Allow to easily switch debug log on/off.
	private final static boolean DEBUG = false;

	// Used to retrieve connected players.
	private Selector selector;

	public ServerGridController(GridModel gridModel, Timer timer) {
		super(gridModel, timer);
	}

	@Override
	protected void processGridReset() {
		super.processGridReset();
		// Server side a reset trigger a randomized world.
//		gridModel.populateRandomly();
		gridModel.tata();
	}

	public void sendWorldSnapShot() {

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
				byte[] code = Constants.GRID_SNAPSHOT.getBytes();
				byte[] snapshot = bs.toByteArray();

				byte[] toSend = UtilsFunctions.concatArray(code, snapshot);

				if(DEBUG){
					bs = BitSet.valueOf(toSend);
					UtilsFunctions.displayBitField(bs, "On send");
				}

				clientChannel.write(ByteBuffer.wrap(toSend));

			} catch (IOException e) {
				selectionKey.cancel();
				e.printStackTrace();
			}
		}

	}

	public void setSelector(Selector selector) {
		this.selector = selector;
	}

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


}
