package barsan.opengl.rendering;

import barsan.opengl.math.Vector3;
import barsan.opengl.util.Color;

public class PointLight {

	private Vector3 position;
	private Color diffuseColor;
	private Color specularColor;
	
	public PointLight(Vector3 position) {
		this(position, Color.WHITE, Color.WHITE);
	}
	
	public PointLight(Vector3 position, Color diffuse) {
		this(position, diffuse, Color.WHITE);
	}
	
	public PointLight(Vector3 position, Color diffuse, Color specular) {
		this.setPosition(position);
		this.setDiffuse(diffuse);
		this.setSpecular(specular);
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

	
}
