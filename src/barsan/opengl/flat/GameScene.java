package barsan.opengl.flat;

import java.awt.RenderingHints.Key;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.math.Rectangle;
import barsan.opengl.math.Vector2;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.SkyBox;
import barsan.opengl.rendering.lights.DirectionalLight;
import barsan.opengl.resources.ResourceLoader;

public class GameScene extends Scene {

	World2D world;
	Player player;
	
	class InputPoller extends KeyAdapter {
		// Quick hacky class to test physics
		public float move;
		
		public boolean jmp;
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_LEFT) {
				move = -1.0f;
			} else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
				move = 1.0f;
			} else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
				jmp = true;
			}
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_LEFT) {
				move = 0.0f;
			} else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
				move = 0.0f;
			} else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
				jmp = false;
			}
		}
	}
	
	InputPoller poller = new InputPoller();
	
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
		world.addEntity(player = new Player(new Vector2(0.0f, 0.0f)));
		player.graphics.getTransform().updateRotation(0.0f, 1.0f, 0.0f, 90.0f);
		
		Yeti.get().addKeyListener(poller);
		
		world.addEntity(new Block(new Rectangle(-5, -10, 40, 1)));
		world.addEntity(new Block(new Rectangle(25, -20, 20, 4)));
		
		lights.add(new DirectionalLight(new Vector3(1f, 3.0f, 0.0f).normalize()));
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		world.update(getDelta());
		super.display(drawable);
		
		player.physics.velocity.x = 8.0f * poller.move;
		if(poller.jmp) {
			player.jump();
		}
	}
}
