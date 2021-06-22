package silverSol.engine.settings;

public class RenderSettings {

	private int openGLMajorVersion;
	private int openGLMinorVersion;
	
	private int fps;
	
	private int screenWidth;
	private int screenHeight;
	private int screenX;
	private int screenY;
	
	public RenderSettings() {
		this.openGLMajorVersion = 1;
		this.openGLMinorVersion = 1;
		this.fps = 60;
		this.screenWidth = 640;
		this.screenHeight = 480;
		this.screenX = 0;
		this.screenY = 0;
	}
	
	public RenderSettings(int openGLMajorVersion, int openGLMinorVersion, int fps, int screenWidth, int screenHeight,
			int screenX, int screenY) {
		this.openGLMajorVersion = openGLMajorVersion;
		this.openGLMinorVersion = openGLMinorVersion;
		this.fps = fps;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.screenX = screenX;
		this.screenY = screenY;
	}

	public int getOpenGLMajorVersion() {
		return openGLMajorVersion;
	}

	public void setOpenGLMajorVersion(int openGLMajorVersion) {
		this.openGLMajorVersion = openGLMajorVersion;
	}

	public int getOpenGLMinorVersion() {
		return openGLMinorVersion;
	}

	public void setOpenGLMinorVersion(int openGLMinorVersion) {
		this.openGLMinorVersion = openGLMinorVersion;
	}

	public int getFPS() {
		return fps;
	}

	public void setFPS(int fps) {
		this.fps = fps;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

	public int getScreenX() {
		return screenX;
	}

	public void setScreenX(int screenX) {
		this.screenX = screenX;
	}

	public int getScreenY() {
		return screenY;
	}

	public void setScreenY(int screenY) {
		this.screenY = screenY;
	}
	
}
