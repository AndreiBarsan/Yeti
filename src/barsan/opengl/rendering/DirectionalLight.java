package barsan.opengl.rendering;

import barsan.opengl.math.Vector3;
import barsan.opengl.util.Color;

/**
 * Not yet implemented !!!
 * 
 * @author SiegeDog
 *
 */
public class DirectionalLight {

	private Vector3 direction;
	private Color color;
	
	public DirectionalLight(Vector3 direction, Color color) {
		this.direction = direction;
		this.color = color;
	}
	
	public Vector3 getDirection() {
		return direction;
	}
	
	public void setDirection(Vector3 direction) {
		this.direction = direction;
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}	
}
