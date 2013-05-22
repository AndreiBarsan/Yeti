package barsan.opengl.scenes;

import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.input.FreeflyCamera;
import barsan.opengl.input.InputAdapter;
import barsan.opengl.math.MathUtil;
import barsan.opengl.math.Vector2;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.Nessie;
import barsan.opengl.rendering.Nessie.Mode;
import barsan.opengl.rendering.Renderer.ShadowQuality;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.StaticModel;
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

	PointLight mainLight;
	PointLight l2;
	ModelInstance box;
	ModelInstance floor;
	Nessie nessie;

	PointLight plShadowTest;
	FreeflyCamera ffc;
	
	ArrayList<SpotLight> slights = new ArrayList<>();
	StaticModelInstance h;
	
	@Override
	public void init(GLAutoDrawable drawable) {
		nessie = new Nessie(Yeti.get().gl);
		renderer = nessie;

		super.init(drawable);
		shadowsEnabled = true;
		
		Yeti.get().gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		camera = ffc = new FreeflyCamera(this, Yeti.get().settings.width, Yeti.get().settings.height);
		camera.lookAt(new Vector3(-45.0f, 30.0f, -45.0f), new Vector3(0.0f, -10.0f, 0.0f), Vector3.UP.copy());		
		
		gui = new DebugGUI(this, drawable.getAnimator());
		gui.setPosition(10, 210);
		
		nessie.setShadowQuality(ShadowQuality.High);

		//*
		DirectionalLight dl = new DirectionalLight(new Vector3(-2.0f, -1.0f, -1.0f).normalize());
		dl.setCastsShadows(true);
		dl.getDiffuse().a = 0.50f;
		lights.add(dl);
		//*/
		
		ResourceLoader.loadObj("box", "texcube.obj");
		ResourceLoader.loadObj("monkey", "monkey.obj");
		ResourceLoader.loadObj("DR_sphere", "dr_icosphere.obj");
		ResourceLoader.loadObj("sphere", "sphere.obj");
		ResourceLoader.loadObj("DR_cone", "cone.obj");
		ResourceLoader.loadObj("LS", "LS.obj", 3);
		
		ResourceLoader.loadObj("hm", "The_Handyman.obj");
		
		ResourceLoader.loadTexture("cubetex", "cubetex.png");
		ResourceLoader.loadTexture("floor", "floor.jpg");
		ResourceLoader.loadTexture("floor.bump", "floor.bump.jpg");
		
		box = new StaticModelInstance(ResourceLoader.model("box"));
		box.getMaterial().setDiffuseMap(ResourceLoader.texture("cubetex"));
		box.getTransform().updateScale(4.0f).updateTranslate(2.0f, 10.5f, 0.0f);
		//addModelInstance(box);
		
		h = new StaticModelInstance(ResourceLoader.model("hm"));
		h.getTransform().updateTranslate(-15.0f, -10.0f, -18.0f);
		h.getTransform().updateRotation(0.0f, 1.0f, 0.0f, -90.0f);
		//	h.getTransform().updateScale(0.1f);
		
		addModelInstance(h);
		
		StaticModelInstance littleSister = new StaticModelInstance(ResourceLoader.model("LS"));
		littleSister.getTransform().updateScale(0.06f);
		addModelInstance(littleSister);

		floor = new StaticModelInstance(ModelLoader.buildPlane(
				100.0f, 100.0f, 10, 10));
		floor.getTransform().updateTranslate(0.0f, -12.0f, 0.0f);
		floor.getMaterial().setDiffuseMap(ResourceLoader.texture("floor"));
		floor.getMaterial().setNormalMap(ResourceLoader.texture("floor.bump"));
		floor.getMaterial().setSpecularIntensity(0.005f);
		floor.getMaterial().setSpecularPower(256);
		addModelInstance(floor);
		
		float wallZ = 6.0f;
		StaticModel wm = ModelLoader.buildPlane(100.0f, 10.0f, 10, 1);
		ModelInstance wall = new StaticModelInstance(wm);
		wall.getTransform().updateTranslate(0.0f, -12.0f, wallZ - 1.15f)
							.updateRotation(1.0f, 0.0f, 0.0f, -90.0f);
		wall.getMaterial().setNormalMap(ResourceLoader.texture("floor.bump"));
		wall.getMaterial().setSpecularIntensity(0.0f);
		addModelInstance(wall);
		
		wall = new StaticModelInstance(wm);
		wall.getTransform().updateTranslate(0.0f, -2.0f, wallZ + 1.15f)
							.updateRotation(1.0f, 0.0f, 0.0f, 90.0f);
		wall.getMaterial().setNormalMap(ResourceLoader.texture("floor.bump"));
		wall.getMaterial().setSpecularIntensity(0.0f);
		addModelInstance(wall);
		
		int mlim = 2;
		float mGrid = 2.5f;
		//*
		for (int i = -mlim; i < mlim; ++i) {
			for (int j = -mlim; j < mlim; ++j) {
				Material mat = new BasicMaterial(Color.random());
				mat.setSpecularIntensity(4.0f);
				mat.setSpecularPower(64);
				StaticModelInstance monkey = new StaticModelInstance(
						ResourceLoader.model("sphere"), mat);
				monkey.getTransform().updateTranslate(i * mGrid, -8.5f, j * mGrid);
				addModelInstance(monkey);
			}
		}// */
		
		/*
		float sz = mGrid * (mlim + 1) * 2;
		nessie.setDirectionalShadowSize(new Vector2(sz, sz));
		nessie.setDirectionalShadowCenter(new Vector3(-sz / 2, 0, 0));
		*/
		
		nessie.setDirectionalShadowSize(new Vector2(120, 120));
		nessie.setDirectionalShadowDepth(new Vector2(-80, 150));
		
		//*
		int lightLim = 3;
		float lgs = 18.0f;
		for(int i = -lightLim; i < lightLim; ++i) {
			for(int j = -lightLim; j < lightLim; ++j) {
				Color c = Color.random();
				c.a = 16.0f;
				PointLight light = new PointLight(new Vector3(i * lgs, -6.0f, j * lgs), c);
				light.setAttenuation(0.0f, 0.0f, 1.5f);
				lights.add(light);
			}
		}//*/

		plShadowTest = new PointLight(new Vector3(4, -3.0f, -22.0f), Color.WHITE.copy());
		plShadowTest.setAttenuation(0.0f, 0.0f, 0.1f);
		plShadowTest.getDiffuse().a = 2.0f;
		plShadowTest.setCastsShadows(true);
		//lights.add(plShadowTest);
		
		
		/*
		int al = 6;
		float sector = ((float) Math.PI * 2.0f) / al;
		for (int i = 0; i < al; ++i) {
			SpotLight spot = new SpotLight(
					new Vector3(-(float)Math.cos(i * sector) * 2.0f,
							-3.0f,
							(float)Math.sin(i * sector) * 2.0f),
					
					new Vector3(-(float) Math.cos(i * sector),
							-1.0f,
							(float) Math.sin(i * sector)).normalize(),
					(float) Math.cos(MathUtil.DEG_TO_RAD * 10.0f),
					(float) Math.cos(MathUtil.DEG_TO_RAD * 15.0f), 1.0f);
			spot.setAttenuation(1.0f, 0.0f, 0.0005f);
			spot.setDiffuse(new Color(1.0f, 1.0f, 1.0f, 0.95f));
			lights.add(spot);
			spot.setCastsShadows(true);
			slights.add(spot);
		}// */

		SpotLight spot = new SpotLight(new Vector3(20.0f, 8.0f, -18.0f),
				new Vector3(-8.0f, -5.0f, 0.0f).normalize(),
				(float) Math.cos(MathUtil.DEG_TO_RAD * 25.0f),
				(float) Math.cos(MathUtil.DEG_TO_RAD * 30.0f), 1.0f);
		spot.setAttenuation(1.0f, 0.0f, 0.0005f);
		spot.setDiffuse(new Color(1.0f, 1.0f, 1.0f, 3.0f));
		lights.add(spot);
		spot.setCastsShadows(true);

		addInput(new InputAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.isConsumed()) {
					return;
				}
			}
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int step = e.getWheelRotation();
				if(e.isShiftDown()) {
					nessie.AO().sampleRad -= step * 0.005f;
					if(nessie.AO().sampleRad < 0.0f) {
						nessie.AO().sampleRad = 0.0f;
					}
					System.out.println("Sample radius: " + nessie.AO().sampleRad);
					
				} else if(e.isControlDown()) {
					nessie.AO().scale -= step * 0.033f;
					if(nessie.AO().scale < 0.0f) {
						nessie.AO().scale = 0.0f;
					}
					System.out.println("AO Scale: " + nessie.AO().scale);
					
				} else {
					nessie.AO().intensity -= step * 0.033f;
					if(nessie.AO().intensity < 0.0f)  {
						nessie.AO().intensity = 0.0f;
					}
					System.out.println("AO Intensity: " + nessie.AO().intensity);
				}
				
			}
		});
		
		addInput(new InputAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.isConsumed()) {
					return;
				}
				
				if (e.getKeyChar() == KeyEvent.VK_SPACE) {
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
		
		h.getTransform().updateRotation(0.0f, 1.0f, 0.0f, time * 10);
		
		super.display(drawable);
		
		ffc.update(Yeti.get().getDelta());

		box.getTransform().updateRotation(0.0f, 1.0f, 0.0f, time * 3);

		((DebugGUI) gui).info = "Testing deferred rendering. "
				+ String.format("%d lights in the scene.", lights.size())
				+ "\n" + "Rendering: " + nessie.mode.toString();

		time += Yeti.get().getDelta();
		
		plShadowTest.getPosition().x = (float)Math.sin(time) * 20.5f;

		//*
		for (SpotLight sl : slights) {
			//double angle = Math.atan2(sl.getDirection().z, sl.getDirection().x);
			//angle += Math.PI / 8 * (Yeti.get().getDelta());
			//sl.getDirection().set((float) Math.cos(angle), sl.getDirection().y,
			//		(float) Math.sin(angle));
			
			sl.getPosition().x = (float)Math.sin(time) * 8.0f;
			double aa = (35 + Math.sin(time) * 15);
			sl.setCosOuter((float)Math.cos( aa * MathUtil.DEG_TO_RAD));
			sl.setCosInner((float)Math.cos( (aa - 2.0f) * MathUtil.DEG_TO_RAD));
		}
		 	//*/
		// mainLight.getPosition().x = (float)Math.sin(time) * 30.0f;
	}

	@Override
	public void play() {
		ffc.setMouseControlled(true);
	}

	@Override
	public void pause() {
		ffc.setMouseControlled(false);
	}
}
