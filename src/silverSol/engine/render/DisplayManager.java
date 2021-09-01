package silverSol.engine.render;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Vector2f;

public class DisplayManager {
	private int fps;
	private float targetDT;
		
	private Vector2f screenDimensions;
	private Vector2f pixelDimensions;
	private boolean fullScreen;
	
	private ContextAttribs contextAttribs;
	
	public DisplayManager() {
		this.fps = 30;
		this.targetDT = 1f / (float) fps;
		
		this.screenDimensions = new Vector2f(0, 0);
		this.pixelDimensions = new Vector2f(0, 0);
		this.fullScreen = false;
	}
	
	public void createDisplay(int openGLMajorVersion, int openGLMinorVersion, int screenWidth, int screenHeight, int screenX, int screenY) {
		contextAttribs = new ContextAttribs(openGLMajorVersion, openGLMinorVersion)
			.withForwardCompatible(true).withProfileCore(true);
		
		try {
			Display.create(new PixelFormat(), contextAttribs);
			Display.setDisplayMode(new DisplayMode(screenWidth, screenHeight));
			Display.setLocation(screenX, screenY);
			Display.setTitle("SilverSol Engine Game");
			Display.setVSyncEnabled(true);
			
			screenDimensions = new Vector2f(screenWidth, screenHeight);
			pixelDimensions = new Vector2f(2f / screenWidth, 2f / screenHeight);
			GL11.glViewport(0, 0, screenWidth, screenHeight);
			
			updateDisplay();
		} catch(LWJGLException e) {
			e.printStackTrace();
		}
	}
	
	public void updateDisplay() throws LWJGLException {
		Display.update();
		Display.sync(fps);	
	}
	
	public float getFPS() {
		return fps;
	}
	
	public void setFPS(int fps) {
		this.fps = fps;
		this.targetDT = 1f / (float) fps;
	}
	
	public boolean getFullscreen() {
		return fullScreen;
	}
	
	public void setFullscreen(boolean fullScreen) throws LWJGLException {
		this.fullScreen = fullScreen;
		
		if(fullScreen) Display.setDisplayModeAndFullscreen(Display.getDesktopDisplayMode());
		else Display.setDisplayMode(Display.getDesktopDisplayMode());
	}
	
	public float getTargetFrameTime() {
		return targetDT;
	}
	
	public Vector2f getDimensions() {
		return screenDimensions;
	}
	
	public Vector2f getPixelDimensions() {
		return pixelDimensions;
	}
	
	public static void closeDisplay() {
		Display.destroy();
	}
}
