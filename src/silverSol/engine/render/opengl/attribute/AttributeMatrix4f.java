package silverSol.engine.render.opengl.attribute;

public class AttributeMatrix4f extends Attribute {

	public AttributeMatrix4f(String name, boolean instanced) {
		super(name, instanced);
		this.attributeBindLength = 4;
	}
	
}
