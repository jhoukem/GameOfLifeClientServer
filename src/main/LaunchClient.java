package main;

import game.GameOfLifeClient;

/**
 * Launcher for the client.
 * 
 * @author Jean-Hugo
 *
 */
public class LaunchClient {
	
	public static void main(String[] args) {
		GameOfLifeClient client = new GameOfLifeClient();
		client.start();
	}
	
}
