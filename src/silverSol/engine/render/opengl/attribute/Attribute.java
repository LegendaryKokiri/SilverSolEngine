package silverSol.engine.render.opengl.attribute;

public abstract class Attribute {

	protected int programID;
	private String name;
	
	protected int attributeBindLength;
	protected int attributeBindLocation;
	private boolean instanced;
	
	public Attribute(String attributeName, boolean instanced) {
		this.programID = -1;
		this.name = attributeName;
		this.instanced = instanced;
		this.attributeBindLocation = 0;
	}
	
	public void setProgramID(int programID) {
		this.programID = programID;
	}
	
	public int getProgramID() {
		return programID;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getBindLocation() {
		return attributeBindLocation;
	}

	public void setBindLocation(int attributeBindLocation) {
		this.attributeBindLocation = attributeBindLocation;
	}

	public int getAttributeBindLength() {
		return attributeBindLength;
	}

	public boolean isInstanced() {
		return instanced;
	}

	public void setInstanced(boolean instanced) {
		this.instanced = instanced;
	}
	
	@Override
	public String toString() {
		return "Attribute " + name + " at location " + attributeBindLocation;
	}
	
}
