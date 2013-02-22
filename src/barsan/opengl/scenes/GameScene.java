package barsan.opengl.scenes;

import java.awt.event.KeyEvent;

import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.flat.Block;
import barsan.opengl.flat.Player;
import barsan.opengl.flat.World2D;
import barsan.opengl.input.CameraInput;
import barsan.opengl.input.InputAdapter;
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

	InputPoller poller = new InputPoller();
	protected CameraInput cameraInput;
	
	
	class InputPoller extends InputAdapter {
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
				if(move == -1.0f) {
					move = 0.0f;
				}
			} else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
				if(move == 1.0f) {
					move = 0.0f;
				}
			} else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
				jmp = false;
			}
		}
	}
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);

		ResourceLoader.loadCubeTexture("skybox01", "jpg");
		ResourceLoader.loadObj("planetHead", "planetHead.obj");
		//ResourceLoader.loadObj("planetHead", "planetHead/exp_18.obj");
		ResourceLoader.loadKeyFrameAnimatedObj("planetHeadAnimated", "planetHead");
		
		addModelInstance(new SkyBox(ResourceLoader.cubeTexture("skybox01"), camera));
		shadowsEnabled = true;
		
		addInput(cameraInput = new CameraInput(camera));
		
		// Let's set up the level
		world = new World2D(this);
		world.addEntity(player = new Player(new Vector2(0.0f, 0.0f)));
		player.getGraphics().getTransform().updateRotation(0.0f, 1.0f, 0.0f, 90.0f);
		
		Yeti.get().addInputProvider(poller);
		
		world.addEntity(new Block(new Rectangle(-5, -10, 40, 1)));
		world.addEntity(new Block(new Rectangle(25, -20, 20, 4)));
		
		lights.add(new DirectionalLight(new Vector3(1f, 3.0f, 0.0f).normalize()));
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		world.update(getDelta());
		super.display(drawable);
		
		player.getPhysics2d().acceleration.x = 300.0f * poller.move;
		if(poller.move != 0) {
			player.wantsToWalk = true;
		} else {
			player.wantsToWalk = false;
		}
		if(poller.jmp) {
			player.jump();
		}
	}
	
	@Override
	public void play() {
		cameraInput.setMouseControlled(true);
	}
	
	@Override
	public void pause() {
		cameraInput.setMouseControlled(false);
	}
}
