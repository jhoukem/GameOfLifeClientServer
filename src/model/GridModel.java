package model;

import java.util.BitSet;
import java.util.Observable;

import utils.Constants;

/**
 * This class represent the grid of the simulation, this is the model of the game.
 * 
 * @author Jean-Hugo
 */
public class GridModel extends Observable{

	private static final int APPARITION_PERCENTAGE = 20;
	// The maximum size that a grid can take.
	public static final int MAXIMUM_GRID_SIZE = 100;
	// The size of the grid by default.
	public static final int DEFAULT_GRID_SIZE = 10;

	// A 2 dimensional array that represent the current simulation (true = cell alive, false = dead_cell).
	private boolean grid[][] = new boolean[MAXIMUM_GRID_SIZE][MAXIMUM_GRID_SIZE];
	// Same as above but used to calculate the next game state without interfering with the current game state.
	private boolean gridReference[][] = new boolean[MAXIMUM_GRID_SIZE][MAXIMUM_GRID_SIZE];

	// The current size of the grid.
	private int currentGridSize = DEFAULT_GRID_SIZE;

	// The cycle counter that represent the current iteration number.
	private int cycle = 0;

	// The time in millisecond between each call to update.
	private int updateRate = 1000;

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
	 * Creates a basic periodic structure (for testing purpose).
	 * 
	 * @param x 
	 * @param y
	 */
	public void createBar(int x, int y) {
		setCell(getCorrectPosition(y - 1), x, true);
		setCell(y, x, true);
		setCell(getCorrectPosition(y + 1), x, true);
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

	/**
	 * Reset the counter cycle and set all the grid cells to dead.
	 */
	public void resetGrid(){
		cycle = 0;
		setAllGridsTo(false);
		notifyObservers();
	}

	/**
	 * Fill the grid with random cells.
	 */
	public void populateRandomly(){
		for (int i = 0; i < currentGridSize; i++) {
			for (int j = 0; j < currentGridSize; j++) {
				if(Math.random()*100 < APPARITION_PERCENTAGE){
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
					// Die if it hasn't the correct number of neighbors.
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
	 * I overridden this method for convenience since I don't want to call setChanged every time.
	 */
	@Override
	public void notifyObservers() {
		this.setChanged();
		super.notifyObservers();
	}

	/**
	 * Convenient method to set a cell to the given state since we need to populate both the reference and the actual grid.
	 */
	private void setCell(int i, int j, boolean alive) {
		grid[i][j] = alive;
		gridReference[i][j] = alive;
	}

	/**
	 * If the size it within the max and min bound, it will set the grid size accordingly to the
	 * given size.
	 * @param newGridSize The new size for the grid.
	 */
	public void setCurrentSize(int newGridSize) {
		if(newGridSize >= DEFAULT_GRID_SIZE && newGridSize <= MAXIMUM_GRID_SIZE){
			this.currentGridSize = newGridSize;
			// Set all the cell beyond the grid border to dead.
			for(int i = currentGridSize; i < grid.length; i++){
				for (int j = currentGridSize; j < grid.length; j++) {
					setCell(i, j, false);
				}
			}
			notifyObservers();
		} else {
			System.err.println("Invalid size given: "+ newGridSize);
		}
	}

	public void setUpdateRate(int newUpdateRate) {
		if(newUpdateRate >= Constants.MINIMUM_UPDATE_RATE && newUpdateRate < Constants.MAXIMUM_UPDATE_RATE){
			this.updateRate = newUpdateRate;
		}
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

	public float getUpdateRate() {
		return updateRate;
	}

	/**
	 * To represent the world, I used a BitSet. Each bit represent a cell a bit set to 1 is a living cell and a bit set to 0
	 * is a dead cell. I start from the top left corner.
	 * 
	 * @return A BitSet representing the current grid state.
	 */
	public BitSet getWorldSnapShot() {
		BitSet bitField = new BitSet();
		for (int i = 0; i < currentGridSize; i++) {
			for (int j = 0; j < currentGridSize; j++) {
				int idx = currentGridSize * i + j;
				if(grid[i][j]){
					bitField.set(idx);
				} else {
					bitField.clear(idx);
				}
			}
		}
		
		return bitField;
	}

	/**
	 * Use the BitSet to populate the world. The BitSet act as a bitField the bit num "x" correspond to a
	 * cell in the grid. If the bit is set then the corresponding cell is alive if the bit is missing or unset then the corresponding
	 * cell is dead.
	 * 
	 * @param bitField
	 */
	public void populateWithSnapshot(BitSet bitField) {

		for (int i = 0; i < currentGridSize; i++) {
			for (int j = 0; j < currentGridSize; j++) {
				boolean alive = bitField.get(currentGridSize * i +j);
				setCell(i, j, alive);
			}
		}
		notifyObservers();
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
