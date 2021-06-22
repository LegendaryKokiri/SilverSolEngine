package silverSol.engine.render.opengl.attribute;

public class AttributeVector3f extends Attribute {

	public AttributeVector3f(String name, boolean instanced) {
		super(name, instanced);
		this.attributeBindLength = 1;
	}
	
}
