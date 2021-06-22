package silverSol.engine.render.opengl.attribute;

public class AttributeVector4f extends Attribute {

	public AttributeVector4f(String name, boolean instanced) {
		super(name, instanced);
		this.attributeBindLength = 1;
	}
	
}
