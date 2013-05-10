package barsan.opengl.rendering.techniques;

import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.CubeSMAux;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.rendering.Shader;
import barsan.opengl.resources.ResourceLoader;

public class PointLightSM extends Technique {

	float far = 350.0f;
	private Vector3 lightPosition;
	
	public PointLightSM() {
		super(ResourceLoader.shader("depthWriterPoint"));
	}
	
	public void setLightPosition(Vector3 lightPosition) {
		this.lightPosition = lightPosition;
	}

	@Override
	public void setup(RendererState rs) {
		super.setup(rs);
		
		CubeSMAux.setup(lightPosition, projection);
		
		program.setUMatrix4a("vpMatrices", CubeSMAux.pViewMatrices);
		program.setUVector4f("lightPos_wc", new float[] 
				{ lightPosition.x, lightPosition.y, lightPosition.z, 1.0f });
		program.setU1f("far", rs.getOmniShadowFar());
	}
}
