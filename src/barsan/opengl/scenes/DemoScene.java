package barsan.opengl.scenes;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Collections;

import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.math.Transform;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.materials.BasicMaterial;
import barsan.opengl.rendering.materials.CubicEnvMappingMaterial;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.rendering.materials.ToonMaterial;
import barsan.opengl.rendering.materials.BasicMaterial.BumpComponent;
import barsan.opengl.rendering.Fog;
import barsan.opengl.rendering.Model;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.SkyBox;
import barsan.opengl.resources.HeightmapBuilder;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;
import barsan.opengl.util.TextHelper;

public class DemoScene extends Scene {
	
	PointLight pl;
	float a = 0.0f;
	Material ironbox;
	Material redShit, blueShit;
	boolean smoothRendering = true;
	
	SkyBox sb;
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		
		try {
			ResourceLoader.loadObj("asteroid10k", "res/models/asteroid10k.obj");
			ResourceLoader.loadObj("asteroid1k", "res/models/asteroid1k.obj");
			ResourceLoader.loadObj("sphere", "res/models/prettysphere.obj");
			ResourceLoader.loadObj("bunny", "res/models/bunny.obj");
			ResourceLoader.loadObj("texcube", "res/models/texcube.obj");
			
			ResourceLoader.loadTexture("heightmap01", "res/tex/height.png");
			ResourceLoader.loadTexture("grass", "res/tex/grass01.jpg");
			ResourceLoader.loadTexture("stone", "res/tex/stone03.jpg");
			ResourceLoader.loadTexture("stone.bump", "res/tex/stone03.bump.jpg");
			ResourceLoader.loadTexture("billboard", "res/tex/tree_billboard.png");
			
			ResourceLoader.loadCubeTexture("skybox01", "jpg");
			
		} catch (IOException e) {
			System.out.println("Could not load the resources.");
			e.printStackTrace();
		}
		
		blueShit = new BasicMaterial(new Color(0.0f, 0.0f, 1.0f));
		redShit = new ToonMaterial(new Color(1.0f, 0.25f, 0.33f));
		
		// FIXME: this isn't right; the skybox should be drawn last in
		// order for as few fragments as possible to be processed, not first
		// so that we overwrite most of it!
		SkyBox sb = new SkyBox(Yeti.get().gl, ResourceLoader.cubeTexture("skybox01"), camera);
		modelInstances.add(sb);
		
		blueShit.setShininess(16);
		
		Model groundMesh = HeightmapBuilder.modelFromMap(Yeti.get().gl,
				ResourceLoader.texture("heightmap01"),
				ResourceLoader.textureData("heightmap01"),
				4.0f, 4.0f,
				-15.0f, 120.0f);
		
		/*
		modelInstances.add(new ModelInstance(
				groundMesh,
				new MultiTextureMaterial(ResourceLoader.texture("stone"),
						ResourceLoader.texture("grass"), -10, 25)
				//new ToonMaterial(ResourceLoader.texture("grass"))
				));
		//*/
		//*
		modelInstances.add(new ModelInstance(ResourceLoader.model("sphere"),
				new CubicEnvMappingMaterial(ResourceLoader.cubeTexture("skybox01"), ResourceLoader.texture("grass")),
				new Transform().updateTranslate(0.0f, 50.0f, -30.0f).updateScale(4.0f)));
		//*/
		camera.setPosition(new Vector3(0.0f, 50.00f, 0.0f));
		camera.setDirection(new Vector3(0.0f, 0.0f, -1.0f));
		camera.setFOV(45.0f);
		
		pointLights.add(pl = new PointLight(new Vector3(0f, 15f, 10f), new Color(0.75f, 0.80f, 0.75f, 1.0f)));
		
		globalAmbientLight.setColor(new Color(0.05f, 0.05f, 0.05f));
		
		fog = new Fog(new Color(0.0f, 0.0f, 0.0f, 0.0f));
		fog.fadeCamera(camera);
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
	public void display(GLAutoDrawable drawable) {
		a += 0.8 * getDelta() * 10;
		
		pl.getPosition().x = 10 * (float)(30 * Math.sin(a / 10));
		
		// Calls the renderer
		super.display(drawable);
		drawGUI(drawable);
	}
	
	void drawGUI(GLAutoDrawable drawable) {
		float fps = drawable.getAnimator().getLastFPS();
		Yeti.get().gl.glUseProgram(0);
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
