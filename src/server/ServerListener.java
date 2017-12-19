package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class ServerListener implements Runnable{

	// The port the server will listen on.
	public static final int SERVER_PORT = 9999;

	// The controller that will handle data received from client.
	private ServerController serverController;

	// A list that keep a reference to all the connected client sockets.
	//private ArrayList<Socket> clientSockets = new ArrayList<>();
	// A list that keep a reference to all the connected client sockets.
	//private ArrayList<SocketChannel> clientSockets = new ArrayList<SocketChannel>();

	private Selector selector;
	// The socket channel to listen for client connection/inputs.
	private ServerSocketChannel serverSocketChannel;
	
	// Useful to stop the thread when the server run on gui mode.
	private boolean running = true;


	public ServerListener(ServerController controller) {
		this.serverController = controller;
		initSocket();
	}

	private void initSocket() {
		try {
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			// Set to non-blocking.
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.socket().bind(new InetSocketAddress(SERVER_PORT));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

			System.out.println("Server open. Listening on port: "+serverSocketChannel.socket().getLocalPort());
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

	private void processReadySet(Set<SelectionKey> readySet) throws IOException {

		Iterator<SelectionKey> iterator = readySet.iterator();

		while (iterator.hasNext()) {
			SelectionKey selectionKey = iterator.next();

			if (selectionKey.isAcceptable()) {
				ServerSocketChannel ssChannel = (ServerSocketChannel) selectionKey.channel();
				SocketChannel sChannel = (SocketChannel) ssChannel.accept();
				sChannel.configureBlocking(false);
				sChannel.register(selectionKey.selector(), SelectionKey.OP_READ);
				System.out.println("Server received a new connection");
			}
			if (selectionKey.isReadable()) {
				String msg = processRead(selectionKey);
				System.out.println("Server received : "+msg);

				serverController.processMessage(msg);
				/*if (msg.length() > 0) {
					SocketChannel sChannel = (SocketChannel) selectionKey.channel();
					ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
					sChannel.write(buffer);
				}*/
			}

			iterator.remove();
		}
	}

	public static String processRead(SelectionKey key) throws IOException {
		SocketChannel sChannel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int bytesCount = sChannel.read(buffer);
		if (bytesCount > 0) {
			buffer.flip();
			return new String(buffer.array());
		}
		return "NoMessage";
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
	
}
