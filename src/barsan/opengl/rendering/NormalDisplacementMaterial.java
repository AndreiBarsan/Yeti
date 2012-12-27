package barsan.opengl.rendering;

import barsan.opengl.math.MathUtil;
import barsan.opengl.math.Matrix4;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;

public class NormalDisplacementMaterial extends Material {

	public NormalDisplacementMaterial(Color color) {
		super(ResourceLoader.shader("normalDisplacement"), color, Color.WHITE);
	}

	@Override
	public void setup(RendererState rendererState, Matrix4 transform) {
		Matrix4 view = rendererState.getCamera().getView();
		Matrix4 projection = rendererState.getCamera().getProjection();
		Matrix4 viewModel = new Matrix4(view).mul(transform);
		Matrix4 MVP = new Matrix4(projection).mul(view).mul(transform);
		
		enableShader(rendererState);
		
		shader.setUMatrix4("mvpMatrix", MVP);
		shader.setUMatrix4("mvMatrix", viewModel);
		shader.setUMatrix4("pMatrix", projection);
		shader.setUMatrix3("normalMatrix", MathUtil.getNormalTransform(viewModel));
		shader.setUVector4f("matColor", diffuse.getData());
		shader.setUVector3f("cameraPosition", rendererState.getCamera().getPosition());
		
		// Fog
		if(rendererState.getFog() != null) {
			Fog fog = rendererState.getFog();
			shader.setU1i("fogEnabled", 1);
			shader.setU1f("minFogDistance", fog.minDistance);
			shader.setU1f("maxFogDistance", fog.maxDistance);
			shader.setUVector4f("fogColor", fog.color.getData());
		} else {
			shader.setU1i("fogEnabled", 0);
		}
	}

}
