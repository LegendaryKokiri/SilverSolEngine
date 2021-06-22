package silverSol.engine.render.opengl.attribute;

public class AttributeIVector3f extends Attribute {

	public AttributeIVector3f(String name, boolean instanced) {
		super(name, instanced);
		this.attributeBindLength = 1;
	}
	
}
