package barsan.opengl.rendering.techniques;

import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.resources.ResourceLoader;

public class FlatTechnique extends Technique {

	/**
	 * Renders entities by transforming the vertices normally, but then only applying
	 * the material's flat, diffuse color to the output. One step above the null
	 * technique, which just writes to the depth buffer and outputs no color.
	 */
	public FlatTechnique() {
		super(ResourceLoader.shader("flat"));
	}
	
	@Override
	public void setup(RendererState rs) {
		super.setup(rs);
		
		view.set(rs.getCamera().getView());
		projection.set(rs.getCamera().getProjection());
	}
	
	@Override
	protected void instanceRenderSetup(ModelInstance mi, RendererState rs,
			Matrix4Stack matrixStack) {
		super.instanceRenderSetup(mi, rs, matrixStack);
		
		program.setUVector4f("matColor", mi.getMaterial().getDiffuse().getData());
	}

}
