package barsan.opengl.scenes;

import java.awt.event.KeyEvent;
import java.util.Random;

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
import barsan.opengl.planetHeads.GameGUI;
import barsan.opengl.rendering.Renderer;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.SkyBox;
import barsan.opengl.rendering.lights.DirectionalLight;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.DebugGUI;

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
		ResourceLoader.loadObj("cube", "texcube.obj");
		ResourceLoader.loadTexture("coin", "coinTex.png");
		ResourceLoader.loadTexture("block01", "stone03.jpg");
		ResourceLoader.loadKeyFrameAnimatedObj("planetHeadAnimated", "planetHead");
		
		addModelInstance(new SkyBox(ResourceLoader.cubeTexture("skybox01"), camera));
		
		Renderer.renderDebug = false;
		shadowsEnabled = true;
		
		// Let's set up the level
		world = new World2D(this);
		world.addEntity(player = new Player(new Vector2(0.0f, 0.0f)));
		player.getGraphics().getTransform().updateRotation(0.0f, 1.0f, 0.0f, 90.0f);
		
		gui = new GameGUI(player);
		gui.setPosition(new Vector3(220.0f, 10.0f, 0.0f));
		
		Yeti.get().addInputProvider(poller);
		
		// Left wall
		addBlock(-10, -20, 5, 40);
		// Walkway A
		addBlock(-5, -14, 40, 4);
		// Walkway B
		addBlock(25, -20, 20, 4);
		// Right square-ish wall/walkway
		addBlock(45, -15, 15, 12);
		
		Random rand = new Random();
		for(int i = 0; i < 20; i++) {
			addBlock(i * 10, -0.6f + i * 3.5f, 8, 2);
			if(i % 5 == 0) {
				addBlock(i * 10 - 2 + rand.nextFloat() * 4, 20f + i * 3.5f, 2 + rand.nextFloat() * 6, 4);
			}
		}
		
		for(int i = 0; i < 8; i++) {
			world.addEntity(new Coin(new Vector2(5 * i, -4.5f)));
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
		
		if(pr.y > currentSector * sectorHeight + sectorHeight * spc) {
			// Passed into the upper sector
			currentSector++;
		} else if(pr.y < currentSector * sectorHeight - sectorHeight * spc) {
			currentSector--;
		}

		float delta = getDelta();
		float goal = currentSector * sectorHeight + 5;
		if(goal < -10) goal = -10;
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
		
		renderer.setDirectionalShadowCenter(new Vector3(pp.x, pp.y, 0.0f));
	}
	
	@Override
	public void play() {
	}
	
	@Override
	public void pause() {
	}
	
	private void addBlock(float x, float y, float w, float h) {
		
		float divW = 10.0f;
		float divH = 10.0f;
		
		int wSteps = (int)(w / divW);
		int hSteps = (int)(h / divH);
		
		float wReminder = w - wSteps * divW;
		float hReminder = h - hSteps * divH;
		
		for(int i = 0; i < wSteps; i++) {
			for(int j = 0; j < hSteps; j++) {
				world.addEntity(new Block(new Rectangle(x + i * divW, y + j * divH, divW, divH), ResourceLoader.texture("block01")));
			}
			if(hReminder > 0.01f) {
				world.addEntity(new Block(new Rectangle(x + i * divW, y + h - hReminder, divW, hReminder), ResourceLoader.texture("block01")));
			}
		} 
		if(wReminder > 0.01f) {
			for(int j = 0; j < hSteps; j++) {
				world.addEntity(new Block(new Rectangle(x + w - wReminder, y + j * divH, wReminder, divH), ResourceLoader.texture("block01")));
			}
			if(hReminder > 0.01f) {
				world.addEntity(new Block(new Rectangle(x + w - wReminder, y + h - hReminder, wReminder, hReminder), ResourceLoader.texture("block01")));
			}
		}
	}
}
