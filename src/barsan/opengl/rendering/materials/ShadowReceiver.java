package barsan.opengl.rendering.materials;

import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.RendererState;

/**
 * Shadow quality:
 * 	- 1: basic mapping and bias
 *  - 2: bias now depends on the fragments' normals
 *  - 3: multiple poisson sampling
 *  - 4: randomized poisson sampling
 *   
 * @author Andrei Bârsan
 */
public class ShadowReceiver implements MaterialComponent {
	
	/*
	 * Source of awful crashing found. It is unbound samplers that get accessed
	 * when one might think they shouldn't be accessed. The GPU doesn't *do*
	 * branch *prediction*. It just executes both branches of an if-statement and
	 * discards the wrong one afterwards. So when my shadow receiver component is
	 * missing, `useShadows` stays 0, but the shader still tries to sample **both**
	 * shadow maps - both the cube map for omnilights and the other one. And since
	 * nobody binds them, the shader blows up, before figuring out he shouldn' even
	 * have been around those samplers.
	 * 
	 * Possible fixes:
	 *  - dynamic shader code generation based on components used
	 *  - this bug doesn't seem to hurt other setups (bump-no bump/ texture-no texture etc.)
	 *    so maybe shadowReceiver could get hard-coupled to the material for the
	 *    time being, before a more advanced system such as the one above is needed
	 *    OR
	 *    maybe manually create shader handling NO shadow mapping?
	 */
	@Override
	public void setup(Material m, RendererState rs, Matrix4 modelMatrix) {
		// These settings are not material- or material-instance specific, but, 
		// rather, renderer (state) specific.
		rs.shadowMapBindings(m.shader, modelMatrix);
	}

	@Override
	public int setupTexture(Material m, RendererState rs, int slot) {
		return rs.shadowMapTextureBindings(m.shader, slot);
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
