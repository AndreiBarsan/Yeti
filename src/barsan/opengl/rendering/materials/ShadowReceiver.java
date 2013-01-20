package barsan.opengl.rendering.materials;

import javax.media.opengl.GL2;

import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.Renderer;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.util.GLHelp;

public class ShadowReceiver implements MaterialComponent {
	
	@Override
	public void setup(Material m, RendererState rs, Matrix4 modelMatrix) {
		Matrix4 projection = rs.depthProjection;
		Matrix4 view = rs.depthView;
		
		Matrix4 MVP = new Matrix4(projection).mul(view).mul(modelMatrix);
		
		// Really important! Converts the z-values from [-1, 1] to [0, 1]
		Matrix4 biasMVP = new Matrix4(Renderer.shadowBiasMatrix).mul(MVP);
		
		m.shader.setUMatrix4("mvpMatrixShadows", biasMVP);
		m.shader.setU1i("useShadows", true);
		m.shader.setU1i("shadowQuality", 3);
	}

	@Override
	public int setupTexture(Material m, RendererState rs, int slot) {
		m.shader.setU1i("shadowMap", slot);
		rs.gl.glActiveTexture(GLHelp.textureSlot[slot]);
		rs.gl.glBindTexture(GL2.GL_TEXTURE_2D, rs.shadowTexture);
		
		return 1;
	}

	@Override
	public void cleanup() {
		
	}

	@Override
	public void dispose() {
		
	}

}
