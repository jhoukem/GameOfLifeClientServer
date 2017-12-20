package test;

import static org.junit.Assert.*;

import java.util.BitSet;

import game.GameOfLifeClient;
import game.GameOfLifeServer;
import utils.UtilsFunctions;

public class WorldSnapshotUpdateTest {



	private static final String LOCALHOST = "127.0.0.1";
	private static final int GRID_SIZE_FOR_TEST = 10;

	@org.junit.Test
	public void testClientReceivedCorrectMessageFromServer() {
		
		// Set to false since we do not want the JFrame to be visible.
		GameOfLifeServer server = new GameOfLifeServer(false);
		GameOfLifeClient client = new GameOfLifeClient(false);
		client.connectTo(LOCALHOST);
		server.getGrid().setCurrentSize(GRID_SIZE_FOR_TEST);

		for (int i = 0; i < 100; i++) {
			server.getGrid().resetGrid();
			server.getGrid().populateRandomly();
			for (int j = 0; j < 100; j++) {

				server.getGrid().update();
				BitSet serverSnapshot = server.getServerGridController().sendWorldSnapShotToClients();
				
				BitSet clientSnapshot = null;
				do {
					// This function should update the client world using the BitSet send by the server.
					client.getClientController().processPendingCommands();
					clientSnapshot = client.getClientController().popLastSnapshotReceived();
				} while(clientSnapshot == null);
				
				// Compare the two snapshot.
				boolean equals = isSnapShotEquals(serverSnapshot, clientSnapshot);
				
				// If they are not equal, we print some debug info.
				if(!equals){
					System.out.println("i = "+i+" j= "+j);	
					UtilsFunctions.displayBitField(serverSnapshot, "[SERVEUR]");
					UtilsFunctions.displayBitField(clientSnapshot, "[CLIENT]");
				}

				assertTrue(equals);
			}
		}

	}

	private boolean isSnapShotEquals(BitSet snapshot1, BitSet snapshot2) {

		float snapshotSize = snapshot1.length();

		for (int i = 0; i < snapshotSize; i++) {
			if(snapshot1.get(i) != snapshot2.get(i)){
				System.err.println("snapshot1["+i+"]="+snapshot1.get(i)+"\nsnapshot2["+i+"]="+snapshot2.get(i));
				return false;
			}
		}

		return true;
	}

}
