package barsan.opengl.scenes;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.input.CameraInput;
import barsan.opengl.input.InputAdapter;
import barsan.opengl.math.MathUtil;
import barsan.opengl.math.Quaternion;
import barsan.opengl.math.Transform;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Fog;
import barsan.opengl.rendering.Renderer.ShadowQuality;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.SkyBox;
import barsan.opengl.rendering.StaticModel;
import barsan.opengl.rendering.StaticModelInstance;
import barsan.opengl.rendering.lights.DirectionalLight;
import barsan.opengl.rendering.lights.Light.LightType;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.lights.SpotLight;
import barsan.opengl.rendering.materials.BasicMaterial;
import barsan.opengl.rendering.materials.BumpComponent;
import barsan.opengl.rendering.materials.GammaCorrection;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.rendering.materials.TextureComponent;
import barsan.opengl.resources.ModelLoader;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;
import barsan.opengl.util.DebugGUI;

public class LightTest extends Scene {

	StaticModelInstance plane, chosenOne;
	PointLight test_pl;
	SpotLight test_sl;
	DirectionalLight test_dl;

	LightType currentlyActive = LightType.Point;
	
	BumpComponent bc;
	GammaCorrection gammaCorrection;
	
	Material monkeyMat;
	Material skyMat;
	
	float lightX = 0.0f;
	float lightZ = 0.0f;
	float pointLightY = 4.0f;
	float linearAtt = 0.0f;

	Vector3 tv = new Vector3();
	float a = 0.0f;
	protected CameraInput cameraInput;
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		
		ResourceLoader.loadObj("monkey", "monkey.obj");
		ResourceLoader.loadObj("sphere", "sphere.obj");
		ResourceLoader.loadObj("texcube", "texcube.obj");
		ResourceLoader.loadTexture("floor", "floor.jpg");
		ResourceLoader.loadTexture("cubetex", "cubetex.png");
		ResourceLoader.loadTexture("floor.bump", "floor.bump.jpg");
		ResourceLoader.loadCubeTexture("test", "png");
		
		shadowsEnabled = true;
		
		StaticModel quad = ModelLoader.buildPlane(500.0f, 500.0f, 50, 50);
		monkeyMat = new BasicMaterial(new Color(0.0f, 0.0f, 1.0f));
		monkeyMat.setAmbient(new Color(0.05f, 0.05f, 0.10f));
		fog = new Fog(Color.TRANSPARENTBLACK);
		fog.fadeCamera(camera);
		camera.setPosition(new Vector3(0.0f, 20.0f, 0.0f));
		addInput(cameraInput = new CameraInput(camera));
		
		gammaCorrection = new GammaCorrection(1.2f);
		
		final Material floorMat = new BasicMaterial(new Color(1.0f, 1.0f, 1.0f));	
		floorMat.setTexture(ResourceLoader.texture("floor"));
		floorMat.addComponent(new TextureComponent());
		bc = new BumpComponent(ResourceLoader.texture("floor.bump"));
		floorMat.setAmbient(new Color(0.01f, 0.01f, 0.01f));
		floorMat.setShininess(128);
		
		SkyBox sb = new SkyBox(ResourceLoader.cubeTexture("test"), getCamera());
		skyMat = sb.getMaterial();
		modelInstances.add(sb);
		modelInstances.add(plane = new StaticModelInstance(quad, floorMat));
			
		BasicMaterial cmat = new BasicMaterial();
		cmat.setTexture(ResourceLoader.texture("cubetex"));
		cmat.addComponent(new TextureComponent());
		StaticModelInstance cube = new StaticModelInstance(ResourceLoader.model("texcube"), cmat);
		cube.getTransform().updateTranslate(2.0f, 2.5f, 0.0f);
		addModelInstance(cube);
		
		float step = 6.0f;
		int monkeys = 5;
		for(int i = -monkeys; i < monkeys; i++) {
			for(int j = -monkeys; j < monkeys; j++) {
				Transform pm = new Transform().setTranslate(i * step, 1.2f, j * step);
				pm.refresh();
				modelInstances.add(new StaticModelInstance(ResourceLoader.model("monkey"), monkeyMat, pm));
			}
		}//*/
		modelInstances.add(chosenOne = new StaticModelInstance(ResourceLoader.model("monkey"), monkeyMat, new Transform().updateScale(0.33f)));		
			
		modelInstances.add(new StaticModelInstance(ResourceLoader.model("sphere"), monkeyMat,
			new Transform().updateTranslate(28.0f, 30.0f, -4.0f).updateScale(4.0f)));
		
