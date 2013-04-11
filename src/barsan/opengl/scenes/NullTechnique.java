package barsan.opengl.scenes;

import barsan.opengl.rendering.Technique;
import barsan.opengl.resources.ResourceLoader;

public class NullTechnique extends Technique {

	public NullTechnique() {
		super(ResourceLoader.shader("depthWriter"));
	}

}
