package barsan.opengl.rendering.materials;

import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.Renderer;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.resources.ResourceLoader;

public class DepthWriterDirectional extends Material {

	public DepthWriterDirectional() {
		super(ResourceLoader.shader("depthWriter"));
		
		// Do not expect normals
		ignoreLights = true;
	}

	@Override
	public void setup(RendererState rendererState, Matrix4 transform) {
		enableShader(rendererState);
		
		// Note: the camera is temporarily set to an orthographic projection
		// of the directional light
		view.set(rendererState.getCamera().getView());
		projection.set(rendererState.getCamera().getProjection());
		//viewModel.set(view).mul(transform);
		MVP.set(projection).mul(view).mul(transform);
		
		shader.setUMatrix4("mvpMatrix", MVP);
	}
	
	@Override
	public void cleanUp(RendererState rendererState) { 	}

	
}
