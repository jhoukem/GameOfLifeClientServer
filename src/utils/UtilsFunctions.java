package utils;

import java.util.BitSet;

public class UtilsFunctions {

	
	/**
	 * 
	 * @param i time to sleep the current thread in seconds.
	 */
	public static void sleep(int i) {
		try {
			Thread.sleep(i*100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}

	public static void displayBitField(BitSet bs, String message) {
		System.out.println(message);
		for(int i = 0; i < bs.length(); i++){
			System.out.print(bs.get(i) ? 1 : 0);
		}
		System.out.println();
	}
	
}
