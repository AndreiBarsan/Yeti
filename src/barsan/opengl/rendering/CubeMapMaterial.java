package barsan.opengl.rendering;

import barsan.opengl.math.Matrix4;
import barsan.opengl.resources.ResourceLoader;

public class CubeMapMaterial extends Material {

	static final String SHADER_NAME = "cubeMap";
	
	public CubeMapMaterial() {
		super(ResourceLoader.shader(SHADER_NAME));
		
		ignoreLights = true;
	}

	@Override
	public void setup(RendererState rendererState, Matrix4 transform) {
		Matrix4 projection = rendererState.getCamera().getProjection();
		Matrix4 view = rendererState.getCamera().getView();
		Matrix4 viewModel = view.mul(transform);
		// Ignoring transform for the moment
		// proj x view x model
		Matrix4 mvp = new Matrix4(projection).mul(viewModel);
		
		rendererState.getGl().glUseProgram(shader.getHandle());
		shader.setUMatrix4("mvpMatrix", mvp);
		shader.setU1f("cubeMap", 0);
		
		// It contains the texture wrapped by the CubeTexture
		texture.bind(rendererState.getGl());
	}
	
	@Override
	public void bindTextureCoodrinates(Model model) {
		// nop?
		// FIXME: use dynbinding to solve all material peculiarites !!!
	}
	
	@Override
	public void unsetBuffers(Model model) {
		model.getVertices().cleanUp(getPositionIndex());
	}
}
