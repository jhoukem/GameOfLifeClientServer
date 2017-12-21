package view;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import model.GridModel;
import utils.Constants;

/**
 * This class can render a textual/graphical representation of the current grid state and allow the client
 * to set a cell alive on the server.
 *  
 * @author Jean-Hugo
 */
public class ClientGridView extends GridView implements MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// The client socket which is connected to the server.
	private SocketChannel clientSocket;

	public ClientGridView(GridModel gridModel) {
		super(gridModel);
		this.addMouseListener(this);
	}

	/**
	 * Check if the mouse is within the grid size and if it is, it send a message to the server so it will
	 * set the cell alive at the clicked coordinates.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {

		// Not connected yet.
		if(clientSocket == null || !clientSocket.isConnected()){
			return;
		}
		
		Point touchPoint = e.getPoint();

		if(isValuesCorrect(touchPoint)){

			int y = (int)(touchPoint.getY() - heightPadding) / cellSize;
			int x = (int)(touchPoint.getX() - widthPadding) / cellSize;

			int cellPosition = gridModel.getCurrentGridSize() * y + x;
			String command = Constants.GRID_SET_CELL+":"+cellPosition;
			
			try {
				// Send the message to the server.
				clientSocket.write(ByteBuffer.wrap(command.getBytes()));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	}

	/**
	 * @param touchPoint the point clicked on the screen.
	 * @return true if the clicked point is within the grid size
	 */
	private boolean isValuesCorrect(Point touchPoint) {
		return (touchPoint.getX() >= widthPadding && touchPoint.getX() < (this.getWidth() - widthPadding) &&
				touchPoint.getY() >= heightPadding && touchPoint.getY() < (this.getHeight() - heightPadding));
	}

	public void setClientSocket(SocketChannel clientSocket) {
		this.clientSocket = clientSocket;
	}
	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {}

}
