package barsan.opengl.rendering.techniques;

import barsan.opengl.rendering.RendererState;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.resources.ResourceLoader;

public class NullTechnique extends Technique {

	public NullTechnique() {
		super(ResourceLoader.shader("null"));
	}

	@Override
	public void loadMaterial(Material material) {
		// nop, ignores all material properties
	}
	
	@Override
	public void setup(RendererState rs) {
		super.setup(rs);
		
		view.set(rs.getCamera().getView());
		projection.set(rs.getCamera().getProjection());
	}
}
