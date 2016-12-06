package barsan.opengl.util;

import com.jogamp.opengl.GL2;

import barsan.opengl.rendering.cameras.Camera;

public class FPCameraAdapter {

	private Camera camera;
	
	public FPCameraAdapter(Camera camera) {
		this.camera = camera;
	}
	
	public void setProjection(GL2 gl) {
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadMatrixf(camera.getProjection().getData(), 0);
	}
	
	public void setView(GL2 gl) {
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadMatrixf(camera.getView().getData(), 0);
		gl.glPushMatrix();
	}
	
	public void prepare(GL2 gl) {
		setProjection(gl);
		setView(gl);
	}
}
