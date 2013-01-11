package barsan.opengl.scenes;

import java.awt.RenderingHints.Key;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import com.jogamp.opengl.util.gl2.GLUT;

import barsan.opengl.Yeti;
import barsan.opengl.math.MathUtil;
import barsan.opengl.math.Transform;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.BasicMaterial;
import barsan.opengl.rendering.BasicMaterial.BumpComponent;
import barsan.opengl.rendering.Fog;
import barsan.opengl.rendering.Material;
import barsan.opengl.rendering.Model;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.PointLight;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.SkyBox;
import barsan.opengl.rendering.SpotLight;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;
import barsan.opengl.util.FPCameraAdapter;

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
		
		Model quad = Model.buildPlane(200.0f, 200.0f, 50, 50);
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
		
		modelInstances.add(new SkyBox(Yeti.get().gl, ResourceLoader.cubeTexture("test"), getCamera()));
		
		modelInstances.add(plane = new ModelInstance(quad, floorMat));
		
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
				0.75f, 0.9f, 1.0f);
		sl.setDiffuse(new Color(0.95f, 0.95f, 0.95f));
		testLight = new PointLight(new Vector3(lightX, 2.50f, lightZ));
		//pointLights.add(sl);
		pointLights.add(testLight);
		
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
		
		sl.getDirection().x =  (float)Math.sin(a) * 10.0f;
		sl.getDirection().z = -(float)Math.cos(a) * 10.0f;
		testLight.getPosition().z = lightZ + (float)Math.cos(a) * 20.0f;
		testLight.setAttenuation(0.0f, linearAtt, 0.0f, 0.0f);
		
		//camera.setPosition(new Vector3(-10.0f, 8.0f, 0.0f));
		//camera.setDirection(new Vector3(camera.getPosition()).sub(testLight.getPosition().copy().setY(0.0f)).normalize());
		
		chosenOne.getTransform().updateTranslate(lx, 2.5f, 0.0f);
		super.display(drawable);
		
	}
}
