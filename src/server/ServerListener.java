package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import networkcontrollers.ServerGridController;
import utils.Constants;

public class ServerListener implements Runnable{

	// Allow to easily switch debug log on/off.
	private final static boolean DEBUG = false;

	// The port the server will listen on.
	public static final int SERVER_PORT = 9999;

	// The controller that will handle data received from client.
	private ServerGridController serverController;

	// A list that keep a reference to all the connected client sockets.
	//private ArrayList<Socket> clientSockets = new ArrayList<>();
	// A list that keep a reference to all the connected client sockets.
	//private ArrayList<SocketChannel> clientSockets = new ArrayList<SocketChannel>();

	private Selector selector;
	// The socket channel to listen for client connection/inputs.
	private ServerSocketChannel serverSocketChannel;

	// The buffer used to read from client channels. TODO use later
	//	private ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);

	// Useful to stop the thread when the server run on gui mode.
	private boolean running = true;

	public ServerListener(ServerGridController controller) {
		this.serverController = controller;
		initSocket();
		this.serverController.setSelector(selector);
	}

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


	@Override
	public void run() {
		while(running){
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

	private void processReadySet(Set<SelectionKey> readySet) {

		Iterator<SelectionKey> iterator = readySet.iterator();

		while (iterator.hasNext()) {
			SelectionKey selectionKey = iterator.next();

			if (selectionKey.isAcceptable()) {
				try {
					ServerSocketChannel ssChannel = (ServerSocketChannel) selectionKey.channel();
					SocketChannel sChannel = (SocketChannel) ssChannel.accept();
					sChannel.configureBlocking(false);
					sChannel.register(selectionKey.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE);
					System.out.println("[Server]: New client connected with ip address: "+sChannel.getRemoteAddress());
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			if (selectionKey.isReadable()) {
				try {
					String msg = processRead(selectionKey);
					if (!msg.isEmpty()) {

						// Add the command to the pending command list so it will be processed later.
						serverController.addPendingCommand(msg);
						sendCommandToClients(msg);
						if(DEBUG){
							System.out.println("[Server] received command: "+msg);
						}

					}
				} catch (IOException e) {
					// On error cancel this key.
					selectionKey.cancel();
				}
			}

			iterator.remove();
		}
	}

	private void sendCommandToClients(String msg) {

		Iterator<SelectionKey> iterator = selector.keys().iterator();

		// Iterate through all the connected clients.
		while (iterator.hasNext()) {
			SelectionKey selectionKey = iterator.next();
			if(selectionKey.channel() == serverSocketChannel){
				continue;
			}
			SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
			try {
				clientChannel.write(ByteBuffer.wrap(msg.getBytes()));
			} catch (IOException e) {
				selectionKey.cancel();
				e.printStackTrace();
			}
		}
	}

	public String processRead(SelectionKey key) throws IOException {
		SocketChannel clientChannel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(Constants.BUFFER_SIZE);
		int bytesCount = clientChannel.read(buffer);
		if (bytesCount > 0) {
			buffer.flip();
			String msg = new String(buffer.array()).trim();
			return msg;
		}
		return "";
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

}
