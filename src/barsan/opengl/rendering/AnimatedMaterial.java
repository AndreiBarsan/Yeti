package barsan.opengl.rendering;

import barsan.opengl.rendering.materials.Material;
import barsan.opengl.resources.ResourceLoader;

public class AnimatedMaterial extends Material {

	public AnimatedMaterial() {
		super(ResourceLoader.shader("animatedPhong"));
	}

	
}