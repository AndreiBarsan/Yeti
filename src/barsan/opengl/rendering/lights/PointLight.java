package barsan.opengl.rendering.lights;

import barsan.opengl.math.Vector3;
import barsan.opengl.util.Color;

/**
 * Basic point/directional light support with custom diffuse and specular colors,
 * as well as constant, linear, quadratic and cubic attenuation.
 *  
 * @author Andrei Bârsan
 */
public class PointLight extends Light {

	private Vector3 position;
	
	public PointLight(Vector3 position) {
		this(position, Color.WHITE, Color.WHITE);
	}
	
	public PointLight(Vector3 position, Color diffuse) {
		this(position, diffuse, Color.WHITE);
	}
	
	public PointLight(Vector3 position, Color diffuse, Color specular) {
		this(position, diffuse, specular, 0.00f, 0.00f, 0.10f);
	}
	
	public PointLight(Vector3 position, Color diffuse, Color specular,
			float ka, float la, float qa) {
		super(diffuse, specular, ka, la, qa);
		this.setPosition(position);
	}
	
	@Override
	public float getBoundingRadius() {
		Color d = getDiffuse();
		float maxChannel = Math.max(d.r, Math.max(d.g, d.b));
		float c = maxChannel * d.a;
		return 12.0f * (float)Math.sqrt(c) + 1.0f;
	}
	
	public Vector3 getPosition() {
		return position;
	}

	public void setPosition(Vector3 position) {
		this.position = position;
	}
	
	@Override
	public LightType getType() {
		return LightType.Point;
	}
	
}
