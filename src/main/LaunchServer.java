package main;

import game.GameOfLifeServer;

public class LaunchServer {

	public static void main(String[] args) {
		GameOfLifeServer server = new GameOfLifeServer();
		// Start the game simulation.
		server.start();
	}
	
}
