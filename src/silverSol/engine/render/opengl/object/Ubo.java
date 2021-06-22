package silverSol.engine.render.opengl.object;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;

public class Ubo {
	
	public static int createUBO(int floatCount) {
		//Generate UBO
		int ubo = GL15.glGenBuffers();
				
		//Resize the UBO
		bindUBO(ubo);
		GL15.glBufferData(ubo, floatCount * 4, GL15.GL_DYNAMIC_DRAW);
		unbindUBO();
		
		return ubo;
	}
	
	public static void bindUBO(int uboID) {
		GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, uboID);
	}
	
	public static void unbindUBO() {
		GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, 0);
	}
	
	public static void updateBoundUbo(int ubo, float[] data, FloatBuffer buffer) {
		buffer.clear();
		buffer.put(data);
		buffer.flip();
		GL15.glBufferSubData(GL31.GL_UNIFORM_BUFFER, 0, buffer);
	}

}
