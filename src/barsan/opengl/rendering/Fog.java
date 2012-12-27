package barsan.opengl.rendering;

import barsan.opengl.util.Color;

public class Fog {	
	public float minDistance;
	public float maxDistance;
	public Color color;
	
	// NYI (a certain interpolation function)
	public int interpolation;

	public Fog(float minDistance, float maxDistance, Color color) {
		this.minDistance = minDistance;
		this.maxDistance = maxDistance;
		this.color = color;
	}	
}