		modelInstances.add(new StaticModelInstance(ResourceLoader.model("sphere"), monkeyMat,
				new Transform().updateTranslate(35.0f, 25.0f, -10.0f).updateScale(6.0f)));
			
		
		test_sl = new SpotLight(new Vector3(0.0f, 12.0f, 1.5f), 
								new Vector3(1.0f, -1.0f, 0.0f).normalize(),
								0.85f, 0.9f, 1.0f);
		test_sl.setAttenuation(0.0f, 0.0f, 0.0f, 0.0f);
		test_sl.setDiffuse(new Color(0.55f, 0.55f, 0.55f));
		
		test_pl = new PointLight(new Vector3(lightX, pointLightY, lightZ));
		
		test_dl = new DirectionalLight(new Vector3(0.0f, -1.0f, 1.0f).normalize());
		lights.add(test_pl);
		
		gui = new DebugGUI(this, drawable.getAnimator());
		gui.setPosition(new Vector3(220, 10, 0));
		
		Yeti.get().addInputProvider(new InputAdapter() {
			
			int shadowQuality = 0;
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_SPACE) {
					if(floorMat.containsComponent(bc)) {
						floorMat.removeComponent(bc);
					} else {
						floorMat.addComponent(bc);
					}
				}
				else if(e.getKeyCode() == KeyEvent.VK_G) {
					if(floorMat.containsComponent(gammaCorrection)) {
						floorMat.removeComponent(gammaCorrection);
						monkeyMat.removeComponent(gammaCorrection);
						skyMat.removeComponent(gammaCorrection);
					} else {
						floorMat.addComponent(gammaCorrection);
						monkeyMat.addComponent(gammaCorrection);
						skyMat.addComponent(gammaCorrection);
					}
				} else if(e.getKeyCode() == KeyEvent.VK_Q) {
					Yeti.get().loadScene(new MenuScene());
				} else if(e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET) {
					ShadowQuality[] vals = ShadowQuality.values();
					renderer.setShadowQuality(vals[(shadowQuality = (shadowQuality - 1 + vals.length) % vals.length)]);
				} else if(e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET) {
					ShadowQuality[] vals = ShadowQuality.values();
					renderer.setShadowQuality(vals[(shadowQuality = (shadowQuality + 1) % vals.length)]);
				}
			}
		});
		
		linearAtt = 1f;
		Yeti.get().addInputProvider(new InputAdapter() {
			
			float dist = 50.0f;
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if((e.getModifiers() & MouseWheelEvent.CTRL_MASK) != 0) {
					dist -= e.getWheelRotation();
					if(dist < 1.0f) dist = 1.0f;
					if(dist > 50.0f) {
						dist = 50.0f;
						linearAtt = 0.0f;	// No more attenuation
					} else {
						linearAtt = 1.0f / dist;
					}
				} else {
					pointLightY -= (e.getWheelRotation() / 12.0f);
					test_pl.getPosition().setY(pointLightY);
				}
			}
		});
		
		Yeti.get().addInputProvider(new InputAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(e.getButton() != MouseEvent.BUTTON3) return;	// right-click only!
				
				lights.clear();
				currentlyActive = LightType.values()[(currentlyActive.ordinal() + 1) % LightType.values().length];
				switch(currentlyActive) {
				case Directional:
					lights.add(test_dl);
					break;
				case Point:
					lights.add(test_pl);
					break;
				case Spot:
					lights.add(test_sl);
					break;
				}
			}
		});
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		float delta = Yeti.get().getDelta();
		a += delta;
		
		test_sl.getDirection().x =  (float)Math.sin(a / 4) * 20.0f;
		test_sl.getDirection().z = -(float)Math.cos(a / 4) * 20.0f;
		test_sl.getDirection().y = -20.0f;
		test_sl.getDirection().normalize();
		
		test_sl.getPosition().setX((float)Math.cos(a / 2) * 40f);
		
		test_pl.getPosition().z = lightZ + (float)Math.cos(a) * 20.0f;
		test_pl.setAttenuation(0.0f, 0.0f, 0.005f, 0.0f);
		
		tv.set(1.0f, 1.0f, (float)Math.sin(a) * 1.5f);
		test_dl.getDirection().set(tv).normalize();

		float lx = (float)Math.cos(a) * 30.0f;
		chosenOne.getTransform().updateTranslate(lx, 2.5f, 0.0f).updateRotation(new Quaternion(new Vector3(0.0f, 1.0f, 0.0f), a * MathUtil.RAD_TO_DEG)).updateScale(0.75f);
		
		ShadowQuality sq = renderer.getShadowQuality();
		((DebugGUI)gui).info = String.format("Press [RMB] to cycle through light types\n" +
				"Use the [scrollwheel] to adjust the point light's Y.\n" +
				"[LBRACKET][RBRACKET] control shadow quality.\nCurrent: [%s] %s\n" +
				"[Space] toggles normal mapping\n" +
				"[G] toggles a reduced gamma-correction effect\n" +
				"[Q] returns to the PlanetHeadsMenu", sq.name(), sq.getDescription());
		
		super.display(drawable);
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
