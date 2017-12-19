package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientListener implements Runnable {

	// The client socket used to communicate with the server.
	private Socket socket;
	// This class will handle every message received from the server.
	private ClientController clientController;
	// Allow us to read lines from the socket.
	private BufferedReader in;

	private boolean running = true;

	public ClientListener(Socket socket, ClientController clientController) {
		this.socket = socket;
		this.clientController = clientController;

		try {
			this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while(running){
			try {
				String message = in.readLine();
				System.out.println("Client received message: "+message);
				clientController.processMessage(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
