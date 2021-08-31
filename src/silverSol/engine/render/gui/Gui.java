package silverSol.engine.render.gui;

import org.lwjgl.opengl.GL15;

import silverSol.engine.entity.Entity;
import silverSol.engine.render.model.Model;
import silverSol.engine.render.model.models.Quad3d;

public class Gui extends Entity {
	
	private boolean threeDimensional;
	
	public Gui(Model.VertexType vertexType, boolean threeDimensional, float width, float height) {
		super(new Quad3d(vertexType, false, false, 2f, 2f, GL15.GL_STREAM_DRAW));
		
		if(threeDimensional) {
			this.setBody(new silverSol.engine.physics.d3.body.Body());
			body3d.setScale(width, height, 1f);
			body3d.updateTransformation();
		} else {
			this.setBody(new silverSol.engine.physics.d2.body.Body());
			body2d.setScale(width, height);
		}
	}
	
	public Gui(boolean threeDimensional, Model model) {
		super(model);
		
		if(threeDimensional) this.setBody(new silverSol.engine.physics.d3.body.Body());
		else this.setBody(new silverSol.engine.physics.d2.body.Body());
	}

	public boolean isThreeDimensional() {
		return threeDimensional;
	}

	public void setThreeDimensional(boolean threeDimensional) {
		this.threeDimensional = threeDimensional;
	}
	
}
