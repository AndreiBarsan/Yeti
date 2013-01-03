package barsan.opengl.scenes;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.BasicMaterial;
import barsan.opengl.rendering.BasicMaterial.ShadingModel;
import barsan.opengl.rendering.Fog;
import barsan.opengl.rendering.HeightMapMaterial;
import barsan.opengl.rendering.Model;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.PointLight;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.SkyBox;
import barsan.opengl.rendering.ToonMaterial;
import barsan.opengl.resources.HeightmapBuilder;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;
import barsan.opengl.util.TextHelper;

public class DemoScene extends Scene {
	
	private ModelInstance a10k;
	float a = 0.0f;
	BasicMaterial ironbox;
	BasicMaterial redShit, blueShit;
	boolean smoothRendering = true;
	
	SkyBox sb;
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		
		try {
			ResourceLoader.loadObj("asteroid10k", "res/models/asteroid10k.obj");
			ResourceLoader.loadObj("asteroid1k", "res/models/asteroid1k.obj");
			ResourceLoader.loadObj("sphere", "res/models/sphere.obj");
			ResourceLoader.loadObj("bunny", "res/models/bunny.obj");
			ResourceLoader.loadObj("texcube", "res/models/texcube.obj");
			
			ResourceLoader.loadTexture("heightmap01", "res/tex/height.png");
			ResourceLoader.loadTexture("grass", "res/tex/grass01.jpg");
			ResourceLoader.loadTexture("stone", "res/tex/stone03.jpg");
			ResourceLoader.loadTexture("billboard", "res/tex/tree_billboard.png");
			
			ResourceLoader.loadCubeTexture("test", "png");
			
			blueShit = new BasicMaterial(new Color(0.0f, 0.0f, 1.0f));
			redShit = new ToonMaterial(new Color(1.0f, 0.25f, 0.33f));
			
			// FIXME: this isn't right; the skybox should be drawn last in
			// order for as few fragments as possible to be processed, not first
			// so that we overwrite most of it!
			SkyBox sb = new SkyBox(Yeti.get().gl, ResourceLoader.cubeTexture("test"), camera);
			modelInstances.add(sb);
			
			blueShit.setShininess(16);
			
			a10k = new ModelInstance(
							ResourceLoader.model("bunny"),
							blueShit,
							new Matrix4()
						);
			
			ModelInstance a1k = new ModelInstance(
							ResourceLoader.model("asteroid10k"),
							redShit,
							new Matrix4().setTranslate(0.0f, 20.0f, 20.0f)
							);
			
			BasicMaterial gMat = new BasicMaterial();
			gMat.setMode(ShadingModel.Gouraud);
			
			BasicMaterial pMat = new BasicMaterial();
			pMat.setMode(ShadingModel.Phong);
			
			
			// Setup the iron box (which has texture mapping)
			/*
			ironbox = new BasicMaterial();
			ironbox.setTexture(TextureIO.newTexture(
					new File("res/tex/cubetex.png"), false
					));
			ModelInstance texcube = new ModelInstance(
					ResourceLoader.model("texcube"),
					ironbox,
					new Matrix4()
					);
			*/
			
			
			modelInstances.add(a10k);
			modelInstances.add(a1k);
			//modelInstances.add(texcube);
			
			Model groundMesh = HeightmapBuilder.modelFromMap(Yeti.get().gl,
					ResourceLoader.texture("heightmap01"),
					ResourceLoader.textureData("heightmap01"),
					2.0f, 2.0f,
					-15.0f, 120.0f);
			
			//BasicMaterial gmm = new BasicMaterial();
			//gmm.setTexture(ResourceLoader.texture("grass"));
			//gmm.setShininess(128);
			modelInstances.add(new ModelInstance(
					groundMesh,
					new HeightMapMaterial(ResourceLoader.texture("stone"),
							ResourceLoader.texture("grass"), -10, 25),
					//new ToonMaterial(ResourceLoader.texture("grass")),
					//gmm,
					new Matrix4()
					));
			
		} catch (IOException e) {
			System.out.println("Could not load the resources.");
			e.printStackTrace();
		}
		
		camera.setPosition(new Vector3(0.0f, 0.25f, -4.0f));
		camera.setDirection(new Vector3(0.0f, 0.0f, -1.0f));
		camera.setFOV(45.0f);
		pointLights.add(new PointLight(new Vector3(0f, 15f, 10f), new Color(1.0f, 1.0f, 1.0f, 1.0f)));
		ambientLight.setColor(new Color(0.05f, 0.05f, 0.05f));
		
		fog = new Fog(camera.getFrustumFar() - 8.0f, camera.getFrustumFar(), new Color(0.0f, 0.0f, 0.0f, 0.0f));
		fogEnabled = true;
		Yeti.get().gl.glClearColor(0.1f, 0.33f, 0.2f, 1.0f);
		
		Yeti.debug("\n\tRendering controls: \n\tF - toggle Fog\n\tM - toggle sMoothing");
		Yeti.get().addKeyListener(new KeyListener() {
			
			public void keyPressed(KeyEvent e) { }
			public void keyTyped(KeyEvent e) { }
			
			public void keyReleased(KeyEvent e) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_F:
					fogEnabled = !fogEnabled;
					break;
					
				case KeyEvent.VK_M:
					smoothRendering = !smoothRendering;
					break;
				}
			}
		});
	}
	
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		super.reshape(drawable, x, y, width, height);
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		float radius = 3.2f;
		a += 0.8 * getDelta() * 10;
		
		
		a10k.getTransform().setRotate(a, 0.0f, 0.0f, 1.0f)//.setTranslate(1.0f, -1.0f, 2.0f)
			.mul(new Matrix4().setScale(2.0f));
		
		PointLight light = pointLights.get(0);
		light.getPosition().x = 10 * (float)(30 * Math.sin(a / 10));
		
		// Calls the renderer
		super.display(drawable);
		
		drawGUI(drawable);
	}
	
	void drawGUI(GLAutoDrawable drawable) {
		float fps = drawable.getAnimator().getLastFPS();
		drawable.getGL().getGL2().glUseProgram(0);
		TextHelper.beginRendering(camera.getWidth(), camera.getHeight());
		{
			Vector3 cp = camera.getPosition();
			String hud = String.format("FPS: %.2f\nCamera: X:%.2f Y:%.2f Z:%.2f", fps,
					cp.x, cp.y, cp.z);
			
			TextHelper.drawTextMultiLine(20, 20, hud);
		}
		TextHelper.endRendering();		
	}
}
