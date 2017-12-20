package utils;

public class Timer {

	// The time the last frame was called. Useful to calculate the current StateTime.
	private long lastFrame = 0;
	// The stateTime allow me to trigger some event a certain point in time.
	private float stateTime = 0;

	
	public boolean isTimerOver(float time){
		return stateTime > time;
	}
	
	public void updateTimer(){
		stateTime += getDelta();
	}
	
	public void resetTimer(){
		stateTime = 0;
	}
	
	private float getDelta() {
		long delta = System.currentTimeMillis() - lastFrame;
		lastFrame = System.currentTimeMillis();
		return (float)delta;
	}
}
