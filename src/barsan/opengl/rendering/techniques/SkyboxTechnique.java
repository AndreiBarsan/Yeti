package barsan.opengl.rendering.techniques;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.resources.ResourceLoader;

public class SkyboxTechnique extends Technique {

	public SkyboxTechnique() {
		super(ResourceLoader.shader("cubeMap"));
	}

	@Override
	public void setup(RendererState rs) {
		view.set(rs.getCamera().getView());
		projection.set(rs.getCamera().getProjection());
		super.setup(rs);
	}
	
	@Override
	public void renderDude(ModelInstance mi, RendererState rs,
			Matrix4Stack matrixStack) {
		
		super.renderDude(mi, rs, matrixStack);
	}
	
	@Override
	public void loadMaterial(RendererState rs, Material material) {
		
		program.setU1i("diffuseMap", 10);
		program.setU1i("useGammaCorrection", false);
		
		rs.gl.glActiveTexture(GL.GL_TEXTURE0 + 10);
		rs.gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
		rs.gl.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, 
				material.getDiffuseMap().getTextureObject(rs.gl));
	}
}
