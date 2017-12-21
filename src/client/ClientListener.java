package client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import networkcontroller.ClientGridController;
import utils.Constants;

/**
 * This class is responsible for listening to any server message and to store them in a list for a later processing.
 * 
 * @author Jean-Hugo
 *
 */
public class ClientListener implements Runnable {

	// The client socket used to communicate with the server.
	private SocketChannel clientSocket;
	// This class will handle every message received from the server.
	private ClientGridController clientController;

	public ClientListener(SocketChannel socket, ClientGridController clientController) {
		this.clientSocket = socket;
		this.clientController = clientController;
	}

	@Override
	public void run() {
		while(true){
			try {
				ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
				int byteRead = clientSocket.read(buffer);
				clientController.addPendingCommand(buffer.array());

				if(Constants.DEBUG_BITSET){
					System.out.println("[CLIENT] read "+byteRead+" bytes");
				}
			} catch (IOException e) {
				// Connection failed the thread stops.
				break;
			}
		}
	}

}
