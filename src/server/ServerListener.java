package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Set;

import networkcontroller.ServerGridController;
import utils.Constants;
import utils.UtilsFunctions;

/**
 * This class is responsible for listening to any clients message and to store them in a list for a later processing.
 * 
 * @author Jean-Hugo
 *
 */
public class ServerListener implements Runnable{

	// Allow to easily switch debug log on/off.
	private final static boolean DEBUG = false;
	// The port the server will listen on.
	public static final int SERVER_PORT = 9999;
	// The controller that will handle data received from client.
	private ServerGridController serverController;

	// The selector allow to watch multiple socket without being blocked.
	private Selector selector;
	// The socket channel to listen for client connection/inputs.
	private ServerSocketChannel serverSocketChannel;

	public ServerListener(ServerGridController controller) {
		this.serverController = controller;
		initSocket();
		this.serverController.setSelector(selector);
	}

	/**
	 * Create the server socket and set up the listening process.
	 */
	private void initSocket() {
		try {
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			// Set to non-blocking.
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.socket().bind(new InetSocketAddress(SERVER_PORT));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("[Server] Started. Listening on port: "+serverSocketChannel.socket().getLocalPort());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Continuously read messages from all clients.
	 */
	@Override
	public void run() {
		while(true){
			try {
				if (selector.select() <= 0) {
					continue;
				}
				processReadySet(selector.selectedKeys());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * When the server has a channel ready for an operation (accept/read) this method is called.
	 * 
	 * @param readySet The Set representing the channel with pending operation.
	 */
	private void processReadySet(Set<SelectionKey> readySet) {

		Iterator<SelectionKey> iterator = readySet.iterator();

		while (iterator.hasNext()) {
			SelectionKey selectionKey = iterator.next();

			if (selectionKey.isAcceptable()) {
				try {
					SocketChannel clientChannel = (SocketChannel) serverSocketChannel.accept();
					clientChannel.configureBlocking(false);
					SelectionKey clientSelectionKey = clientChannel.register(selectionKey.selector(), SelectionKey.OP_READ);
					System.out.println("[Server]: New client connected with ip address: "+clientChannel.getRemoteAddress());
					sendClientGridInit(clientSelectionKey);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			if (selectionKey.isReadable()) {
				try {
					// Get the client message.
					byte[] message = processRead(selectionKey);
					
					if (message.length > 0) {

						// Add the command to the pending command list so it will be processed later.
						serverController.addPendingCommand(message);
						// Send the command to the others clients.
						sendCommandToClients(message);
						if(DEBUG){
							System.out.println("[Server] received command: "+new String(message).trim());
						}

					}
				} catch (IOException e) {
					// Cancel this selection key on write error (Client disconnected ?).
					selectionKey.cancel();
				}
			}
			iterator.remove();
		}
	}

	/**
	 * Send to the client the complete grid state (snapshot + grid parameter).
	 * 
	 * @param selectionKey the client SocketChannel.
	 */
	private void sendClientGridInit(SelectionKey selectionKey) {

		try {

			byte[] toSend = serverController.getInitializationMessage();
			// Send the data.
			SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
			clientChannel.write(ByteBuffer.wrap(toSend));
			if(DEBUG){
				BitSet bs = BitSet.valueOf(toSend);
				UtilsFunctions.displayBitField(bs, "On send");
			}
		} catch (IOException e) {
			// Cancel this selection key on write error (Client disconnected ?).
			selectionKey.cancel();
		}
	}

	/**
	 * Forward this message to all the clients.
	 * 
	 * @param message the message to be forwarded.
	 */
	private void sendCommandToClients(byte[] message) {

		Iterator<SelectionKey> iterator = selector.keys().iterator();

		// Iterate through all the connected clients.
		while (iterator.hasNext()) {
			SelectionKey selectionKey = iterator.next();
			if(selectionKey.channel() == serverSocketChannel){
				continue;
			}
			SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
			try {
				clientChannel.write(ByteBuffer.wrap(message));
			} catch (IOException e) {
				// Cancel this selection key on write error (Client disconnected ?).
				selectionKey.cancel();
			}
		}
	}

	/**
	 * Read a byte array from the client.
	 * 
	 * @param key The client SelectionKey
	 * @return a byte array filled with the client message.
	 * @throws IOException
	 */
	public byte[] processRead(SelectionKey key) throws IOException {
		SocketChannel clientChannel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
		clientChannel.read(buffer);
		return buffer.array();
	}

}
