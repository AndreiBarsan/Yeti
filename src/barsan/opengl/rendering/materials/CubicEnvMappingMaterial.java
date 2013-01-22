package barsan.opengl.rendering.materials;

import javax.media.opengl.GL2;

import barsan.opengl.Yeti;
import barsan.opengl.math.MathUtil;
import barsan.opengl.math.Matrix3;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Quaternion;
import barsan.opengl.rendering.Camera;
import barsan.opengl.rendering.CubeTexture;
import barsan.opengl.rendering.Model;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.GLHelp;

import com.jogamp.opengl.util.texture.Texture;

public class CubicEnvMappingMaterial extends Material {

	CubeTexture ctex;

	public CubicEnvMappingMaterial(CubeTexture texture, Texture tex2d) {
		super(ResourceLoader.shader("cubicEnvMapping"));
		this.ctex = texture;
		//setTexture(tex2d);
	}

	
	protected static Matrix4 view = new Matrix4();
	protected static Matrix4 projection = new Matrix4();
	protected static Matrix4 viewModel = new Matrix4();
	protected static Matrix4 MVP = new Matrix4();
	
	@Override
	public void setup(RendererState rendererState, Matrix4 transform) {
		GL2 gl = Yeti.get().gl.getGL2();
		
		enableShader(rendererState);
		
		gl.glActiveTexture(GLHelp.textureSlot[0]);
		shader.setU1i("cubeMap", 0);
		ctex.bind(gl);
		
		/*
		gl.glActiveTexture(GLHelp.textureSlot[1]);
		shader.setU1i("colorMap", 1);
		texture.bind(gl);
		//*/
		Camera cam = rendererState.getCamera();
		projection.set(rendererState.getCamera().getProjection());
		view.set(cam.getView());
		MVP.set(projection).mul(view).mul(transform);
		viewModel.set(view).mul(transform);
		
		shader.setUMatrix4("mvpMatrix", MVP);
		shader.setUMatrix4("mvMatrix", viewModel);
		shader.setUMatrix3("normalMatrix", MathUtil.getNormalTransform(viewModel));
	}
	
	@Override
	public void cleanUp(RendererState rendererState) { }
	
	@Override
	public void bindTextureCoodrinates(Model model) {
		// nop!
	}
}
