package silverSol.engine.input;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;

public class Input {
	
	//TODO: Add a similar release/inactive/pressed/held framework for the mouse. Mouse input will have to track position as well.
	
	private static Map<Integer, Integer> keys = new HashMap<>();
	private static Map<Integer, Integer> buffers = new HashMap<>();
	
	private static final int RELEASED = -1;
	private static final int INACTIVE = 0;
	private static final int PRESSED = 1;
	
	private static int maxHoldFrames = 60;
	private static int maxBufferFrames = 60;
	
	public static void trackKeys(int... keyCodes) {
		keys.clear();
		buffers.clear();
		
		for(int key : keyCodes) {
			keys.put(key, 0);
			buffers.put(key, 0);
		}
	}
	
	public static void poll() {
		for(int key : keys.keySet()) {
			int current = keys.get(key);
			int buffer = buffers.get(key);
			
			int keyState = Keyboard.isKeyDown(key) ? Math.min(current + 1, maxHoldFrames) : ((current >= PRESSED) ? RELEASED : INACTIVE);
			
			keys.put(key, keyState);
			buffers.put(key, keyState == PRESSED ? maxBufferFrames + 1 : Math.max(buffer - 1, 0));
		}		
	}
	
	public static boolean keyReleased(int key) {
		return keys.get(key) == RELEASED;
	}
	
	public static boolean keyInactive(int key) {
		return keys.get(key) == INACTIVE;
	}
	
	public static boolean keyPressed(int key) {
		return keys.get(key) == PRESSED;
	}
	
	public static boolean keyHeld(int key) {
		return keys.get(key) >= 2;
	}
	
	public static boolean keyHeld(int key, int holdFrames) {
		return keys.get(key) >= holdFrames;
	}
	
	public static boolean keyUp(int key) {
		return keys.get(key) <= INACTIVE;
	}
	
	public static boolean keyDown(int key) {
		return keys.get(key) >= PRESSED;
	}
	
	public static boolean keyBuffered(int key) {
		return buffers.get(key) > 0;
	}
	
	public static boolean keyBuffered(int key, int frameWindow) {
		return buffers.get(key) > maxBufferFrames - frameWindow;
	}
	
	public static void clearBuffer(int key) {
		buffers.put(key, 0);
	}
	
	public static void setMaxBufferFrames(int frames) {
		maxBufferFrames = frames;
	}
	
	public static void setMaxHoldFrames(int frames) {
		if(frames < 2) System.err.println("WARNING: Input can set maxHoldFrames to no fewer than 2.");
		maxHoldFrames = Math.max(frames, 2);
	}
	
}
