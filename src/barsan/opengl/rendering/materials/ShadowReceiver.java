package barsan.opengl.rendering.materials;

import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.RendererState;

/**
 * 
 * Shadow quality:
 * 	- 1: basic mapping and bias
 *  - 2: bias now depends on the fragments' normals
 *  - 3: multiple poisson sampling
 *  - 4: randomized poisson sampling
 *   
 * @author Andrei Bârsan
 */
public class ShadowReceiver implements MaterialComponent {
	
	@Override
	public void setup(Material m, RendererState rs, Matrix4 modelMatrix) {
		// These settings are not material- or material-instance specific, but, 
		// rather, renderer (state) specific.
		rs.shadowMapBindings(m, modelMatrix);
	}

	@Override
	public int setupTexture(Material m, RendererState rs, int slot) {
		return rs.shadowMapTextureBindings(m, slot);
	}

	@Override
	public void cleanUp(Material m, RendererState rs) {
		m.shader.setU1i("useShadows", false);
		m.shader.setU1i("samplingCube", false);
		m.shader.setU1i("shadowQuality", 0);
	}

	@Override
	public void dispose() {
		
	}

}
