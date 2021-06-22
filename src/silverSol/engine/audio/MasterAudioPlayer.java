package silverSol.engine.audio;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;
import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.audio.sound.Sound;

public class MasterAudioPlayer {
	
	public static final int SOUND_NAME_DOES_NOT_EXIST = 10000;
	public static final int BUFFER_INDEX_DOES_NOT_EXIST = 10001;
	
	private List<Integer> buffers;
	private List<Source> sources;
	
	public MasterAudioPlayer() {
		try {
			AL.create();
			
			setListenerPosition(0, 0, 0);
			setListenerVelocity(0, 0, 0);
			
			buffers = new ArrayList<>();
			sources = new ArrayList<>();
				sources.add(new Source());
		} catch(LWJGLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Updates the sounds currently set to be playing.
	 * @param sounds The sounds to be playing
	 * @throws FileNotFoundException
	 */
	public void update(List<Sound> sounds) throws FileNotFoundException {
		for(Sound sound : sounds) {
			if(!sound.isCurrentlyPlaying()) {
				loadSound(sound);
				playSound(buffers.size() - 1, sources.size() - 1, true);
				sound.setCurrentlyPlaying(true);
			}
		}
	}
	
	/**
	 * Loads a new sound and stores its data in the buffer.
	 * @param sound The sound to be loaded
	 * @return The ID of the new sound buffer
	 * @throws FileNotFoundException
	 */
	public int loadSound(Sound sound) throws FileNotFoundException {
		int buffer = AL10.alGenBuffers();
		sound.setSoundID(buffer);
		buffers.add(buffer);
		WaveData waveFile = WaveData.create(ClassLoader.getSystemResourceAsStream(sound.getFilePath()));
		AL10.alBufferData(buffer, waveFile.format, waveFile.data, waveFile.samplerate);
		waveFile.dispose();		
		return buffer;
	}
	
	public void addSource() {
		sources.add(new Source());
	}
	
	/**
	 * Plays the sound whose buffer's ID matches the passed ID
	 * @param sound The ID of the sound buffer to be played
	 * @param source The ID of the source from which to play the sound
	 * @param loop Whether the sound should be looped
	 */
	public void playSound(int sound, int source, boolean loop) {
		sources.get(source).play(buffers.get(sound), loop);
	}
	
	public void setListenerPosition(float x, float y, float z) {
		AL10.alListener3f(AL10.AL_POSITION, x, y, z);
	}
	
	public void setListenerPosition(Vector3f position) {
		setListenerPosition(position.x, position.y, position.z);
	}
	
	public void setListenerVelocity(float x, float y, float z) {
		AL10.alListener3f(AL10.AL_VELOCITY, x, y, z);
	}
	
	public void setListenerVelocity(Vector3f velocity) {
		setListenerVelocity(velocity.x, velocity.y, velocity.z);
	}
	
	/**
	 * Clears all buffers and sources from memory.
	 */
	public void cleanUp() {
		for(int buffer : buffers) {
			AL10.alDeleteBuffers(buffer);
		}
		
		for(Source source : sources) {
			source.delete();
		}
		
		AL.destroy();
	}
	
}
