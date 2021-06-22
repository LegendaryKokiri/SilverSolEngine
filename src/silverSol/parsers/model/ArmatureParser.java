package silverSol.parsers.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import silverSol.engine.render.armature.Bone;

public class ArmatureParser {

	public static Bone parseArmature(String boneNameLine, String boneIndexLine) throws IOException {
		Bone armature = null;
		List<String> boneOrder = new ArrayList<>();
		
		//Start the loop from 2 to avoid the string "Bone Name: "
		String[] boneNames = boneNameLine.split(" ");
		for(int i = 2; i < boneNames.length; i++) {
			Bone bone = new Bone();
			bone.setName(boneNames[i].split("\\|")[0]);
			boneOrder.add(bone.getName());
			
			if(armature == null) {
				armature = bone;
				bone.setRoot(true);
			} else {
				//Build bone hierarchy
				String childName = boneNames[i].split("\\|")[1]; 
				Bone parentBone = armature.getBone(childName);
				parentBone.addChild(bone);
			}
		}
		
		//Start the loop from 2 to avoid the string "Bone Indices: "
		String[] boneIndices = boneIndexLine.split(" ");
		for(int i = 2; i < boneIndices.length; i++) {
			armature.getBone(boneOrder.get(i - 2)).setIndex(Integer.parseInt(boneIndices[i]));
		}
	    
	    return armature;
	}
	
}
