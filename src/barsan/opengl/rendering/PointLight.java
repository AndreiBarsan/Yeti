package barsan.opengl.rendering;

import barsan.opengl.math.Vector3;
import barsan.opengl.util.Color;

public class PointLight {

	private Vector3 position;
	private Color diffuseColor;
	private Color specularColor;
	
	private float 	constantAttenuation,
					linearAttenuation,
					quadraticAttenuation,
					cubicAttenuation;
	
	public PointLight(Vector3 position) {
		this(position, Color.WHITE, Color.WHITE);
	}
	
	public PointLight(Vector3 position, Color diffuse) {
		this(position, diffuse, Color.WHITE);
	}
	
	public PointLight(Vector3 position, Color diffuse, Color specular) {
		this(position, diffuse, specular, 0.0f, 0.0f, 0.0f, 0.0f);
	}
	
	public PointLight(Vector3 position, Color diffuse, Color specular,
			float ka, float la, float qa, float ca) {
		this.setPosition(position);
		this.setDiffuse(diffuse);
		this.setSpecular(specular);
		
		constantAttenuation = ka;
		linearAttenuation = la;
		quadraticAttenuation = qa;
		cubicAttenuation = ca;
	}
	
	public Vector3 getPosition() {
		return position;
	}

	public void setPosition(Vector3 position) {
		this.position = position;
	}

	public Color getDiffuse() {
		return diffuseColor;
	}

	public void setDiffuse(Color color) {
		this.diffuseColor = color;
	}

	public Color getSpecular() {
		return specularColor;
	}

	public void setSpecular(Color specularColor) {
		this.specularColor = specularColor;
	}

	public float getConstantAttenuation() {
		return constantAttenuation;
	}

	public void setConstantAttenuation(float constantAttenuation) {
		this.constantAttenuation = constantAttenuation;
	}

	public float getLinearAttenuation() {
		return linearAttenuation;
	}

	public void setLinearAttenuation(float linearAttenuation) {
		this.linearAttenuation = linearAttenuation;
	}

	public float getQuadraticAttenuation() {
		return quadraticAttenuation;
	}

	public void setQuadraticAttenuation(float quadraticAttenuation) {
		this.quadraticAttenuation = quadraticAttenuation;
	}

	public float getCubicAttenuation() {
		return cubicAttenuation;
	}

	public void setCubicAttenuation(float cubicAttenuation) {
		this.cubicAttenuation = cubicAttenuation;
	}

	
}
