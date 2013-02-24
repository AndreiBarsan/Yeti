package barsan.opengl.util;

import java.awt.Font;

import javax.media.opengl.GLAnimatorControl;

import barsan.opengl.Yeti;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Camera;
import barsan.opengl.rendering.Scene;

public class DebugGUI extends GUI {

	GLAnimatorControl animator;
	Scene host;
	Camera camera;
	public String info = "";
	private final Font debugFont = new Font(Font.MONOSPACED, Font.PLAIN, 20);
	
	public DebugGUI(Scene scene, GLAnimatorControl glAnimatorControl) {
		this.animator = glAnimatorControl;
		this.host = scene;
		this.camera = scene.getCamera();
	}
	
	@Override
	public void render() {
		float fps = animator.getLastFPS();
		
		TextHelper.setFont(debugFont);
		TextHelper.beginRendering(camera.getWidth(), camera.getHeight());
		{
			Vector3 cp = camera.getPosition();
			String hud = String.format("FPS: %.2f | %d entities\nCamera: X:%.2f Y:%.2f Z:%.2f\n%s", fps,
					host.getModelInstances().size(), cp.x, cp.y, cp.z, info);
			
			TextHelper.drawTextMultiLine((int)position.x, (int)position.y, hud);
		}
		TextHelper.endRendering();
	}

}
