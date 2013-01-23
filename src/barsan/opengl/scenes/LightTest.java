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
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Quaternion;
import barsan.opengl.math.Transform;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Fog;
import barsan.opengl.rendering.Model;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.PerspectiveCamera;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.SkyBox;
import barsan.opengl.rendering.lights.DirectionalLight;
import barsan.opengl.rendering.lights.Light.LightType;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.lights.SpotLight;
import barsan.opengl.rendering.materials.BasicMaterial;
import barsan.opengl.rendering.materials.BumpComponent;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.rendering.materials.ShadowReceiver;
import barsan.opengl.rendering.materials.TextureComponent;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;
import barsan.opengl.util.DebugGUI;

public class LightTest extends Scene {

	ModelInstance plane, chosenOne;
	PointLight test_pl;
	SpotLight test_sl;
	DirectionalLight test_dl;

	LightType currentlyActive = LightType.Point;
	
	BumpComponent bc;
	
	float lightX = 0.0f;
	float lightZ = 0.0f;
	
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
		
		Model quad = Model.buildPlane(500.0f, 500.0f, 50, 50);
		Material monkeyMat = new BasicMaterial(new Color(0.0f, 0.00f, 1.0f));
		monkeyMat.setAmbient(new Color(0.11f, 0.11f, 0.11f));
		monkeyMat.addComponent(new ShadowReceiver());
		//camera.setFrustumFar(180.0f);
		fog = new Fog(Color.TRANSPARENTBLACK);
		fog.fadeCamera(camera);
		//fogEnabled = true;
		
		
		final Material floorMat = new BasicMaterial(new Color(1.0f, 1.0f, 1.0f));
		floorMat.setTexture(ResourceLoader.texture("floor"));
		bc = new BumpComponent(ResourceLoader.texture("floor.bump"));
		floorMat.setAmbient(new Color(0.1f, 0.1f, 0.1f));
		floorMat.setShininess(256);
		floorMat.addComponent(new ShadowReceiver());
		floorMat.addComponent(new TextureComponent());
		
		modelInstances.add(new SkyBox(Yeti.get().gl.getGL2(), ResourceLoader.cubeTexture("test"), getCamera()));
		modelInstances.add(plane = new ModelInstance(quad, floorMat));
			
		float step = 6.0f;
		int monkeys = 4;
		for(int i = -monkeys; i < monkeys; i++) {
			for(int j = -monkeys; j < monkeys; j++) {
				Transform pm = new Transform().setTranslate(i * step, 1.2f, j * step);
				//float a = (float) (Math.PI - Math.atan2(lightZ - j * step, lightX - i * step));
				//pm.setRotation(0.0f, 1.0f, 0.0f, MathUtil.RAD_TO_DEG * a);
				pm.refresh();
				modelInstances.add(new ModelInstance(ResourceLoader.model("monkey"), monkeyMat, pm));
			}
		}
		modelInstances.add(chosenOne = new ModelInstance(ResourceLoader.model("monkey"), monkeyMat, new Transform().updateScale(0.33f)));		
			
		modelInstances.add(new ModelInstance(ResourceLoader.model("sphere"), monkeyMat,
			new Transform().updateTranslate(28.0f, 30.0f, -4.0f).updateScale(4.0f)));
		
		modelInstances.add(new ModelInstance(ResourceLoader.model("sphere"), monkeyMat,
				new Transform().updateTranslate(35.0f, 25.0f, -10.0f).updateScale(6.0f)));
			
		
		test_sl = new SpotLight(new Vector3(0.0f, 12.0f, 1.5f), 
				new Vector3(1.0f, -1.0f, 0.0f).normalize(),
				0.75f, 0.8f, 2.0f);
		test_sl.setDiffuse(new Color(0.95f, 0.95f, 0.95f));
		//test_sl.setQuadraticAttenuation(0.001f);
		test_sl.setLinearAttenuation(0.05f);
		
		test_pl = new PointLight(new Vector3(lightX, 1.50f, lightZ));
		
		test_dl = new DirectionalLight(new Vector3(0.0f, -1.0f, 1.0f).normalize());
		//lights.add(test_dl);
		lights.add(test_sl);
		
		gui = new DebugGUI(drawable.getAnimator(), camera);
		gui.setPosition(new Vector3(220, 10, 0));
		
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
			}
		});
		
		linearAtt = 1.0f;
		Yeti.get().addMouseWheelListener(new MouseWheelListener() {
			
			float dist = 1.0f;
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				dist -= e.getWheelRotation();
				if(dist < 1.0f) dist = 1.0f;
				if(dist > 100.0f) {
					dist = 100.0f;
					linearAtt = 0.0f;	// No more attenuation
				} else {
					linearAtt = 1.0f / dist;
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
		float lx = -25 + (float)Math.cos(a) * 25.0f;
		
		//test_sl.getDirection().x =  (float)Math.sin(a / 4) * 20.0f;
		//test_sl.getDirection().z = -(float)Math.cos(a / 4) * 20.0f;
		//test_sl.getDirection().y = -20.0f;
		//test_sl.getDirection().normalize();
		
		test_sl.getPosition().setX((float)Math.cos(a / 2) * 40f);
		
		test_pl.getPosition().z = lightZ + (float)Math.cos(a) * 20.0f;
		test_pl.setAttenuation(0.0f, linearAtt, 0.0f, 0.0f);
		
		//tv.set(1.0f, 1.0f, 4.0f);
		tv.set(1.0f, 1.0f, (float)Math.sin(a / 2.5f) * 3.0f);
		test_dl.getDirection().set(tv).normalize();
		
		chosenOne.getTransform().updateTranslate(lx, 2.5f, 0.0f).updateRotation(new Quaternion(new Vector3(0.0f, 1.0f, 0.0f), a * MathUtil.RAD_TO_DEG)).updateScale(0.75f);
		
		//plane.getTransform().getTranslate().y = -1.0f + (float)Math.sin(a) * 2.5f;
		//plane.getTransform().refresh();
		
		/*
		((PerspectiveCamera)camera).setFOV(90.0f);
		
		camera.setPosition(test_sl.getPosition());
		camera.setDirection(test_sl.getDirection());
		//*/
		super.display(drawable);
	}
}
