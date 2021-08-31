package silverSol.parsers.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.render.animation.model.Keyframe;
import silverSol.engine.render.animation.model.ModelAnimation;
import silverSol.engine.render.armature.Bone;
import silverSol.math.MatrixMath;

public class AnimationParser {
	
	private static final Matrix4f CORRECTIVE_MATRIX = new Matrix4f().rotate((float) -Math.PI * 0.5f, new Vector3f(1, 0, 0));
	
	public static void parseAnimations(BufferedReader reader, Bone armature, String startingLine) throws IOException {
	    setReaderPosition(reader, startingLine);
	    parseBindPositions(reader, armature);
	    armature.calculateInverseBindTransformation(new Matrix4f());
	    parseAnimations(reader, armature);
	}
	
	private static void setReaderPosition(BufferedReader reader, String startingLine) throws IOException {
		while(!startingLine.equals("BIND MODEL SPACE POSITIONS")) {
			startingLine = reader.readLine();
	    }
	}
	
	private static void parseBindPositions(BufferedReader reader, Bone armature) throws IOException {
		String line = "";
	    while((line = reader.readLine()) != null && !line.equals("ANIMATIONS")) {
	    	if(line.length() > 0) {
		    	String[] data = line.split(" ");
		    	String boneName = data[0].split(" ")[0];
		    	
		    	float[] floatData = new float[data.length - 1];
				for(int i = 1; i < data.length; i++) {
					floatData[i - 1] = Float.parseFloat(data[i]);
				}
				
				Bone bone = armature.getBone(boneName);
				if(bone != null) {
					Matrix4f bindMatrix = MatrixMath.createMatrix4f(floatData);
					bindMatrix.transpose();
					if(bone.isRoot()) Matrix4f.mul(CORRECTIVE_MATRIX, bindMatrix, bindMatrix);
					bone.setLocalBindTransformation(bindMatrix);
				}
	    	}
	    }
	}
	
	private static void parseAnimations(BufferedReader reader, Bone armature) throws IOException {
		List<ModelAnimation> animations = new ArrayList<>();
	    float sourceFps = 24f;
	    float targetFps = 60f;
	    float fpsRatio = targetFps / sourceFps;
	    float secondsPerFrame = 1f / targetFps;
	    
	    String animationName = "";
		Map<Integer, List<Keyframe>> keyframes = new HashMap<>();
		int currentFrame = 0;
		int frameCount = 0;
		int animationIndex = 0;
		boolean oughtLoop = false;
	    
	    Map<Integer, Matrix4f> frameTransformations = new HashMap<>();
	    
	    String line = "";
		
		while((line = reader.readLine()) != null) {
			if(line.startsWith("FPS:")) {
				
				//TODO: Since the model converter doesn't export framerate data, this will always be set to default. It's fine for now, but it needs to be implemented in Python and Java
				String[] data = line.split(" ");
				sourceFps = (float) Integer.parseInt(data[1]);
				targetFps = (float) Integer.parseInt(data[2]);
				fpsRatio = targetFps / sourceFps;
				
			} else if(line.startsWith("ANIMATION:")) {

				animationName = line.split(" ")[1];
				oughtLoop = animationName.contains("Loop");
				
			} else if(line.endsWith("Frames")) {
				
				frameCount = (int) (Float.parseFloat(line.split(" ")[0]) * fpsRatio);

			} else if(line.startsWith("Frame")) {
				
				storeFrame(keyframes, frameTransformations, armature, (int) (currentFrame * fpsRatio), targetFps);
				currentFrame = Integer.parseInt(line.split(" ")[1]);
				
			} else if(line.equals("END OF ANIMATION")) {
				storeFrame(keyframes, frameTransformations, armature, (int) (currentFrame * fpsRatio), targetFps);
								
				//Add the animation
				ModelAnimation animation = new ModelAnimation(animationName, frameCount, secondsPerFrame, keyframes);
				animation.setIndex(animationIndex++);
				animation.setOughtLoop(oughtLoop);
				animations.add(animation);
				
				//Reset all animation data we've gained so far
				keyframes = new HashMap<>();
				
			} else if(line.length() > 0) {
				
				String[] stringData = line.split(" ");
				String boneName = stringData[0];
				int index = armature.getIndex(stringData[0]);
				
				if(keyframes.get(index) == null) {
					keyframes.put(index, new ArrayList<Keyframe>());
				}
				
				float[] floatData = new float[stringData.length - 1];
				for(int i = 1; i < stringData.length; i++) {
					floatData[i - 1] = Float.parseFloat(stringData[i]);
				}
				
				Matrix4f transformationMatrix = MatrixMath.createMatrix4f(floatData);
				transformationMatrix.transpose();
				if(armature.getBone(boneName).isRoot()) Matrix4f.mul(CORRECTIVE_MATRIX, transformationMatrix, transformationMatrix);
				frameTransformations.put(index, transformationMatrix);
			}
		}
		
		armature.addAnimations(animations);
	}
	
	private static boolean hasKeyframes(Map<Integer, Matrix4f> frameTransformations) {
		return !frameTransformations.isEmpty();
	}
	
	private static void storeFrame(Map<Integer, List<Keyframe>> keyframes, Map<Integer, Matrix4f> frameTransformations,
			Bone armature, int currentFrame, float fps) {
		if(hasKeyframes(frameTransformations)) {
			storeKeyframes(keyframes, frameTransformations, armature, currentFrame, fps);
			frameTransformations.clear();
		}
	}
	
	private static void storeKeyframes(Map<Integer, List<Keyframe>> keyframes, Map<Integer, Matrix4f> frameTransformations,
			Bone armature, int currentFrame, float fps) {
		Matrix4f transformationMatrix = frameTransformations.get(armature.getIndex());
		
		Vector3f translation = MatrixMath.getTranslation(transformationMatrix);
		Quaternion rotation = Quaternion.setFromMatrix(transformationMatrix, new Quaternion());
		Vector3f scale = new Vector3f(1f, 1f, 1f);
		
		if(keyframes.get(armature.getIndex()) == null) keyframes.put(armature.getIndex(), new ArrayList<Keyframe>());
		Keyframe keyframe = new Keyframe(currentFrame, fps, translation, rotation, scale);
		keyframes.get(armature.getIndex()).add(keyframe);
		
		for(Bone child : armature.getChildren()) {
			storeKeyframes(keyframes, frameTransformations, child, currentFrame, fps);
		}
	}
	
}
