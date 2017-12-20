package model;

import java.util.BitSet;
import java.util.Observable;

import utils.Constants;

/**
 * This class represent the grid of the simulation, this is the model of the game.
 * 
 * @author Jean-Hugo
 *
 */
public class GridModel extends Observable{


	// The maximum size that a grid can take.
	transient public static final int MAXIMUM_GRID_SIZE = 100;
	// The size of the grid by default.
	transient public static final int DEFAULT_GRID_SIZE = 10;

	// A 2 dimensional array that represent the current simulation (true = cell alive, false = dead_cell).
	private boolean grid[][] = new boolean[MAXIMUM_GRID_SIZE][MAXIMUM_GRID_SIZE];
	// Same as above but used to check the game state during the update without interfering the game state.
	private boolean gridReference[][] = new boolean[MAXIMUM_GRID_SIZE][MAXIMUM_GRID_SIZE];

	// The current size of the grid.
	private int currentGridSize = DEFAULT_GRID_SIZE;

	// The cycle counter that represent the current iteration number.
	private int cycle = 0;

	// The time in millisecond between each call to update.
	private int updateRate = 5000;

	// Default interval for a living cell to stay alive.
	private int minInterval = 2;
	private int maxInterval = 3;

	public GridModel() {
		this(DEFAULT_GRID_SIZE);
	}

	public GridModel(int size) {

		if(size > MAXIMUM_GRID_SIZE){
			System.err.println("Size given is too big : "+size+". Setted the grid size to "+MAXIMUM_GRID_SIZE);
			setCurrentSize(MAXIMUM_GRID_SIZE);
		} else {
			setCurrentSize(size);
		}

		resetGrid();
	}

	/**
	 * Creates a basic periodic structure.
	 * 
	 * @param x
	 * @param y
	 */
	private void createBar(int x, int y) {

		setCell(getCorrectPosition(y-1), x, true);
		setCell(y, x, true);
		setCell(getCorrectPosition(y+1), x, true);
	}

	public void tata() {
//		setAllGridsTo(true);
//		setCell(currentGridSize-1, currentGridSize-1, true);
		
		/*createBar(currentGridSize/2, currentGridSize/2);
		createBar(currentGridSize/2+1, currentGridSize/2);
		createBar(currentGridSize/2-1, currentGridSize/2);

		/*setCell(0, 0, true);
		setCell(0, 1, true);
		setCell(1, 0, true);
		setCell(1, 1, true);

		setCell(1, 1, true);
		setCell(1, 2, true);
		setCell(2, 1, true);
		setCell(2, 2, true);
		setCell(2, 3, true);
		setCell(1, 0, true);

		setCell(1, 1, true);
		setCell(1, 2, true);
		setCell(2, 1, true);
		setCell(2, 2, true);
		setCell(5, 3, true);
		setCell(6, 0, true);
		setCell(8, 1, true);
		setCell(5, 2, true);
		setCell(5, 1, true);
		setCell(4, 2, true);
		setCell(2, 3, true);
		setCell(5, 9, true);
		setCell(1, 5, true);
		setCell(3, 2, true);
		setCell(8, 8, true);
		setCell(3, 3, true);
		setCell(7, 3, true);
		setCell(5, 6, true);*/

		notifyObservers();
	}


