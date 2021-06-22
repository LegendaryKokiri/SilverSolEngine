package silverSol.engine.audio.sound;

public class Sound {

	private String filePath;
	private int soundID;
	private boolean currentlyPlaying;
	
	/**
	 * Creates a new instance of a sound.
	 * @param filePath The directory at which the sound can be found
	 */
	public Sound(String filePath) {
		this.filePath = filePath;
		this.soundID = -1;
		this.currentlyPlaying = false;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public int getSoundID() {
		return soundID;
	}

	public void setSoundID(int soundID) {
		this.soundID = soundID;
	}

	public boolean isCurrentlyPlaying() {
		return currentlyPlaying;
	}

	public void setCurrentlyPlaying(boolean currentlyPlaying) {
		this.currentlyPlaying = currentlyPlaying;
	}
}
