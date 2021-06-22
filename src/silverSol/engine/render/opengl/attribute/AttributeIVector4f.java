package silverSol.engine.render.opengl.attribute;

public class AttributeIVector4f extends Attribute {

	public AttributeIVector4f(String name, boolean instanced) {
		super(name, instanced);
		this.attributeBindLength = 1;
	}
	
}