	/**
	 * Reset the all grid cells to the given value (true = alive, false = dead).
	 */
	private void setAllGridsTo(boolean value) {
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid.length; j++) {
				setCell(i, j, value);
			}
		}
		notifyObservers();
	}

	public void resetGrid(){
		cycle = 0;
		setAllGridsTo(false);
		notifyObservers();
	}

	public void populateRandomly(){
		for (int i = 0; i < currentGridSize; i++) {
			for (int j = 0; j < currentGridSize; j++) {
				if(Math.random()*100 < 20){
					setCell(i, j, true);
				} else {
					setCell(i, j, false);
				}
			}
		}
		notifyObservers();
	}


	/**
	 * Process one simulation step.
	 */
	public void update() {
		incrementCycle();
		for(int i = 0; i < currentGridSize; i++){
			for (int j = 0; j < currentGridSize; j++) {
				int neighborsCount = getCellNeighbours(i, j);

				// Check in the grid reference so the update do not affect the current grid state.
				if(gridReference[i][j]){
					// Die if it does not have 2 or 3 neighbors.
					grid[i][j] = (neighborsCount >= minInterval && neighborsCount <= maxInterval);
				} else {
					// Become alive if it has just 3 neighbors.
					grid[i][j] = (neighborsCount == 3);
				}

			}
		}
		updateGridReference();
		notifyObservers();
	}

	public void incrementCycle() {
		cycle++;		
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
			return currentGridSize - 1;
		} else if(value >= currentGridSize){
			return 0;
		} else {
			return value;
		}
	}

	/*
	 * Set the reference grid to the current grid state.
	 */
	private void updateGridReference() {
		for(int i = 0; i < currentGridSize; i++){
			for (int j = 0; j < currentGridSize; j++) {
				gridReference[i][j] = grid[i][j];
			}
		}
	}

	/**
	 * I overridden this method for convenience since I don't want to call setChanged everytime.
	 */
	@Override
	public void notifyObservers() {
		this.setChanged();
		super.notifyObservers();
	}

	/**
	 * Convenient method to set a cell to the given state since we need to populate both the reference grid and the actual grid.
	 */
	private void setCell(int i, int j, boolean alive) {
		grid[i][j] = alive;
		gridReference[i][j] = alive;
	}

	public int getCycle() {
		return cycle;
	}

	public boolean[][] getGrid() {
		return grid;
	}

	public int getCurrentGridSize() {
		return currentGridSize;
	}

	public void setCurrentSize(int actualSize) {
		if(actualSize >= DEFAULT_GRID_SIZE && actualSize <= MAXIMUM_GRID_SIZE){
			this.currentGridSize = actualSize;

			// Kill all the cell beyond the grid border.
			for(int i = currentGridSize; i < grid.length; i++){
				for (int j = currentGridSize; j < grid.length; j++) {
					setCell(i, j, false);
				}
			}

			notifyObservers();
		}
	}

	public void setUpdateRate(int newUpdateRate) {
		if(newUpdateRate >= Constants.MINIMUM_UPDATE_RATE && newUpdateRate < Constants.MAXIMUM_UPDATE_RATE){
			this.updateRate = newUpdateRate;
		}
	}

	public float getUpdateRate() {
		return updateRate;
	}

	/**
	 * To represent the world, I used a bitfield. Each bit represent a cell a bit set to 1 is a living cell and a bit set to 0
	 * is a dead cell. I start from the top left corner.
	 * @return A bitfield representing the current living cells.
	 */
	public BitSet getWorldSnapShot() {
		BitSet bs = new BitSet();
		for (int i = 0; i < currentGridSize; i++) {
			for (int j = 0; j < currentGridSize; j++) {
				int idx = currentGridSize * i + j;
				if(grid[i][j]){
					bs.set(idx);
				} else {
					bs.clear(idx);
				}
			}
		}

		int gridCellCount = getAliveCellCount();
		int bitSetCount = bs.cardinality();
		if(gridCellCount != bitSetCount){
			System.err.println("[Server] Error the bitfield contains "+ bitSetCount +" living cell but the grid has "+gridCellCount);
		}
		
		
		return bs;
	}

	/**
	 * Use the bitfield to populate the world.
	 * @param bs
	 */
	public void populateWithSnapshot(BitSet bs) {

		for (int i = 0; i < currentGridSize; i++) {
			for (int j = 0; j < currentGridSize; j++) {
				boolean alive = bs.get(currentGridSize * i +j);
				setCell(i, j, alive);
			}
		}

		int gridCellCount = getAliveCellCount();
		int bitSetCount = bs.cardinality();
		
		if(gridCellCount != bitSetCount){
			System.err.println("[Client] Error the bitfield contains "+ bitSetCount +" living cell but the grid has "+gridCellCount);
		}
		
		notifyObservers();
	}

	/**
	 * Return the number of cell alive in the current simulation.
	 * @return
	 */
	public int getAliveCellCount(){
		int total = 0;
		for (int i = 0; i < currentGridSize; i++) {
			for (int j = 0; j < currentGridSize; j++) {
				if(grid[i][j]){
					total++;
				}
			}
		}
		return total;
	}

	public void setCellRequirement(int min, int max) {
		if(cellRequirementCorrect(min, max)){
			this.minInterval = min;
			this.maxInterval = max;
		} else {
			System.err.println("Wrong data received cell requirement is not correct. Min = "+min+" Max = "+max );
		}
	}

	public static boolean cellRequirementCorrect(int min, int max) {
		return (min >= 0 && max >= 0 && min <= Constants.MAXIMUM_CELL_NEIGHBORS && max <= Constants.MAXIMUM_CELL_NEIGHBORS
				&& min <= max);
	}

	public int getMinimumCellRequirement() {
		return minInterval;
	}

	public int getMaximumCellRequirement() {
		return maxInterval;
	}

}
