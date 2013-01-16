package barsan.opengl.scenes;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;

import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.math.MathUtil;
import barsan.opengl.math.Quaternion;
import barsan.opengl.math.Transform;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.materials.BasicMaterial;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.rendering.materials.BasicMaterial.BumpComponent;
import barsan.opengl.rendering.Fog;
import barsan.opengl.rendering.Model;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.PointLight;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.SkyBox;
import barsan.opengl.rendering.SpotLight;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;

public class LightTest extends Scene {

	ModelInstance plane, chosenOne;
	PointLight testLight;
	SpotLight sl;
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
		
		Model quad = Model.buildPlane(500.0f, 500.0f, 100, 100);
		Material monkeyMat = new BasicMaterial(new Color(0.0f, 0.00f, 1.0f));
		monkeyMat.setAmbient(new Color(0.11f, 0.11f, 0.11f));
		
		//camera.setFrustumFar(150.0f);
		fog = new Fog(Color.TRANSPARENTBLACK);
		fog.fadeCamera(camera);
		//fogEnabled = true;
		
		final BasicMaterial floorMat = new BasicMaterial(new Color(1.0f, 1.0f, 1.0f));
		floorMat.setTexture(ResourceLoader.texture("floor"));
		bc = new BumpComponent(ResourceLoader.texture("floor.bump"));
		//floorMat.addComponent(bc);
		floorMat.setAmbient(new Color(0.1f, 0.1f, 0.1f));
		floorMat.setShininess(256);
		
		modelInstances.add(new SkyBox(Yeti.get().gl, ResourceLoader.cubeTexture("test"), getCamera()));
		
		modelInstances.add(plane = new ModelInstance(quad, floorMat));
		
		camera.setFrustumFar(3000.0f);
			
		float step = 10.0f;
		for(int i = -5; i < 5; i++) {
			for(int j = -5; j < 5; j++) {
				Transform pm = new Transform().setTranslate(i * step, 2.0f, j * step);
				float a = (float) (Math.PI - Math.atan2(lightZ - j * step, lightX - i * step));
				pm.setRotation(0.0f, 1.0f, 0.0f, MathUtil.RAD_TO_DEG * a);
				pm.refresh();
				modelInstances.add(new ModelInstance(ResourceLoader.model("monkey"), monkeyMat, pm));
			}
		}
		modelInstances.add(chosenOne = new ModelInstance(ResourceLoader.model("monkey"), monkeyMat, new Transform().updateScale(0.33f)));		
			
		sl = new SpotLight(new Vector3(0.0f, 2.50f, 0.0f), 
				new Vector3(-1.0f, -1.0f, 0.0f).normalize(),
				0.75f, 0.8f, 2.0f);
		sl.setDiffuse(new Color(0.95f, 0.95f, 0.95f));
		sl.setQuadraticAttenuation(0.003f);
		testLight = new PointLight(new Vector3(lightX, 2.50f, lightZ));
		pointLights.add(sl);
		//pointLights.add(testLight);
		
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
	}
	
	float a = 0.0f;
	@Override
	public void display(GLAutoDrawable drawable) {
		a += getDelta();
		float lx = -25 + (float)Math.cos(a) * 25.0f;
		
		sl.getDirection().x =  (float)Math.sin(a / 4) * 10.0f;
		sl.getDirection().z = -(float)Math.cos(a / 4) * 10.0f;
		
		testLight.getPosition().z = lightZ + (float)Math.cos(a) * 20.0f;
		testLight.setAttenuation(0.0f, linearAtt, 0.0f, 0.0f);
		
		//camera.setPosition(new Vector3(-10.0f, 8.0f, 0.0f));
		//camera.setDirection(new Vector3(camera.getPosition()).sub(testLight.getPosition().copy().setY(0.0f)).normalize());
		
		chosenOne.getTransform().updateTranslate(lx, 2.5f, 0.0f).updateRotation(new Quaternion(new Vector3(0.0f, 1.0f, 0.0f), a * MathUtil.RAD_TO_DEG)).updateScale(0.75f);
		super.display(drawable);
		
	}
}
