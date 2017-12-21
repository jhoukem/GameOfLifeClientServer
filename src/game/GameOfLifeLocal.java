package game;



import java.awt.BorderLayout;

import javax.swing.JFrame;

import model.GridModel;
import utils.Constants;
import utils.UtilsFunctions;
import view.GridView;

/**
 * This class act as the game manager, it holds all the objects to make the game run. Run the game locally.
 * 
 * @author Jean-Hugo
 *
 */
public class GameOfLifeLocal extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// The grid that hold the game simulation.
	private GridModel gridModel;
	// The class that allow to visualize the simulation.
	private GridView gridView;
	
	public GameOfLifeLocal(int size) {
		
		this.gridModel = new GridModel(size);
		this.gridModel.populateRandomly();
		this.gridView = new GridView(gridModel);
		initGraphics();
		start();
	}

	/**
	 * Initialize the graphic components Panel/Buttons etc...
	 */
	private void initGraphics() {
		this.setLayout(new BorderLayout());
		this.add(gridView, BorderLayout.CENTER);
		this.setSize(Constants.WIDTH, Constants.HEIGHT);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	/**
	 * Update the grid periodically.
	 */
	public void start(){
		while(true){
			gridView.displayGridAscii();
			gridView.repaint();
			UtilsFunctions.sleepSec(1);
			gridModel.update();
		}
	}

}
