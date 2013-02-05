package barsan.opengl.flat;

import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.lights.DirectionalLight;

public class GameScene extends Scene {

	World2D world;
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);

		lights.add(new DirectionalLight(new Vector3(0.5f, 2.0f, 0.0f).normalize()));
		world = new World2D(this);
	}
}
