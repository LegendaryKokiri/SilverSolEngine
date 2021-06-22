package silverSol.engine.audio;

import org.lwjgl.openal.AL10;

public class Source {

	private int sourceID;
	
	public Source() {
		sourceID = AL10.alGenSources();
		if(AL10.alGetError() != AL10.AL_NO_ERROR) System.err.println("There was an error generating sources.");
		
		AL10.alSourcef(sourceID, AL10.AL_GAIN, 1.0f);
		AL10.alSourcef(sourceID, AL10.AL_PITCH, 1.0f);
		AL10.alSource3f(sourceID, AL10.AL_POSITION, 0, 0, 0);
	}
	
	/**
	 * Plays the sound whose buffer's ID matches the ID passed as a parameter.
	 * @param buffer The ID of the buffer corresponding to the sound to be played
	 * @param loop Whether or not the sound should be looped
	 */
	public void play(int buffer, boolean loop) {
		AL10.alSourcei(sourceID, AL10.AL_BUFFER, buffer);
		
		if(loop) AL10.alSourcei(sourceID, AL10.AL_LOOPING, 1);
		else AL10.alSourcei(sourceID, AL10.AL_LOOPING, 0);
		
		AL10.alSourcePlay(sourceID);
	}
	
	/**
	 * Clears this source from memory.
	 */
	public void delete() {
		AL10.alDeleteSources(sourceID);
	}
	
}
