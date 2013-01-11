package barsan.opengl.rendering;

import javax.media.opengl.GL2;

import barsan.opengl.math.MathUtil;
import barsan.opengl.math.Matrix3;
import barsan.opengl.math.Matrix4;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.GLHelp;

public class CubicEnvMappingMaterial extends Material {

	CubeTexture texture;
	
	public CubicEnvMappingMaterial(CubeTexture texture) {
		super(ResourceLoader.shader("cubicEnvMapping"));
		this.texture = texture;
	}

	@Override
	public void setup(RendererState rendererState, Matrix4 transform) {
		GL2 gl = rendererState.getGl();
		
		enableShader(rendererState);
		
		gl.glActiveTexture(GLHelp.textureSlot[0]);
		shader.setU1i("samplerCube", 0);
		texture.bind(gl);
			
		MVP.set(projection).mul(view).mul(transform);
		viewModel.set(view).mul(transform);
		
		Camera cam = rendererState.getCamera();
		Matrix4 cRot = cam.getRotation().toMatrix4().inv();
		
		shader.setUMatrix4("mvpMatrix", MVP);
		shader.setUMatrix4("mvMatrix", viewModel);
		shader.setUMatrix4("mInverseCameraRot", cRot);
		shader.setUMatrix3("normalMatrix", MathUtil.getNormalTransform(viewModel));
		shader.setUMatrix3("mvMatrix3x3", new Matrix3(viewModel));
		shader.setUVector3f("cameraPosition", cam.getPosition());
	}
	
	@Override
	public void bindTextureCoodrinates(Model model) {
		// nop!
	}

	@Override
	public void unsetBuffers(Model model) {
		super.unsetBuffers(model);
		
		model.getVertices().cleanUp(getPositionIndex());
		model.getNormals().cleanUp(getNormalIndex());
	}
}
