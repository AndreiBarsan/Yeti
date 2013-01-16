package barsan.opengl.rendering.lights;

import barsan.opengl.util.Color;

public abstract class Light {

	/**	Eases up light usage helping avoid a bunch of ugly instanceofs.
	 */
	public enum LightType {
		Directional,
		Point,
		Spot		
	}
	
	private Color diffuseColor;
	private Color specularColor;
	protected float constantAttenuation;
	protected float linearAttenuation;
	protected float quadraticAttenuation;
	protected float cubicAttenuation;
	
	public Light(Color diffuse, Color specular, float ka, float la, float qa, float ca) {
		this.setDiffuse(diffuse);
		this.setSpecular(specular);
		
		constantAttenuation = ka;
		linearAttenuation = la;
		quadraticAttenuation = qa;
		cubicAttenuation = ca;
	}
	
	public abstract LightType getType();

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

	public void setAttenuation(float ka, float la, float qa,
			float ca) {
				constantAttenuation = ka;
				linearAttenuation = la;
				quadraticAttenuation = qa;
				cubicAttenuation = ca;
			}

}
