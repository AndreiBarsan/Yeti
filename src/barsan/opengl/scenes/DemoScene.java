package barsan.opengl.scenes;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.math.Transform;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Fog;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.PerspectiveCamera;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.SkyBox;
import barsan.opengl.rendering.StaticModelInstance;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.materials.BasicMaterial;
import barsan.opengl.rendering.materials.BumpComponent;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.rendering.materials.TextureComponent;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;
import barsan.opengl.util.DebugGUI;

public class DemoScene extends Scene {
	
	PointLight pl;
	float a = 0.0f;
	Material ironbox;
	Material redShit, blueShit;
	boolean smoothRendering = true;
	
	SkyBox sb;
	Transform tct;
	
	ModelInstance m2;
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
			
		try {
			//ResourceLoader.loadObj("asteroid10k", "asteroid10k.obj");
			//ResourceLoader.loadObj("asteroid1k", "asteroid1k.obj");
			ResourceLoader.loadObj("sphere", "prettysphere.obj");
			//ResourceLoader.loadObj("bunny", "bunny.obj");
			ResourceLoader.loadObj("texcube", "texcube.obj");
			
			/*
			ResourceLoader.loadTexture("heightmap01", "height.png");
			ResourceLoader.loadTexture("grass", "grass01.jpg");
			*/
			ResourceLoader.loadTexture("stone", "stone03.jpg");
			ResourceLoader.loadTexture("stone.bump", "stone03.bump.jpg");
			//ResourceLoader.loadTexture("billboard", "tree_billboard.png");
			
			ResourceLoader.loadCubeTexture("skybox01", "jpg");
			
		} catch (IOException e) {
			System.out.println("Could not load the resources.");
			e.printStackTrace();
		}
		
		//blueShit = new BasicMaterial(new Color(0.0f, 0.0f, 1.0f));
		//blueShit.setShininess(16);
		//redShit = new ToonMaterial(new Color(1.0f, 0.25f, 0.33f));
		
		// FIXME: this isn't right; the skybox should be drawn last in
		// order for as few fragments as possible to be processed, not first
		// so that we overwrite most of it!
		SkyBox sb = new SkyBox(ResourceLoader.cubeTexture("skybox01"), camera);
		modelInstances.add(sb);
		
		/*
		Model groundMesh = HeightmapBuilder.modelFromMap(Yeti.get().gl.getGL2(),
				ResourceLoader.texture("heightmap01"),
				ResourceLoader.textureData("heightmap01"),
				4.0f, 4.0f,
				-15.0f, 120.0f);
		
		
		modelInstances.add(new ModelInstance(
				groundMesh,
				new MultiTextureMaterial(ResourceLoader.texture("stone"),
						ResourceLoader.texture("grass"), -10, 25)
				//new ToonMaterial(ResourceLoader.texture("grass"))
				));
		//*/
		//*
		//modelInstances.add(new ModelInstance(ResourceLoader.model("sphere"),
		//		new CubicEnvMappingMaterial(ResourceLoader.cubeTexture("skybox01"), ResourceLoader.texture("grass")),
		//		new Transform().updateTranslate(0.0f, 50.0f, -30.0f).updateScale(4.0f)));
		//*/
		
		shadowsEnabled = false;
		
		Material bumpMat = new BasicMaterial();
		bumpMat.setTexture(ResourceLoader.texture("stone"));
		bumpMat.addComponent(new TextureComponent());
		bumpMat.addComponent(new BumpComponent(ResourceLoader.texture("stone.bump")));
		
		StaticModelInstance daddy;
		tct = new Transform().updateTranslate(0.0f, 50.0f, 3.0f).updateScale(1.0f);
		modelInstances.add(daddy = new StaticModelInstance(ResourceLoader.model("texcube"), 
				bumpMat, tct));
		

		daddy.addChild(new StaticModelInstance(ResourceLoader.model("sphere"),
				bumpMat, new Transform().updateTranslate(10.0f, 0.5f, 0.0f)));
		
		ModelInstance m1;
		daddy.addChild(m1 = new StaticModelInstance(ResourceLoader.model("sphere"),
				bumpMat, new Transform().updateTranslate(-10.0f, 0.5f, 0.0f)));
		
		m1.addChild(m2 = new StaticModelInstance(ResourceLoader.model("sphere"),
				bumpMat, new Transform().updateTranslate(-2.0f, 0.5f, 0.0f).updateScale(0.33f)));
		
		camera.setPosition(new Vector3(0.0f, 50.00f, 0.0f));
		((PerspectiveCamera)camera).setFOV(90.0f);
		
		lights.add(pl = new PointLight(new Vector3(0f, 15f, 10f), new Color(0.75f, 0.80f, 0.75f, 1.0f)));
		
		globalAmbientLight.setColor(new Color(0.05f, 0.05f, 0.05f));
		
		fog = new Fog(new Color(0.0f, 0.0f, 0.0f, 0.0f));
		fog.fadeCamera(camera);
		fogEnabled = true;
		
		gui = new DebugGUI(drawable.getAnimator(), getCamera());
		
		Yeti.debug("\n\tRendering controls: \n\tF - toggle Fog\n\tM - toggle sMoothing");
		Yeti.get().addKeyListener(new KeyListener() {
			
			public void keyPressed(KeyEvent e) { }
			public void keyTyped(KeyEvent e) { }
			
			public void keyReleased(KeyEvent e) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_F:
					fogEnabled = !fogEnabled;
					break;
				}
			}
		});
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		a += 0.8 * getDelta() * 10;
		
		pl.getPosition().x = 10 * (float)(30 * Math.sin(a / 10));
		tct.updateRotation(0.0f, 1.0f, 0.0f, a * 15);
		
		m2.getTransform().updateRotation(0.0f, 1.0f, 0.0f, a * 10);
		float orbit = 2.0f;
		m2.getTransform().updateTranslate((float)Math.cos(a / 3) * orbit, 0.0f, (float)Math.sin(a / 3) * orbit);
		
		// Calls the renderer
		super.display(drawable);
	}
}
