package main;

import game.GameOfLifeClient;
import game.GameOfLifeLocal;

public class LaunchClient {
	
	public static void main(String[] args) {
		new GameOfLifeLocal(100);
//		new GameOfLifeClient();
	}
	
}
