package networkcontrollers;

import java.util.BitSet;

import model.GridModel;
import utils.Constants;
import utils.Timer;
import utils.UtilsFunctions;

public class ClientGridController extends NetworkedGridController{

	// Allow to easily switch debug log on/off.
	private final static boolean DEBUG = false;

	public ClientGridController(GridModel gridModel, Timer timer) {
		super(gridModel, timer);
	}


	@Override
	public boolean processPendingCommands() {

		// When a command has been received, an update is necessary.
		boolean needUpdate = !pendingCommands.isEmpty();

		synchronized (pendingCommands) {

			for(String msg : pendingCommands){
				
				if(isWorldSnapshot(msg)){
					processWorldSnapshot(msg);
				} else {
					processCommand(msg);
				}

			}
			pendingCommands.clear();
		}

		return needUpdate;
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
