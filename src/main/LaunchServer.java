package main;

import game.GameOfLifeServer;

/**
 * Launcher for the server.
 * 
 * @author Jean-Hugo
 *
 */
public class LaunchServer {

	public static void main(String[] args) {
		GameOfLifeServer server = new GameOfLifeServer();
		// Start the game simulation.
		server.start();
	}
	
}
