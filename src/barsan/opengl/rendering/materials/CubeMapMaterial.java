package barsan.opengl.rendering.materials;

import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.Model;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.GLHelp;

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
		
		rendererState.gl.glUseProgram(shader.getHandle());
		shader.setUMatrix4("mvpMatrix", mvp);
		shader.setU1i("samplingCube", true);
		shader.setU1i("cubeMap", 0);
		rendererState.gl.glActiveTexture(GLHelp.textureSlot[0]);
		// It contains the texture wrapped by the CubeTexture
		texture.bind(rendererState.gl);
		
		shader.setU1i("derpMap", 1);
		rendererState.gl.glActiveTexture(GLHelp.textureSlot[1]);
		//ResourceLoader.texture("stone").bind(rendererState.gl);
		
		super.setup(rendererState, transform);
	}
	
	@Override
	public void bindTextureCoodrinates(Model model) {
		// nop!
	}
}
