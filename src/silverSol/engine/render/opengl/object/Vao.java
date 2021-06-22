package silverSol.engine.render.opengl.object;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

import silverSol.math.OpenGLMath;

public class Vao {
		
	private int vaoID;
	private List<Vbo> vbos;
	
	private int vertexCount;
		private int renderedVertexCount;
	
	public Vao(int vertexCount) {
		this.vaoID = GL30.glGenVertexArrays();
		this.vbos = new ArrayList<>();
		this.vertexCount = vertexCount;
		this.renderedVertexCount = vertexCount;
	}
	
	public int getVaoID() {
		return vaoID;
	}

	public void setVaoID(int vaoID) {
		this.vaoID = vaoID;
	}

	public List<Vbo> getVbos() {
		return vbos;
	}

	public void setVbos(List<Vbo> vbos) {
		this.vbos = vbos;
	}
	
	public void bind() {
		GL30.glBindVertexArray(vaoID);
	}
	
	public void addAttribute(Vbo vbo, boolean normalizedData, int stride, int offset) {
		bind();
		vbo.bind();
		
		switch(vbo.getDataType()) {
			case GL11.GL_INT:
				GL30.glVertexAttribIPointer(vbo.getAttributeNumber(), vbo.getCoordinateSize(), GL11.GL_INT, vbo.getCoordinateSize() * OpenGLMath.BYTES_PER_INT, offset);
				break;
			case GL11.GL_FLOAT:
				GL20.glVertexAttribPointer(vbo.getAttributeNumber(), vbo.getCoordinateSize(), GL11.GL_FLOAT, normalizedData, vbo.getCoordinateSize() * OpenGLMath.BYTES_PER_INT, offset);
				break;
		}
		
		
		vbo.unbind();
		unbind();
		
		vbo.setVao(this);
		this.vbos.add(vbo);
	}
	
	public void addInstancedAttribute(Vbo vbo, int attributeNumber, int coordinateSize, int dataType, boolean normalizedData, int stride, int offset, int instanceDivisor) {
		bind();
		vbo.bind();
		
		switch(dataType) {
			case GL11.GL_INT:
				GL30.glVertexAttribIPointer(attributeNumber, coordinateSize, GL11.GL_INT, stride, offset);
				GL33.glVertexAttribDivisor(attributeNumber, instanceDivisor);
				break;
			case GL11.GL_FLOAT:
				GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, normalizedData, stride, offset);
				GL33.glVertexAttribDivisor(attributeNumber, instanceDivisor);
				break;
		}
		
		
		vbo.unbind();
		unbind();
		
		vbo.setVao(this);
		this.vbos.add(vbo);
	}
	
	public void addIndexVbo(Vbo vbo) {
		this.vbos.add(vbo);
	}
	
	public int getVertexCount() {
		return vertexCount;
	}

	public void setVertexCount(int vertexCount) {
		this.vertexCount = vertexCount;
		setRenderedVertexCount(vertexCount);
	}
	
	public int getRenderedVertexCount() {
		return renderedVertexCount;
	}

	public void setRenderedVertexCount(int renderedVertexCount) {
		this.renderedVertexCount = renderedVertexCount;
	}

	public void unbind() {
		GL30.glBindVertexArray(0);
	}
	
	public void delete() {
		for(Vbo vbo : vbos) {
			vbo.delete();
		}
		
		GL30.glDeleteVertexArrays(vaoID);
	}
	
	//Functionally identical to the non-static unbind(), but can be used anywhere. 
	public static void unbindVao() {
		GL30.glBindVertexArray(0);
	}
	
	@Override
	public String toString() {
		return "VAO ID " + vaoID;
	}
}
