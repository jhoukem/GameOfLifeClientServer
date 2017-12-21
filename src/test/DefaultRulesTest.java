package test;

import model.GridModel;
import static org.junit.Assert.*;
/**
 * This class allow me to ensure that the default rules of the game are respected.
 * 
 * @author Jean-Hugo
 */
public class DefaultRulesTest {

	/**
	 * Test if a simple game of life periodic structure is calculated correctly.
	 */
	@org.junit.Test
	public void testBasicPeriodicStructure(){
		GridModel model = new GridModel();
		
		// The position for the structure center.
		int x = model.getCurrentGridSize()/2;
		int y = model.getCurrentGridSize()/2;
		
		model.createBar(x, y);

		for(int i = 0; i < 100; i++){

			boolean[][] grid = model.getGrid();

			// Check the position of the living cells.
			
			// The center should always be set to true because there 2 neighbors at any time in the simulation.
			assertTrue(grid[x][y]);
			
			// Every 2 update this should be true.
			if((i % 2) == 0){
				assertTrue(grid[y][x - 1]);
				assertTrue(grid[y][x + 1]);
			} else {
				assertTrue(grid[y - 1][x]);
				assertTrue(grid[y + 1][x]);
			}

			// Update the simulation.
			model.update();
		}

	}

}
