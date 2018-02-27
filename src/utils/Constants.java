package utils;

/**
 * Class that contains all the shared constants of the game.
 * 
 * @author Jean-Hugo
 */
public class Constants {

	// The game windows width.
	public static final int WIDTH = 900;
	// The game windows height.
	public static final int HEIGHT = 700;
	// The maximum update rate for the grid.
	public final static int MAXIMUM_UPDATE_RATE = 5000;
	// The minimum update rate for the grid.
	public final static int MINIMUM_UPDATE_RATE = 100;
	// The default update rate for the grid.
	public final static int DEFAULT_UPDATE_RATE = 1000;

	// Code identifier for networked messages.
	public static final short CHANGE_GRID_SIZE_COMMAND = 0;
	public static final short CHANGE_GRID_UPDATE_RATE_COMMAND = 1;
	public static final short RESET_GRID_COMMAND = 2;
	public static final short CHANGE_GRID_CELL_REQUIREMENT_COMMAND = 3;
	public static final short GRID_SNAPSHOT = 4;
	public static final short GRID_INITIALIZATION = 5;
	public static final short GRID_SET_CELL = 6;
	
	// Size of the buffer used to read and to send to client.
	public static final int BUFFER_SIZE = 2048;
	// The maximum neighbors a cell can have.
	public static final int MAXIMUM_CELL_NEIGHBORS = 8;
	// The minimum neighbors a cell can have.
	public static final int MINIMUM_CELL_NEIGHBORS = 0;
	// Used to see the data send from server to clients.
	public static final boolean DEBUG_BITSET = false;


}
