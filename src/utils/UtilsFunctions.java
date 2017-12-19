package utils;

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
	
}
