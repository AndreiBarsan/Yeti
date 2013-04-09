package barsan.opengl.rendering.lights;

import barsan.opengl.math.Vector3;
import barsan.opengl.util.Color;

/**
 * @author Andrei Bârsan
 *
 */
public class DirectionalLight extends Light {

	private Vector3 direction;
	
	public DirectionalLight(Vector3 direction) {
		this(direction, Color.WHITE.copy());
	}
	
	public DirectionalLight(Vector3 direction, Color diffuse) {
		this(direction, diffuse, Color.WHITE.copy());
	}
	
	public DirectionalLight(Vector3 direction, Color diffuse, Color specular) {
		super(diffuse, specular, 0.0f, 0.0f, 0.0f, 0.0f);
		this.direction = direction;
	}
	
	public Vector3 getDirection() {
		return direction;
	}
	
	public void setDirection(Vector3 direction) {
		this.direction = direction;
	}
	
	@Override
	public float getBoundingRadius() {
		return Float.POSITIVE_INFINITY;
	}

	@Override
	public LightType getType() {
		return LightType.Directional;
	}
	
}
