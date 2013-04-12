package barsan.opengl.scenes;

import barsan.opengl.rendering.RendererState;
import barsan.opengl.rendering.Technique;
import barsan.opengl.resources.ResourceLoader;

public class NullTechnique extends Technique {

	public NullTechnique() {
		super(ResourceLoader.shader("null"));
	}

	@Override
	public void setup(RendererState rs) {
		super.setup(rs);
		
		view.set(rs.getCamera().getView());
		projection.set(rs.getCamera().getProjection());
	}
}
