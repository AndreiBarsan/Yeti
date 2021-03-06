package barsan.opengl.rendering.lights;

import barsan.opengl.util.Color;

// TODO: get rid of this and just classically use ambient components in lights
public class AmbientLight {
	
	private Color color;

	/**
	 * Creates an ambient light from the color, using the color's alpha value
	 * as its intensity.
	 * @param color
	 */
	public AmbientLight(Color color) {
		this.setColor(color);
	}
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
}
