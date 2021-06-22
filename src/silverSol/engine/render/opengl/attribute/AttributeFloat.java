package silverSol.engine.render.opengl.attribute;

public class AttributeFloat extends Attribute {

	public AttributeFloat(String name, boolean instanced) {
		super(name, instanced);
		this.attributeBindLength = 1;
	}
	
}
