package barsan.opengl.rendering;

import barsan.opengl.util.Color;

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
