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
import barsan.opengl.rendering.Cube;
import barsan.opengl.rendering.Fog;
import barsan.opengl.rendering.Renderer;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.SkyBox;
import barsan.opengl.rendering.StaticModelInstance;
import barsan.opengl.rendering.lights.DirectionalLight;
import barsan.opengl.rendering.materials.BasicMaterial;
import barsan.opengl.rendering.materials.BumpComponent;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.rendering.materials.TextureComponent;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;
import barsan.opengl.util.DebugGUI;

public class GameScene extends Scene {

	World2D world;

	InputPoller poller = new InputPoller();
	protected CameraInput cameraInput;
	
	public class InputPoller extends InputAdapter {
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
			else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				Yeti.get().loadScene(new MenuScene());
			}
			
		}
	}
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);

		ResourceLoader.loadCubeTexture("skybox01", "jpg");
		ResourceLoader.loadObj("coin", "coin.obj");
		ResourceLoader.loadObj("end", "heavenBeam.obj");
		ResourceLoader.loadObj("cube", "texcube.obj");
		ResourceLoader.loadTexture("coin", "coinTex.png");
		ResourceLoader.loadTexture("block01", "stone03.jpg");
		ResourceLoader.loadKeyFrameAnimatedObj("planetHeadAnimated", "planetHead");
		
		addModelInstance(new SkyBox(ResourceLoader.cubeTexture("skybox01"), camera));
		
		Renderer.renderDebug = false;
		shadowsEnabled = true;
		
		// Let's set up the level
		world = new World2D(this);
		world.reset();
		
		addInput(poller);
		
		gui = new GameGUI(world.getPlayer());
		gui.setPosition(new Vector2(220.0f, 10.0f));
		
		lights.add(new DirectionalLight(new Vector3(1f, 3.0f, 0.0f).normalize()));
	}
	
	
	int lastSector = 0;
	int currentSector = 0;
	float sectorHeight = 15.0f;
	float spc = 0.9f;
	float currentY = 0.0f;
	float cSpeed = 50.0f;
	
	@Override
	public void display(GLAutoDrawable drawable) {
		// Compensate for MAXIMUM 2.5-ish frames
		world.update(Math.min(Yeti.get().getDelta(), 0.05f));
		super.display(drawable);
		
		// Handles "smart" camera that doesn't spazz out and follow each jump
		// of the player
		Rectangle pr = world.getPlayer().getPhysics2d().getBounds();
		
		if(pr.y > currentSector * sectorHeight + sectorHeight * spc) {
			// Passed into the upper sector
			currentSector++;
		} else if(pr.y < currentSector * sectorHeight - sectorHeight * spc) {
			currentSector--;
		}

		float delta = Yeti.get().getDelta();
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
		world.getPlayer().handleInput(poller);
		
		renderer.setDirectionalShadowCenter(new Vector3(pp.x, pp.y, 0.0f));
	}
	
	@Override
	public void beginExit(Yeti engine, Scene next) {
		world.clearAllEntities();
		super.beginExit(engine, next);
	}
	
	@Override
	public void play() {
	}
	
	@Override
	public void pause() {
	}
	
}
