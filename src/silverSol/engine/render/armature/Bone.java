package silverSol.engine.render.armature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Matrix4f;

import silverSol.engine.render.animation.model.ModelAnimation;

public class Bone {
	
	//Bone identification
	private String name;
	private int index;
	
	//Bone hierarchy
	private boolean isRoot;
	private List<Bone> children;
		
	//ANIMATION COMPONENT
	private boolean hasAnimation;
		private List<ModelAnimation> animations;
		private Map<String, Integer> animationIndices;
	
	//In bone space--The offset of the bone from its parent bone when all bones in are in rest position
	private Matrix4f localBindTransformation;
	
	//In model space--The offset of the bone from the model's origin when all bones in are in rest position
	private Matrix4f modelBindTransformation;
	
	//In model space (note the change from bone space)--The inverse of the local bind transformation
	private Matrix4f inverseBindTransformation;
	
	public Bone() {
		this.name = "";
		this.index = -1;
		
		this.isRoot = false;
		this.children = new ArrayList<>();
		
		this.hasAnimation = false;
			this.animations = new ArrayList<>();
			this.animationIndices = new HashMap<>();
		
		this.localBindTransformation = new Matrix4f();
		this.modelBindTransformation = new Matrix4f();
		this.inverseBindTransformation = new Matrix4f();
		
		this.localBindTransformation = new Matrix4f();
	}
	
	public int getIndex() {
		return index;
	}
	
	public int getIndex(String name) {
		int index = -1;
		
		for(Bone child : children) {
			index = child.getIndex(name);			
			if(index != -1) return index;
		}
		
		return (name.equals(this.name)) ? this.index : -1;
	}
	
	public Bone getBone(String name) {
		if(name.equals(this.name)) return this;
		
		for(Bone child : children) {
			if(child.getName().equals(name)) return child;
			Bone grandchildBone;
			if((grandchildBone = child.getBone(name)) != null) return grandchildBone;
		}
		
		return null;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isRoot() {
		return isRoot;
	}

	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	public void addChild(Bone child) {
		children.add(child);
	}
	
	public List<Bone> getChildren() {
		return children;
	}
	
	public boolean hasAnimation() {
		return hasAnimation;
	}
	
	public void addAnimations(List<ModelAnimation> animations) {
		if(animations == null || animations.size() == 0) return;
		
		hasAnimation = true;
		this.animations = animations;
		
		for(int i = 0; i < animations.size(); i++) {
			animationIndices.put(animations.get(i).getName(), i);
		}
	}
	
	public List<ModelAnimation> getAnimations() {
		return animations;
	}
	
	public int getAnimationIndex(String name) {
		return animationIndices.get(name);
	}
	
	public void calculateInverseBindTransformation(Matrix4f parentBindTransformation) {
		//Convert the bone-space transform to a model-space transform before inversion
		Matrix4f.mul(parentBindTransformation, localBindTransformation, modelBindTransformation);
		
		Matrix4f.invert(modelBindTransformation, inverseBindTransformation);
		
		for(Bone child : children) {
			child.calculateInverseBindTransformation(modelBindTransformation);
		}
	}
	
	public void setLocalBindTransformation(Matrix4f localBindTransformation) {
		this.localBindTransformation = localBindTransformation;
	}
	
	public Matrix4f getLocalBindTransformation() {
		return localBindTransformation;
	}
	
	public Matrix4f getModelBindTransformation() {
		return modelBindTransformation;
	}
	
	public Matrix4f getInverseBindTransformation() {
		return inverseBindTransformation;
	}
	
	public String toString() {
		return "Bone " + name + " (Index " + index + ")";
	}
}
