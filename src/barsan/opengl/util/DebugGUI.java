package barsan.opengl.util;

import javax.media.opengl.GLAnimatorControl;

import barsan.opengl.Yeti;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Camera;

public class DebugGUI extends GUI {

	GLAnimatorControl animator;
	Camera camera;
	public String info = "";
	
	public DebugGUI(GLAnimatorControl glAnimatorControl, Camera camera) {
		this.animator = glAnimatorControl;
		this.camera = camera;
	}
	
	@Override
	public void render() {
		float fps = animator.getLastFPS();
		
		Yeti.get().gl.glUseProgram(0);
		TextHelper.beginRendering(camera.getWidth(), camera.getHeight());
		{
			Vector3 cp = camera.getPosition();
			String hud = String.format("FPS: %.2f\nCamera: X:%.2f Y:%.2f Z:%.2f\n%s", fps,
					cp.x, cp.y, cp.z, info);
			
			TextHelper.drawTextMultiLine((int)position.x, (int)position.y, hud);
		}
		TextHelper.endRendering();
	}

}
