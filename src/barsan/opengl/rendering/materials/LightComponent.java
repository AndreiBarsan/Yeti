package barsan.opengl.rendering.materials;

import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.rendering.lights.AmbientLight;
import barsan.opengl.rendering.lights.DirectionalLight;
import barsan.opengl.rendering.lights.Light;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.lights.SpotLight;
import barsan.opengl.rendering.lights.Light.LightType;
import barsan.opengl.util.Color;

public class LightComponent implements MaterialComponent {

	@Override
	public void setup(Material m, RendererState rs, Matrix4 modelMatrix) {
		// TODO: implement ARRAYS OF LIGHTS here!
		Light light = rs.getLights().get(0);
		AmbientLight ambientLight = rs.getAmbientLight();
		
		if(light.getType() == LightType.Point || light.getType() == LightType.Spot) {
			Vector3 lp = ((PointLight)light).getPosition();
			float[] buff4f = new float[] { lp.x, lp.y, lp.z, 1.0f };
			m.shader.setUVector4f("lightPosition", buff4f);
		} else {
			// Directional light
			Vector3 lp = ((DirectionalLight)light).getDirection();
			float[] buff4f = new float[] { lp.x, lp.y, lp.z, 0.0f };
			m.shader.setUVector4f("lightPosition", buff4f);
		}
		
		m.shader.setUVector4f("globalAmbient", ambientLight.getColor().getData());
		m.shader.setUVector4f("lightDiffuse", light.getDiffuse().getData());
		m.shader.setUVector4f("lightSpecular", light.getSpecular().getData());
		
		m.shader.setU1f("constantAt", light.getConstantAttenuation());
		m.shader.setU1f("linearAt", light.getLinearAttenuation());
		m.shader.setU1f("quadraticAt", light.getQuadraticAttenuation());
		m.shader.setU1f("cubicAt", light.getCubicAttenuation());
		
		// This isn't very clean - TODO: delegate this to special components
		if(light.getType() == LightType.Spot) {
			SpotLight sl = (SpotLight)light;
			m.shader.setU1f("lightTheta", sl.getTheta());
			m.shader.setU1f("lightPhi", sl.getPhi());
			m.shader.setU1f("lightExponent", sl.getExponent());
			m.shader.setUVector3f("spotDirection", sl.getDirection());		
		} else {
			m.shader.setU1f("lightTheta", 0.0f);
			m.shader.setU1f("lightPhi", 0.0f);
			m.shader.setU1f("lightExponent", 1.0f);
			m.shader.setUVector3f("spotDirection", Vector3.ZERO);
		}
	}

	@Override
	public int setupTexture(Material m, RendererState rs, int slot) {
		return 0;
	}

	@Override
	public void cleanUp(Material m, RendererState rs) {
		m.shader.setUVector4f("globalAmbient", Color.TRANSPARENTBLACK.getData());
		m.shader.setUVector4f("lightDiffuse", Color.TRANSPARENTBLACK.getData());
		m.shader.setUVector4f("lightSpecular", Color.TRANSPARENTBLACK.getData());
	}

	@Override
	public void dispose() {	}

}
