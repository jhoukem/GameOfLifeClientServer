package model;

/**
 * This class represent the grid of the simulation, this is the model of the game.
 * 
 * @author Jean-Hugo
 *
 */
public class GridModel {

	// The maximum size that a grid can take.
	public static final int MAXIMUM_GRID_SIZE = 100;
	// The size of the grid by default.
	public static final int DEFAULT_SIZE = 10;

	// A 2 dimensional array that represent the current simulation (true = cell alive, false = dead_cell).
	private boolean grid[][] = new boolean[MAXIMUM_GRID_SIZE][MAXIMUM_GRID_SIZE];
	// Same as above but used to check the game state during the update without interfering the game state.
	private boolean gridReference[][] = new boolean[MAXIMUM_GRID_SIZE][MAXIMUM_GRID_SIZE];

	// The current size of the grid.
	private int currentSize = DEFAULT_SIZE;

	// The cycle counter that represent the current iteration number.
	private int cycle = 0;

	public GridModel() {
		this(DEFAULT_SIZE);
	}

	public GridModel(int size) {

		if(size > MAXIMUM_GRID_SIZE){
			System.err.println("Size given is too big : "+size+". Setted the grid size to "+MAXIMUM_GRID_SIZE);
			setActualSize(MAXIMUM_GRID_SIZE);
		} else {
			setActualSize(size);
		}

		setAllGridsTo(false);

		grid[0][0] = true;
		grid[0][1] = true;
		grid[0][2] = true;
		gridReference[0][0] = true;
		gridReference[0][1] = true;
		gridReference[0][2] = true;
	}

	/**
	 * Reset the all grid cells to the given value (true = alive, false = dead).
	 */
	private void setAllGridsTo(boolean value) {
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid.length; j++) {
				grid[i][j] = value;
				gridReference[i][j] = value;
			}
		}
	}

	public void resetGrid(){
		cycle = 0;
		setAllGridsTo(false);
	}

	/**
	 * Process one simulation step.
	 */
	public void update() {
		cycle++;
		for(int i = 0; i < currentSize; i++){
			for (int j = 0; j < currentSize; j++) {
				int neighborsCount = getCellNeighbours(i, j);

				// Check in the grid reference so the update do not affect the current grid state.
				if(gridReference[i][j]){
					// Die if it does not have 2 or 3 neighbors.
					grid[i][j] = (neighborsCount == 2 || neighborsCount == 3);
				} else {
					// Become alive if it has just 3 neighbors.
					grid[i][j] = (neighborsCount == 3);
				}

			}
		}
		updateGridReference();
	}

	/**
	 * Return the number of neighbors for the cell at the given coordinates.
	 * 
	 */
	int getCellNeighbours(int y, int x){

		int count = 0;

		// Check right.
		count += gridReference[y][getCorrectPosition(x + 1)] ? 1 : 0;
		// Check left.
		count += gridReference[y][getCorrectPosition(x - 1)] ? 1 : 0;
		// Check down.
		count += gridReference[getCorrectPosition(y + 1)][x] ? 1 : 0;
		// Check up.
		count += gridReference[getCorrectPosition(y - 1)][x] ? 1 : 0;
		// Check down-right.
		count += gridReference[getCorrectPosition(y + 1)][getCorrectPosition(x + 1)] ? 1 : 0;
		// Check down-left.
		count += gridReference[getCorrectPosition(y + 1)][getCorrectPosition(x - 1)] ? 1 : 0;
		// Check up-right.
		count += gridReference[getCorrectPosition(y - 1)][getCorrectPosition(x + 1)] ? 1 : 0;
		// Check up-left.
		count += gridReference[getCorrectPosition(y - 1)][getCorrectPosition(x - 1)] ? 1 : 0;

		return count;

	}

	/**
	 * Allow the wrapping in the grid so when we hit a border, we wrap to the other side.
	 * 
	 * @param value the value we try to reach.
	 * @return the correct corresponding value in the grid.
	 */
	private int getCorrectPosition(int value) {

		if(value < 0) {
			return currentSize - 1;
		} else if(value >= currentSize){
			return 0;
		} else {
			return value;
		}
	}

	/*
	 * Set the reference grid to the current grid state.
	 */
	private void updateGridReference() {
		for(int i = 0; i < currentSize; i++){
			for (int j = 0; j < currentSize; j++) {
				gridReference[i][j] = grid[i][j];
			}
		}
	}

	public int getCycle() {
		return cycle;
	}

	public boolean[][] getGrid() {
		return grid;
	}

	public int getActualSize() {
		return currentSize;
	}

	public void setActualSize(int actualSize) {
		this.currentSize = actualSize;
	}

}
