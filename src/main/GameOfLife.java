package main;

import model.GridModel;
import view.GridView;

/**
 * This class act as the game manager, it holds all the objects to make the game run.
 * @author Jean-Hugo
 *
 */
public class GameOfLife {

	// The grid that hold the game simulation.
	private GridModel gridModel;
	// The class that allow to visualise the simulation.
	private GridView gridView;
	
	// The cycle counter that represent the current iteration number.
	private int cycle = 0;

	public GameOfLife(int size) {
		this.gridModel = new GridModel(size);
		this.gridView = new GridView(gridModel);
	}

	public void start(){
		while(true){
			System.out.println("Cycle number "+cycle++);
			gridView.displayGridAscii();
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
