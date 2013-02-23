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
import barsan.opengl.planetHeads.Coin;
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
		ResourceLoader.loadObj("coin", "coin.obj");
		ResourceLoader.loadTexture("coin", "coinTex.png");
		ResourceLoader.loadTexture("block01", "cubetex.png");
		ResourceLoader.loadKeyFrameAnimatedObj("planetHeadAnimated", "planetHead");
		
		addModelInstance(new SkyBox(ResourceLoader.cubeTexture("skybox01"), camera));
		shadowsEnabled = true;
		
		addInput(cameraInput = new CameraInput(camera));
		
		// Let's set up the level
		world = new World2D(this);
		world.addEntity(player = new Player(new Vector2(0.0f, 0.0f)));
		player.getGraphics().getTransform().updateRotation(0.0f, 1.0f, 0.0f, 90.0f);
		
		Yeti.get().addInputProvider(poller);
		
		addBlock(-10, -20, 5, 40);
		world.addEntity(new Block(new Rectangle(-5, -14, 40, 4), ResourceLoader.texture("block01")));
		world.addEntity(new Block(new Rectangle(25, -20, 20, 4), ResourceLoader.texture("block01")));
		world.addEntity(new Block(new Rectangle(45, -15, 15, 12), ResourceLoader.texture("block01")));
		
		for(int i = 0; i < 10; i++) {
			addBlock(i * 10, -0.6f + i * 2.5f, 8, 2);
		}
		
		for(int i = 0; i < 10; i++) {
			world.addEntity(new Coin(new Vector2(5 * i, -3.5f)));
		}
		
		lights.add(new DirectionalLight(new Vector3(1f, 3.0f, 0.0f).normalize()));
	}
	
	int lastSector = 0;
	int currentSector = 0;
	float sectorHeight = 15.0f;
	float spc = 0.9f;
	float currentY = 0.0f;
	float cSpeed = 10000.0f * sectorHeight;
	
	@Override
	public void display(GLAutoDrawable drawable) {
		world.update(getDelta());
		super.display(drawable);
		
		// Handles "smart" camera that doesn't spazz out and follow each jump
		// of the player
		Rectangle pr = player.getPhysics2d().getBounds();
		
		while(pr.y > currentSector * sectorHeight + sectorHeight * spc) {
			// Passed into the upper sector
			// [while] to support extreme cases
			currentSector++;
		}
		
		while(pr.y < currentSector * sectorHeight - sectorHeight * spc) {
			currentSector--;
		}

		float delta = getDelta();
		float goal = currentSector * sectorHeight + 5;
		if(currentY < goal) {
			currentY = Math.min(goal, currentY + cSpeed * delta);
		} else if(currentY > goal) {
			currentY = Math.max(goal, currentY - cSpeed * delta);
		}
		
		Vector3 pp = new Vector3(pr.x + pr.width / 2, currentY, 0.0f);
		Vector3 vs = new Vector3(pp.x, pp.y + 15.0f, -45.0f);
		camera.lookAt(vs, pp, Vector3.UP.copy());
		
		// Handle controls (rudimentarily)
		player.getPhysics2d().acceleration.x = 400.0f * poller.move;
		if(poller.move != 0) {
			player.wantsToWalk = true;
		} else {
			player.wantsToWalk = false;
		}
		
		player.wantsToJump = poller.jmp;
	}
	
	@Override
	public void play() {
		cameraInput.setMouseControlled(true);
	}
	
	@Override
	public void pause() {
		cameraInput.setMouseControlled(false);
	}
	
	private void addBlock(float x, float y, float w, float h) {
		world.addEntity(new Block(new Rectangle(x, y, w, h), ResourceLoader.texture("block01")));
	}
}
