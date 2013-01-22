package barsan.opengl.rendering.materials;

import barsan.opengl.math.Matrix3;
import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.RendererState;

/**
 * Transforms the object's vertices into the world space, then into the camera
 * space and applies a projection afterwards.
 * 
 * @author Andrei Bârsan
 */
public class WorldTransform implements MaterialComponent {

	protected static Matrix4 view = new Matrix4();
	protected static Matrix4 projection = new Matrix4();
	protected static Matrix4 viewModel = new Matrix4();
	protected static Matrix4 MVP = new Matrix4();
	
	@Override
	public void setup(Material m, RendererState rs, Matrix4 modelMatrix) {
		view.set(rs.getCamera().getView());
		projection.set(rs.getCamera().getProjection());
		viewModel.set(view).mul(modelMatrix);
		
		// WARNING: A * B * C != A * (B * C) with matrices
		// The following line does not equal projection * viewModel
		MVP.set(projection).mul(view).mul(modelMatrix);
		
		m.shader.setUMatrix4("mvpMatrix", MVP);
		m.shader.setUMatrix4("mvMatrix", viewModel);
		m.shader.setUMatrix4("vMatrix", view);
		m.shader.setUMatrix3("vMatrix3x3", new Matrix3(view));
	}

	@Override
	public int setupTexture(Material m, RendererState rs, int slot) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void cleanUp(Material m, RendererState rs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
