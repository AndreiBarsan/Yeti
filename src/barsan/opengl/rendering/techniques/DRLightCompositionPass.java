package barsan.opengl.rendering.techniques;

import barsan.opengl.rendering.RendererState;
import barsan.opengl.resources.ResourceLoader;

public class DRLightCompositionPass extends Technique {

	public DRLightCompositionPass() {
		super(ResourceLoader.shader("DRLightCompose"));
	}
	
	@Override
	public void setup(RendererState rs) {
		super.setup(rs);
		
		/* No need for instanceRenderSetup, we are only rendering 1 quad/frame */
		program.setU1i("diffuseMap", 0);
		program.setU1i("lightMap", 1);
	}	
}
