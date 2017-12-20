package view;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import model.GridModel;

/**
 *  This class can render a textual/graphical representation of the current grid state.
 * @author Jean-Hugo
 *
 */
public class GridView extends JPanel implements Observer{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Chacters ascii used to represent the cells state.
	private static final char DEAD_CELL_CHAR = '.';
	private static final char ALIVE_CELL_CHAR = '*';

	// Color used to represent the cells state.
	private static final Color ALIVE_CELL_COLOR = Color.GRAY;
	private static final Color DEAD_CELL_COLOR = Color.BLACK;

	// The grid to watch.
	private GridModel gridModel;


	public GridView(GridModel gridModel) {
		this.gridModel = gridModel;
		// The view will always be notified when the model updates.
		this.gridModel.addObserver(this);
	}


	/**
	 * Draw the game grid state.
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintGrid(g);
	}


	/**
	 * Display the actual state of the gridModel.
	 * 
	 * @param g
	 */
	private void paintGrid(Graphics g) {
		
		int gridSize = gridModel.getCurrentGridSize();
		boolean grid [][] = gridModel.getGrid();
		int cellSize = getCorrectSize()/gridModel.getCurrentGridSize();
		int widthPadding = (this.getWidth() - gridSize * cellSize) / 2;
		int heightPadding = (this.getHeight() - gridSize * cellSize) / 2;
				
		for (int i = 0; i < gridSize; i++) {
			for (int j = 0; j < gridSize; j++) {
				if(grid[i][j]){
					g.setColor(ALIVE_CELL_COLOR);
				} else {
					g.setColor(DEAD_CELL_COLOR);
				}
				g.fill3DRect(j * cellSize + widthPadding, i * cellSize + heightPadding, cellSize, cellSize, grid[i][j]);
			}
		}
	}
	

	/**
	 * Allow to get the correct size reference for the cellSize calculation. We always want to get the shortest dimension.
	 * @return
	 */
	private int getCorrectSize() {
		return this.getWidth() > this.getHeight() ? this.getHeight() : this.getWidth();
	}


	public void displayGridAscii(){

		int gridSize = gridModel.getCurrentGridSize();
		boolean grid [][] = gridModel.getGrid();

		System.out.println("Generation n "+gridModel.getCycle());
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


	@Override
	public void update(Observable o, Object arg) {
//		displayGridAscii();
		repaint();
	}

}
