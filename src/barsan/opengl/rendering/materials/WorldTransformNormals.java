package barsan.opengl.rendering.materials;

import barsan.opengl.math.MathUtil;
import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.RendererState;

/**
 * Performs the traditional world transform, while also providing a normalMatrix
 * to tranform the object's normals.
 * 
 * @author Andrei Bârsan
 */
public class WorldTransformNormals extends WorldTransform {
	
	@Override
	public void setup(Material m, RendererState rs, Matrix4 modelMatrix) {
		super.setup(m, rs, modelMatrix);
		m.shader.setUMatrix3("normalMatrix", MathUtil.getNormalTransform(viewModel));
	}

}
