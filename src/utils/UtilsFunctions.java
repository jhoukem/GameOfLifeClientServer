package utils;

import java.util.BitSet;

/**
 * This class regroup all the shared methods in the project.
 * 
 * @author Jean-Hugo
 */
public class UtilsFunctions {

	
	/**
	 * Sleep the current thread for the given time in second.
	 * (Convenient method)
	 * @param i time in second.
	 */
	public static void sleepSec(int i) {
		try {
			Thread.sleep(i*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Sleep the current thread for the given time in millisecond.
	 * (Convenient method)
	 * @param i time in millisecond.
	 */
	public static void sleepMilli(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * Display a BitSet a bit level. Useful for debugging.
	 * 
	 * @param bitField The BitSet to display.
	 * @param message A message to easily know which BitSet is displayed.
	 */
	public static void displayBitField(BitSet bitField, String message) {
		System.out.println(message);
		for(int i = 0; i < bitField.length(); i++){
			System.out.print(bitField.get(i) ? 1 : 0);
		}
		System.out.println();
	}
	
	/**
	 * Concatene multiples byte array together.
	 * 
	 * @param arrays the arrays to concatene.
	 * @return the array resulting of the concatenation of all given arrays.
	 */
	public static byte[] concatArray(byte[] ... arrays){
		
		byte[] concat;
		int size = 0;
		
		for(byte[] array : arrays){
			size += array.length;
		}
		
		// Allocate the correct size for the array.
		concat = new byte[size];
		
		int pos = 0;
		// Fill the new array.
		for(byte[] array : arrays){
			System.arraycopy(array, 0, concat, pos, array.length);
			pos += array.length;
		}
				
		return concat;
	}

}
