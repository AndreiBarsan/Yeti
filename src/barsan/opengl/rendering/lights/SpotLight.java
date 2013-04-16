package barsan.opengl.rendering.lights;

import barsan.opengl.math.Vector3;
import barsan.opengl.util.Color;

public class SpotLight extends PointLight {

	private float outer, inner;
	private float exponent;
	private Vector3 direction;
	
	public SpotLight(Vector3 position, Vector3 direction, float outer, float inner,
			float exponent, Color diffuse, Color specular) {
		super(position, diffuse, specular);
		
		this.outer = outer;
		this.inner = inner;
		this.exponent = exponent;
		this.direction = direction;
	}
	
	public SpotLight(Vector3 position, Vector3 direction, float outer, float inner, float exponent) {
		this(position, direction, outer, inner, exponent, Color.WHITE, Color.WHITE);
	}
	
	public SpotLight(Vector3 position, Vector3 direction) {
		this(position, direction, 1.0f, 1.0f, 2.0f);
	}

	public float getOuter() {
		return outer;
	}

	public void setOuter(float outer) {
		this.outer = outer;
	}

	public float getInner() {
		return inner;
	}

	public void setInner(float inner) {
		this.inner = inner;
	}

	public Vector3 getDirection() {
		return direction;
	}

	public void setDirection(Vector3 direction) {
		this.direction = direction;
	}

	public float getExponent() {
		return exponent;
	}

	public void setExponent(float exponent) {
		this.exponent = exponent;
	}
	
	@Override
	public LightType getType() {
		return LightType.Spot;
	}
}
