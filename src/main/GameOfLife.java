package main;



import java.awt.BorderLayout;

import javax.swing.JFrame;

import model.GridModel;
import view.GridView;

/**
 * This class act as the game manager, it holds all the objects to make the game run.
 * @author Jean-Hugo
 *
 */
public class GameOfLife extends JFrame{

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
	
	// The cycle counter that represent the current iteration number.
	private int cycle = 0;

	public GameOfLife(int size) {
		
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
			System.out.println("Cycle number "+cycle++);
			gridView.displayGridAscii();
			gridView.repaint();
			sleep(1);
			gridModel.update();
		}
	}


	private void sleep(int i) {
		try {
			Thread.sleep(i*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}

}
