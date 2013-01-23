package barsan.opengl.rendering;

import barsan.opengl.math.Vector3;

/**
 * @author Andrei Barsan
 */
public class PerspectiveCamera extends Camera {
	
	static final float DEFAULT_FOV = 	45.0f;
	
	float FOV; /* Degrees */

	public PerspectiveCamera(int width, int height) {
		this(new Vector3(-4, 2, 0), new Vector3(1, -0.5f, 0).normalize(), width, height);
	}
	
	public PerspectiveCamera(Vector3 position, Vector3 direction, int width, int height) {
		super(position, direction, width, height);
		
		FOV = DEFAULT_FOV;
	}
	
	public void setFOV(float FOV) {
		this.FOV = FOV;
		projectionDirty = true;
	}
	
	public float getFOV(float FOV) {
		return FOV;
	}
	
	@Override
	protected void refreshProjection() {
		assert height != 0;
		assert FOV > 5;
		assert frustumNear < frustumFar;
		
		float aspect = (float)width / height;
		
		projection.setPerspectiveProjection(FOV, aspect, frustumNear, frustumFar);	
	}
}
