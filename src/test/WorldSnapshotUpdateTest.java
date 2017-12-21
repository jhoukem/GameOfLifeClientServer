package test;

import static org.junit.Assert.fail;

import game.GameOfLifeClient;
import game.GameOfLifeServer;

/**
 * This class allow me to test the communications between the client and the server.
 * 
 * @author Jean-Hugo
 *
 */
public class WorldSnapshotUpdateTest {

	// The number of time to reset the world.
	private static final int ITERATION = 100;
	// For every iteration the grid will be updated this amount of time.
	private static final int NUMBER_OF_SIMULATION_PER_ITERATION = 500;
	// Localhost address.
	private static final String LOCALHOST = "127.0.0.1";
	// The size of the grid used for the test.
	private static final int GRID_SIZE_FOR_TEST = 10;

	/**
	 * Test whether the client always received the correct message from the server with my read/write on socket implementation.
	 */
	@org.junit.Test
	public void testClientReceivedCorrectMessageFromServer() {

		// Set to false since we do not want the JFrame to be visible.
		GameOfLifeServer server = new GameOfLifeServer(false);
		GameOfLifeClient client = new GameOfLifeClient(false);

		// Set up the client/server.
		server.getGrid().setCurrentSize(GRID_SIZE_FOR_TEST);
		client.connectTo(LOCALHOST);
		
		for (int i = 0; i < ITERATION; i++) {
			server.getGrid().resetGrid();
			server.getGrid().populateRandomly();
			testSendReceive(server, client);
		}
	}

	/**
	 * Test that the client receive the correct data from the server.
	 * 
	 * @param server
	 * @param client
	 */
	private void testSendReceive(GameOfLifeServer server, GameOfLifeClient client) {
		
		for (int i = 0; i < NUMBER_OF_SIMULATION_PER_ITERATION; i++) {
			
			// Update the server grid.
			server.getGrid().update();
			
			// Send a world snapshot to the client.
			byte[] send = server.getServerGridController().sendWorldSnapShotToClients();

			// Get the snapshot on client.
			byte[] received = null;
			do {
				// This function should update the client world using the BitSet send by the server.
				client.getClientController().processPendingCommands();
				// Get the last snapshot received by the client.
				received = client.getClientController().popLastSnapshotMessageReceived();
			} while(received == null);

			// If it is not equals to the server snapshot
			if(dataNotEqual(send, received)){
				// Display the byte for debugging.
				displayArray(send, "s");
				displayArray(received, "r");
				// Fail the test.
				fail("The received byte arrays are not equal");
			}
		}
	}

	/**
	 * Compare 2 byte array data. Stop comparison at the first array size and does not care about array size differences. 
	 * @param send
	 * @param received
	 * @return
	 */
	private boolean dataNotEqual(byte[] send, byte[] received) {
		// I don't care if they are not the same size since the trailing space will be considered as dead cells anyway.
		for(int i = 0; i < send.length; i++){
			if(send[i] != received[i]){
				return true;
			}
		}
		return false;
	}

	/**
	 * Convenient method to display a byte array with a label to identify it.
	 * 
	 * @param array
	 * @param label
	 */
	private void displayArray(byte[] array, String label) {

		for(int i = 0; i < array.length; i++){
			System.out.print(label+"["+i+"]="+array[i]);
			if(i < array.length - 1){
				System.out.print(", ");
			}
		}
		System.out.println();
	}
}
