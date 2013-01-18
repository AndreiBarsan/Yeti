package barsan.opengl.rendering;

import barsan.opengl.math.Vector3;

public class OrthographicCamera extends Camera {

	public OrthographicCamera(int width, int height) {
		super(new Vector3(), new Vector3(), new Vector3(0.0f, 1.0f, 0.0f), width, height);
		
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void refreshProjection() {
		projection.setOrthogonalProjection(
			-width / 2, -height / 2, width, height, frustumNear, frustumFar);
		
	}

}
