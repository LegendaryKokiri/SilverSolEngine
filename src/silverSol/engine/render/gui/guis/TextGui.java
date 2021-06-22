package silverSol.engine.render.gui.guis;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d2.body.Body;
import silverSol.engine.render.font.Font;
import silverSol.engine.render.font.TextModelBuilder;
import silverSol.engine.render.gui.Gui;
import silverSol.engine.render.model.Model;

public class TextGui extends Gui {

	//Text
	private String text;
	
	//Positional Formatting
	private int numberOfLines;
	private float maxLineSize;
	private boolean centered;
	
	//Font Formatting
	private Font font;
	private float fontSize;
	private Vector3f fontColor;
	private float fontWidth;
	private float fontEdgeWidth;
	
	//Border Formatting
	private Vector3f borderColor;
	private float borderWidth;
	private float borderEdgeWidth;
	private Vector2f borderOffset;
	
	public TextGui(String text, Font font, float fontSize, float maxLineSize, boolean centered) {
		super(Model.VertexType.ELEMENTS, false, 1f, 1f);
		
		this.text = text;
		
		this.numberOfLines = 0;
		this.maxLineSize = maxLineSize;
		this.centered = centered;
		
		this.font = font;
		this.fontSize = fontSize;
		this.fontColor = new Vector3f(0, 0, 0);
		this.fontWidth = 0.6f;
		this.fontEdgeWidth = 0.1f;
		
		this.borderColor = new Vector3f(1f, 1f, 1f);
		this.borderWidth = 0.0f;
		this.borderEdgeWidth = 0.0f;
		this.borderOffset = new Vector2f(0f, 0f);
		
		this.setModel(TextModelBuilder.createTextModel(this));
		this.setBody(new Body());
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		this.getModel().setVao(TextModelBuilder.createTextModel(this).getVao());
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public int getNumberOfLines() {
		return numberOfLines;
	}

	public void setNumberOfLines(int numberOfLines) {
		this.numberOfLines = numberOfLines;
	}

	public float getMaxLineSize() {
		return maxLineSize;
	}

	public void setMaxLineSize(float maxLineSize) {
		this.maxLineSize = maxLineSize;
	}

	public boolean isCentered() {
		return centered;
	}

	public void setCentered(boolean centered) {
		this.centered = centered;
	}
	
	public void setFontParameters(Vector3f fontColor, float fontWidth, float fontEdgeWidth) {
		setFontColor(fontColor);
		setFontWidth(fontWidth);
		setFontEdgeWidth(fontEdgeWidth);
	}
	
	public float getFontSize() {
		return fontSize;
	}

	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
		this.getModel().setVao(TextModelBuilder.createTextModel(this).getVao());
	}
	
	public Vector3f getFontColor() {
		return fontColor;
	}

	public void setFontColor(Vector3f fontColor) {
		this.fontColor = fontColor;
	}

	public float getFontWidth() {
		return fontWidth;
	}

	public void setFontWidth(float fontWidth) {
		this.fontWidth = fontWidth;
	}

	public float getFontEdgeWidth() {
		return fontEdgeWidth;
	}

	public void setFontEdgeWidth(float fontEdgeWidth) {
		this.fontEdgeWidth = fontEdgeWidth;
	}
	
	public void setBorderParameters(Vector3f borderColor, float borderWidth, float borderEdgeWidth, Vector2f borderOffset) {
		setBorderColor(borderColor);
		setBorderWidth(borderWidth);
		setBorderEdgeWidth(borderEdgeWidth);
		setBorderOffset(borderOffset);
	}

	public Vector3f getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Vector3f borderColor) {
		this.borderColor = borderColor;
	}

	public float getBorderWidth() {
		return borderWidth;
	}

	public void setBorderWidth(float borderWidth) {
		this.borderWidth = borderWidth;
	}

	public float getBorderEdgeWidth() {
		return borderEdgeWidth;
	}

	public void setBorderEdgeWidth(float borderEdgeWidth) {
		this.borderEdgeWidth = borderEdgeWidth;
	}

	public Vector2f getBorderOffset() {
		return borderOffset;
	}

	public void setBorderOffset(Vector2f borderOffset) {
		this.borderOffset = borderOffset;
	}
	
	
}
