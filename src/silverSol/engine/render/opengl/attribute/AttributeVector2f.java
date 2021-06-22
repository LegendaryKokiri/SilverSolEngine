package silverSol.engine.render.opengl.attribute;

public class AttributeVector2f extends Attribute {

	public AttributeVector2f(String name, boolean instanced) {
		super(name, instanced);
		this.attributeBindLength = 1;
	}
	
}
