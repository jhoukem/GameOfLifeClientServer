package client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import networkcontrollers.ClientGridController;
import utils.Constants;

public class ClientListener implements Runnable {

	// Allow to easily switch debug log on/off.
	private final static boolean DEBUG = false;

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

				if(Constants.DEBUG_BITSET){
					System.out.println("[CLIENT] read "+byteRead+" bytes");
				}

				String rawMessage = new String(buffer.array());
				clientController.addPendingCommand(rawMessage);
				if(DEBUG){
					System.out.println("[CLIENT] received: "+ rawMessage);
					System.out.println("[CLIENT] received: "+ rawMessage.trim());
				}
			} catch (IOException e) {
				// Connection failed the thread stops.
				break;
			}
		}
	}

}
