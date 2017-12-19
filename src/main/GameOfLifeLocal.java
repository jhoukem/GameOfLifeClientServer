package main;



import java.awt.BorderLayout;

import javax.swing.JFrame;

import model.GridModel;
import utils.UtilsFunctions;
import view.GridView;

/**
 * This class act as the game manager, it holds all the objects to make the game run.
 * @author Jean-Hugo
 *
 */
public class GameOfLifeLocal extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// The game windows width.
	private static final int WIDTH = 700;
	// The game windows height.
	private static final int HEIGHT = 700;
	
	// The grid that hold the game simulation.
	private GridModel gridModel;
	// The class that allow to visualize the simulation.
	private GridView gridView;
	
	public GameOfLifeLocal(int size) {
		
		this.gridModel = new GridModel(size);
		this.gridView = new GridView(gridModel);
		initGraphics();
		start();
	}

	private void initGraphics() {
		this.setLayout(new BorderLayout());
		this.add(gridView, BorderLayout.CENTER);
		this.setSize(WIDTH, HEIGHT);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	public void start(){
		while(true){
			gridView.displayGridAscii();
			gridView.repaint();
			UtilsFunctions.sleep(1);
			gridModel.update();
		}
	}

}
