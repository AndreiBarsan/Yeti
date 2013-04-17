package barsan.opengl.rendering.lights;

import barsan.opengl.math.Vector3;
import barsan.opengl.util.Color;

public class SpotLight extends PointLight {

	private float cosOuter, cosInner;
	private float exponent;
	private Vector3 direction;
	
	public SpotLight(Vector3 position, Vector3 direction, float inner, float outer,
			float exponent, Color diffuse, Color specular) {
		super(position, diffuse, specular);
		
		this.cosOuter = outer;
		this.cosInner = inner;
		this.exponent = exponent;
		this.direction = direction;
	}
	
	public SpotLight(Vector3 position, Vector3 direction, float inner, float outer, float exponent) {
		this(position, direction, inner, outer, exponent, Color.WHITE, Color.WHITE);
	}
	
	public float getCosOuter() {
		return cosOuter;
	}

	public void setCosOuter(float cosOuter) {
		this.cosOuter = cosOuter;
	}

	public float getCosInner() {
		return cosInner;
	}

	public void setCosInner(float cosInner) {
		this.cosInner = cosInner;
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
