package silverSol.engine.render.shader.shaders;

import java.util.List;

import org.lwjgl.opengl.GL11;

import silverSol.engine.render.camera.Camera;
import silverSol.engine.render.gui.guis.TextGui;
import silverSol.engine.render.opengl.attribute.Attribute;
import silverSol.engine.render.opengl.attribute.AttributeVector2f;
import silverSol.engine.render.opengl.uniform.UniformFloat;
import silverSol.engine.render.opengl.uniform.UniformSampler2D;
import silverSol.engine.render.opengl.uniform.UniformVector2f;
import silverSol.engine.render.opengl.uniform.UniformVector3f;
import silverSol.engine.render.shader.ShaderProgram;

public class FontShader<T extends TextGui> extends ShaderProgram<T> {
	
	private UniformVector2f translation;
	
	private UniformSampler2D fontTextureAtlas;
	
	private UniformVector3f fontColor;
	private UniformFloat fontWidth;
	private UniformFloat fontEdgeWidth;
	
	private UniformVector3f borderColor;
	private UniformFloat borderWidth;
	private UniformFloat borderEdgeWidth;
	private UniformVector2f borderOffset;
	
	public FontShader() {
		super("/silverSol/engine/render/shader/shaders/fontVertexShader.txt",
				"/silverSol/engine/render/shader/shaders/fontFragmentShader.txt",
				new Attribute[]{new AttributeVector2f("position", false), new AttributeVector2f("textureCoordinates", false)});
		
		translation = new UniformVector2f(programID, "translation");
		
		fontTextureAtlas = new UniformSampler2D(programID, "fontTextureAtlas");
			fontTextureAtlas.connectTextureUnit(0);
		
		fontColor = new UniformVector3f(programID, "fontColor");
		fontWidth = new UniformFloat(programID, "fontWidth");
		fontEdgeWidth = new UniformFloat(programID, "fontEdgeWidth");
		
		borderColor = new UniformVector3f(programID, "borderColor");
		borderWidth = new UniformFloat(programID, "borderWidth");
		borderEdgeWidth = new UniformFloat(programID, "borderEdgeWidth");
		borderOffset = new UniformVector2f(programID, "borderOffset");
	}
	
	@Override
	public void preRender(Camera camera, List<T> entities) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}

	@Override
	public void preInstance(Camera camera, T textGui, int index) {
		translation.loadVector(textGui.getBody2d().getPosition());
		
		fontColor.loadVector(textGui.getFontColor());
		fontWidth.loadFloat(textGui.getFontWidth());
		fontEdgeWidth.loadFloat(textGui.getFontEdgeWidth());
		
		borderColor.loadVector(textGui.getBorderColor());
		borderWidth.loadFloat(textGui.getBorderWidth());
		borderEdgeWidth.loadFloat(textGui.getBorderEdgeWidth());
		borderOffset.loadVector(textGui.getBorderOffset());
	}

	@Override
	public void postInstance(Camera camera, T entity, int index) {
		
	}

	@Override
	public void postRender(Camera camera, List<T> entities) {
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	
}
