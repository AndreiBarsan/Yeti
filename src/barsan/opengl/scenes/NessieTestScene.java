package barsan.opengl.scenes;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.input.CameraInput;
import barsan.opengl.input.InputAdapter;
import barsan.opengl.math.MathUtil;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.Nessie;
import barsan.opengl.rendering.StaticModel;
import barsan.opengl.rendering.Nessie.Mode;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.StaticModelInstance;
import barsan.opengl.rendering.lights.DirectionalLight;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.lights.SpotLight;
import barsan.opengl.rendering.materials.BasicMaterial;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.resources.ModelLoader;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;
import barsan.opengl.util.DebugGUI;

public class NessieTestScene extends Scene {

	CameraInput cameraInput;
	PointLight mainLight;
	PointLight l2;
	ModelInstance box;
	Nessie nessie;
	
	ArrayList<SpotLight> slights = new ArrayList<>();
	
	@Override
	public void init(GLAutoDrawable drawable) {
		nessie = new Nessie(Yeti.get().gl);
		renderer = nessie;
		
		super.init(drawable);		
		
		Yeti.get().gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		addInput(cameraInput = new CameraInput(camera));
		gui = new DebugGUI(this, drawable.getAnimator());
		gui.setPosition(10, 10);
		
		camera.lookAt(new Vector3(-45.0f, 30.0f, -45.0f), new Vector3(0.0f, -10.0f, 0.0f), Vector3.UP.copy());
		
		//lights.add(mainLight = new PointLight(new Vector3(-0.25f, 5.0f, 0.0f), new Color(1.0f, 1.0f, 0.9f, 5.0f)));
		//lights.add(new PointLight(new Vector3(1.2f, -.1f, 1f), new Color(0.9f, 0.9f, 0.9f, 1.0f)));
		//lights.add(new PointLight(new Vector3(3f, 10.0f, 0.0f), new Color(0.9f, 0.9f, 0.9f, 1.0f)));
		//mainLight.setAttenuation(1.0f, 2.5f, 1.0f, 0.0f);
		PointLight l = new PointLight(new Vector3(0.1f, 0.75f, 0.33f), new Color(1.0f, 1.0f, 1.0f, 1.0f));
		//l.setAttenuation(1.0f, 0.25f, 1.0f, 0.0f);
		//lights.add(l);
		
		lights.add(new DirectionalLight(new Vector3(1.0f, -1.0f, 0.0f).normalize()));
		
		l2 = new PointLight(new Vector3(-0.5f, -2.75f, -0.33f), new Color(1.0f, 1.0f, 0.5f, 3.44f));
		l2.setAttenuation(0.0f, 0.00f, 0.05f);
		lights.add(l2);
		
		ResourceLoader.loadObj("box", "texcube.obj");
		ResourceLoader.loadObj("monkey", "monkey.obj");
		ResourceLoader.loadObj("DR_sphere", "dr_icosphere.obj");
		ResourceLoader.loadObj("DR_cone", "cone.obj");
		ResourceLoader.loadTexture("cubetex", "cubetex.png");
		ResourceLoader.loadTexture("floor", "floor.jpg");
		ResourceLoader.loadTexture("floor.bump", "floor.bump.jpg");
		box = new StaticModelInstance(ResourceLoader.model("box"));
		box.getMaterial().setDiffuseMap(ResourceLoader.texture("cubetex"));		
		box.getTransform().updateScale(4.0f).updateTranslate(2.0f, 10.5f, 0.0f);
		//addModelInstance(box);
		
		ModelInstance floor = new StaticModelInstance(ModelLoader.buildPlane(250.0f, 250.0f, 25, 25));
		floor.getTransform().updateTranslate(0.0f, -10.0f, 0.0f);
		floor.getMaterial().setDiffuseMap(ResourceLoader.texture("floor"));
		floor.getMaterial().setNormalMap(ResourceLoader.texture("floor.bump"));
		floor.getMaterial().setSpecularIntensity(2.0f);
		floor.getMaterial().setSpecularPower(512);
		addModelInstance(floor);
		
		//BasicMaterial monkeyMat = new BasicMaterial(new Color(0.05f, 0.05f, 0.9f));
		//*
		int mlim = 10;
		for(int i = -mlim; i < mlim; ++i) {
			for(int j = -mlim; j < mlim; ++j) {
				Material mat = new BasicMaterial( Color.random() );
				mat.setSpecularIntensity(10.0f);
				mat.setSpecularPower(64);
				StaticModelInstance monkey = new StaticModelInstance(ResourceLoader.model("monkey"), mat);
				monkey.getTransform().updateTranslate(i * 4.2f, -8.5f, j * 4.2f);
				addModelInstance(monkey);
			}
		}//*/
		
		/*
		int lightLim = 1;
		float lgs = 24.0f;
		for(int i = -lightLim; i < lightLim; ++i) {
			for(int j = -lightLim; j < lightLim; ++j) {
				Color c = Color.random();
				c.a = 2.75f;
				lights.add(new PointLight(new Vector3(i * lgs, -4.0f, j * lgs), c));
				
				
			}
		}//*/
		
		/*
		int al = 10;
		float sector = ((float)Math.PI * 2.0f) / al;
		for(int i = 0; i < al; ++i) {
			SpotLight spot = new SpotLight(
					new Vector3(0.0f, -2.0f, 0),
					new Vector3(-(float)Math.cos(i * sector), 0.0f, (float)Math.sin(i * sector)).normalize(),
					(float)Math.cos(MathUtil.DEG_TO_RAD * 25.0f), 
					(float)Math.cos(MathUtil.DEG_TO_RAD * 27.0f), 
					1.0f);
			spot.setAttenuation(1.0f, 0.0f, 0.0005f);
			spot.setDiffuse(new Color(1.0f, 1.0f, 1.0f, 1.95f));
			lights.add(spot);
			slights.add(spot);			
		}//*/
		
		SpotLight spot = new SpotLight(
				new Vector3(0.0f, -2.0f, 0.0f),
				new Vector3(0.1f, -1.0f, 0.0f).normalize(),
				(float)Math.cos(MathUtil.DEG_TO_RAD * 30.0f), 
				(float)Math.cos(MathUtil.DEG_TO_RAD * 32.0f), 
				1.0f);
		spot.setAttenuation(1.0f, 0.0f, 0.0005f);
		spot.setDiffuse(new Color(1.0f, 1.0f, 1.0f, 0.55f));
		lights.add(spot);
		
		addInput(new InputAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == KeyEvent.VK_SPACE) {
					Mode[] m = Nessie.Mode.values();
					nessie.mode = m[(nessie.mode.ordinal() + 1) % m.length];
				}
				super.keyTyped(e);
			}
		});
		
		nessie.init();
	}
	
	float time;
	
	@Override
	public void display(GLAutoDrawable drawable) {
		super.display(drawable);
		
		box.getTransform().updateRotation(0.0f, 1.0f, 0.0f, time * 3);
		
		((DebugGUI)gui).info = "Testing deferred rendering. " + 
		 String.format("%d lights in the scene.", lights.size()) + "\n" +
		 "Rendering: " + nessie.mode.toString();
		
		time += Yeti.get().getDelta();
		l2.getPosition().x = (float)Math.sin(time) * 0.33f;
		
		for(SpotLight sl : slights) {
			double angle = Math.atan2(sl.getDirection().z, sl.getDirection().x);
			angle += Math.PI / 8 * (Yeti.get().getDelta());
			sl.getDirection().set(
					(float) Math.cos(angle),
					sl.getDirection().y,
					(float)  Math.sin(angle)
					);
			
			sl.getDiffuse().a = 0.4f + ((float)Math.sin(time * 10) + 1) / 2.0f * 0.4f;
		}
		
		//mainLight.getPosition().x = (float)Math.sin(time) * 30.0f;
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
