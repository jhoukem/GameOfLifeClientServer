package view;

import java.util.Observable;
import java.util.Observer;

import model.GridModel;

/**
 *  This class can render a textual/graphical representation of the current grid state.
 * @author Jean-Hugo
 *
 */
public class GridView implements Observer{

	private static final char DEAD_CELL_CHAR = '.';
	private static final char ALIVE_CELL_CHAR = '*';
	
	// The grid to watch.
	private GridModel gridModel;
	
	
	public GridView(GridModel gridModel) {
		this.gridModel = gridModel;
	}
	
	public void displayGridAscii(){
		
		int gridSize = gridModel.getActualSize();
		boolean grid [][] = gridModel.getGrid();
		
		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {
				if(grid[i][j]){
					System.out.print(ALIVE_CELL_CHAR);
				} else {
					System.out.print(DEAD_CELL_CHAR);
				}
			}
			System.out.println();
		}
		System.out.println();
	}

	// Called whenever the grid changes.
	@Override
	public void update(Observable arg0, Object arg1) {
		displayGridAscii();
	}
	
}
