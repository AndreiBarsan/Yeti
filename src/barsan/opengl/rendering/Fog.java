package barsan.opengl.rendering;

import barsan.opengl.rendering.cameras.Camera;
import barsan.opengl.util.Color;

public class Fog {	
	private static final float FADE_DISTANCE = 10.0f;
	
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
	
	public Fog(Color color) {
		this.minDistance = 0.0f;
		this.maxDistance = 0.0f;
		this.color = color;
	}	

	
	public void fadeCamera(Camera camera) {
		minDistance = camera.getFrustumFar() - FADE_DISTANCE;
		maxDistance = camera.getFrustumFar();
	}
}
