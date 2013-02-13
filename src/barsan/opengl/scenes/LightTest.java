package barsan.opengl.scenes;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;

import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.math.MathUtil;
import barsan.opengl.math.Quaternion;
import barsan.opengl.math.Transform;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Fog;
import barsan.opengl.rendering.StaticModel;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.SkyBox;
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
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		
		try {
			ResourceLoader.loadObj("monkey", "res/models/monkey.obj");
			ResourceLoader.loadObj("sphere", "res/models/sphere.obj");
			
			ResourceLoader.loadTexture("floor", "res/tex/floor.jpg");
			ResourceLoader.loadTexture("floor.bump", "res/tex/floor.bump.jpg");
			
			ResourceLoader.loadCubeTexture("test", "png");
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		shadowsEnabled = true;
		
		StaticModel quad = ModelLoader.buildPlane(500.0f, 500.0f, 50, 50);
		monkeyMat = new BasicMaterial(new Color(0.0f, 0.0f, 1.0f));
		monkeyMat.setAmbient(new Color(0.05f, 0.05f, 0.10f));
		fog = new Fog(Color.TRANSPARENTBLACK);
		fog.fadeCamera(camera);
		//fogEnabled = true;
		
		gammaCorrection = new GammaCorrection(1.2f);
		
		final Material floorMat = new BasicMaterial(new Color(1.0f, 1.0f, 1.0f));	
		floorMat.setTexture(ResourceLoader.texture("floor"));
		floorMat.addComponent(new TextureComponent());
		bc = new BumpComponent(ResourceLoader.texture("floor.bump"));
		floorMat.setAmbient(new Color(0.01f, 0.01f, 0.01f));
		floorMat.setShininess(256);
		
		
		SkyBox sb = new SkyBox(ResourceLoader.cubeTexture("test"), getCamera());
		skyMat = sb.getMaterial();
		modelInstances.add(sb);
		modelInstances.add(plane = new StaticModelInstance(quad, floorMat));
			
		float step = 6.0f;
		int monkeys = 4;
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
		test_sl.setDiffuse(new Color(0.55f, 0.55f, 0.55f));
		
		test_pl = new PointLight(new Vector3(lightX, pointLightY, lightZ));
		
		test_dl = new DirectionalLight(new Vector3(0.0f, -1.0f, 1.0f).normalize());
		lights.add(test_pl);
		
		gui = new DebugGUI(drawable.getAnimator(), camera);
		gui.setPosition(new Vector3(220, 10, 0));
		((DebugGUI)gui).info = "Press [RMB] to cycle through light types\n" +
				"Use the [scrollwheel] to adjust the point light's Y.\n" +
				"[Space] toggles normal mapping\n" +
				"[G] toggles a reduced gamma-correction effect";
		
		Yeti.get().addKeyListener(new KeyAdapter() {
			
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
				}
			}
		});
		
		linearAtt = 1f;
		Yeti.get().addMouseWheelListener(new MouseWheelListener() {
			
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
		
		Yeti.get().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(e.getButton() != MouseEvent.BUTTON3) return;	// right-click only!
				
				synchronized(lights) {
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
			}
		});
	}
	
	Vector3 tv = new Vector3();
	float a = 0.0f;
	@Override
	public void display(GLAutoDrawable drawable) {
		a += getDelta();
		
		test_sl.getDirection().x =  (float)Math.sin(a / 4) * 20.0f;
		test_sl.getDirection().z = -(float)Math.cos(a / 4) * 20.0f;
		test_sl.getDirection().y = -20.0f;
		test_sl.getDirection().normalize();
		
		test_sl.getPosition().setX((float)Math.cos(a / 2) * 40f);
		
		test_pl.getPosition().z = lightZ + (float)Math.cos(a) * 20.0f;
		test_pl.setAttenuation(0.0f, 0.0f, 0.005f, 0.0f);
		
		tv.set(1.0f, 1.0f, (float)Math.sin(a) * 1.5f);
		test_dl.getDirection().set(tv).normalize();

		float lx = -25 + (float)Math.cos(a) * 25.0f;
		chosenOne.getTransform().updateTranslate(lx, 2.5f, 0.0f).updateRotation(new Quaternion(new Vector3(0.0f, 1.0f, 0.0f), a * MathUtil.RAD_TO_DEG)).updateScale(0.75f);
		super.display(drawable);
	}
}
