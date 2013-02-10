package barsan.opengl.flat;

import java.io.IOException;

import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.math.Transform;
import barsan.opengl.math.Vector2;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Model;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.SkyBox;
import barsan.opengl.rendering.lights.DirectionalLight;
import barsan.opengl.resources.ResourceLoader;

public class GameScene extends Scene {

	World2D world;
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);

		try {
			ResourceLoader.loadCubeTexture("skybox01", "jpg");
			ResourceLoader.loadObj("planetHead", "res/models/planetHead.obj");
		} catch(IOException e) {
			Yeti.screwed("Resource loading failed", e);
		}
		
		addModelInstance(new SkyBox(ResourceLoader.cubeTexture("skybox01"), camera));
		shadowsEnabled = true;
		
		// Let's set up the level
		world = new World2D(this);
		world.addEntity(new Player(new Vector2(30.0f, 0.0f)));
		
		addModelInstance(new ModelInstance(
				Model.buildPlane(300.0f, 10.0f, 30, 1),
				new Transform().updateTranslate(0.0f, 0.0f, -5.0f)
				));
		
		lights.add(new DirectionalLight(new Vector3(1f, 3.0f, 0.0f).normalize()));
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		world.update(getDelta());
		super.display(drawable);
	}
}
