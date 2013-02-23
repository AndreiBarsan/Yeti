package barsan.opengl.rendering;

import barsan.opengl.math.Vector3;

public class OrthographicCamera extends Camera {

	public OrthographicCamera(int width, int height, float near, float far) {
		super(new Vector3(), new Vector3(), new Vector3(0.0f, 1.0f, 0.0f), width, height, near, far);
	}
	
	public OrthographicCamera(int width, int height) {
		this(width, height, -100.0f, 100.0f);
	}
	
	@Override
	public void refreshProjection() {
		projection.setOrthogonalProjection(
			-width / 2, -height / 2, width, height, frustumNear, frustumFar);
	}

}
