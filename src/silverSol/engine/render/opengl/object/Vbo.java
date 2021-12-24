package silverSol.engine.render.opengl.object;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import silverSol.math.OpenGLMath;

public class Vbo {
	
	private Vao vao;
	private int vboID;
	
	private int attributeNumber;
	private int coordinateSize;
	private int dataType;
	private int drawType;
	
	private boolean indicesBuffer;
	
	public Vbo() {
		this.vboID = GL15.glGenBuffers();	
		this.drawType = GL15.GL_STATIC_DRAW;
		this.indicesBuffer = false;
	}
	
	public void bind() {
		if(!indicesBuffer) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		else GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
	}
	
	public void allocateData(int byteCount) {
		bind();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, byteCount, drawType);
		unbind();
	}
	
	public void storeAttribute(int[] data, int attributeNumber, int coordinateSize) {
		this.indicesBuffer = false;
		
		bind();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, Buffer.storeDataInBuffer(data), drawType);
		unbind();
		
		this.attributeNumber = attributeNumber;
		this.coordinateSize = coordinateSize;
		this.dataType = GL11.GL_INT;
	}
	
	public void storeAttribute(float[] data, int attributeNumber, int coordinateSize) {
		this.indicesBuffer = false;
		
		bind();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, Buffer.storeDataInBuffer(data), drawType);
		unbind();
		
		this.attributeNumber = attributeNumber;
		this.coordinateSize = coordinateSize;
		this.dataType = GL11.GL_FLOAT;
	}
	
	public void overwriteAttribute(float[] data) {
		overwriteAttribute(data, 0);
	}
	
	public void overwriteAttribute(float[] data, long offset) {
		bind();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, OpenGLMath.getFloatByteSize(data.length), drawType);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, offset, Buffer.storeDataInBuffer(data));
		unbind();
	}
	
	public void overwriteAttribute(FloatBuffer data) {
		bind();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data.capacity() * 4, drawType);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, data);
		unbind();
	}
	
	public void storeIndexData(int[] indices) {
		this.indicesBuffer = true;
		
		bind();
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, Buffer.storeDataInBuffer(indices), drawType);
	}
	
	public Vao getVao() {
		return vao;
	}

	public void setVao(Vao vao) {
		this.vao = vao;
	}

	public int getVboID() {
		return vboID;
	}

	public void setVboID(int vboID) {
		this.vboID = vboID;
	}
	
	public int getAttributeNumber() {
		return attributeNumber;
	}

	public void setAttributeNumber(int attributeNumber) {
		this.attributeNumber = attributeNumber;
	}

	public int getCoordinateSize() {
		return coordinateSize;
	}

	public void setCoordinateSize(int coordinateSize) {
		this.coordinateSize = coordinateSize;
	}
	
	public int getDataType() {
		return dataType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	
	public int getDrawType() {
		return drawType;
	}

	public void setDrawType(int drawType) {
		this.drawType = drawType;
	}

	public boolean isIndicesBuffer() {
		return indicesBuffer;
	}

	public void unbind() {
		if(!indicesBuffer) GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		else GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	public void delete() {
		GL15.glDeleteBuffers(vboID);
	}
	
	@Override
	public String toString() {
		return "VBO ID " + vboID + " (Attribute " + attributeNumber + ") (Size " + coordinateSize + ")";
	}
}
