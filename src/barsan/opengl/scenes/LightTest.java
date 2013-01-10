package barsan.opengl.scenes;

import java.awt.RenderingHints.Key;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.math.MathUtil;
import barsan.opengl.math.Transform;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.BasicMaterial;
import barsan.opengl.rendering.BasicMaterial.BumpComponent;
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
		BasicMaterial monkeyMat = new BasicMaterial(new Color(0.0f, 0.00f, 1.0f));
		monkeyMat.setAmbient(new Color(0.11f, 0.11f, 0.11f));
		
		final BasicMaterial floorMat = new BasicMaterial(new Color(1.0f, 1.0f, 1.0f));
		floorMat.setTexture(ResourceLoader.texture("floor"));
		bc = new BumpComponent(ResourceLoader.texture("floor.bump"));
		floorMat.addComponent(bc);
		floorMat.setAmbient(new Color(0.1f, 0.1f, 0.1f));
		
		modelInstances.add(new SkyBox(Yeti.get().gl, ResourceLoader.cubeTexture("test"), getCamera()));
		
		modelInstances.add(plane = new ModelInstance(quad, floorMat));
		
		float step = 10.0f;
		float lightX = -35.0f;
		float lightZ = 0.0f;
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
	}
	
	float a = 0.0f;
	@Override
	public void display(GLAutoDrawable drawable) {
		a += getDelta() / 2;
		//testLight.getPosition().z = -10 - (float)Math.sin(a * 2) * 20.0f;
		float lx = -25 + (float)Math.cos(a) * 25.0f;
		
		sl.getDirection().x =  (float)Math.sin(a) * 10.0f;
		sl.getDirection().z = -(float)Math.cos(a) * 10.0f;
		
		chosenOne.getTransform().updateTranslate(lx, 2.5f, 0.0f);
		super.display(drawable);
		
		GL2 gl = Yeti.get().gl;
		FPCameraAdapter ca = new FPCameraAdapter(camera);
		ca.prepare(gl);
		gl.glBegin(GL2.GL_TRIANGLES);
			// helper stuff; TODO
		gl.glEnd();
		gl.glPopMatrix();
	}
}
