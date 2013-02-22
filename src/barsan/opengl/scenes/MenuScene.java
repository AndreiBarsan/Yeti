package barsan.opengl.scenes;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.input.CameraInput;
import barsan.opengl.rendering.Scene;
import barsan.opengl.util.TextHelper;

public class MenuScene extends Scene {

	protected CameraInput cameraInput;

	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		
		// Ideally, using a designated 2D text & sprite renderer would be the
		// best idea.
		if(exiting) {
			exit();
			return;
		}
		
		GL2 gl = Yeti.get().gl;
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		// Do not use a custom renderer
		TextHelper.beginRendering(camera.getWidth(), camera.getHeight());
		{
			String hud = String.format("Testing 1, 2, 3 menu");			
			TextHelper.drawTextMultiLine(10, 100, hud);
		}
		TextHelper.endRendering();
	}
}
