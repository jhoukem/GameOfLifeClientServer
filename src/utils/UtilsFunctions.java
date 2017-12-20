package utils;

import java.util.BitSet;

public class UtilsFunctions {

	
	/**
	 * 
	 * @param i time to sleep the current thread in seconds.
	 */
	public static void sleep(int i) {
		try {
			Thread.sleep(i*1000);
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
	
	public static byte[] concatArray(byte[] ... arrays){
		
		byte[] concat;
		int size = 0;
		
		for(byte[] array : arrays){
			size += array.length;
		}
		concat = new byte[size];
		
		int pos = 0;
		for(byte[] array : arrays){
			System.arraycopy(array, 0, concat, pos, array.length);
			pos += array.length;
		}
				
		return concat;
	}
	
}
