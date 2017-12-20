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
		gridModel.populateRandomly();
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

				byte[] toSend = new byte[code.length+snapshot.length];
				System.arraycopy(code, 0, toSend, 0, code.length);
				System.arraycopy(snapshot, 0, toSend, code.length, snapshot.length);

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


}
