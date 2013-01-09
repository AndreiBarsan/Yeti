package barsan.opengl.scenes;

import java.io.IOException;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.math.MathUtil;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.BasicMaterial;
import barsan.opengl.rendering.Cube;
import barsan.opengl.rendering.Model;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.PointLight;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.SpotLight;
import barsan.opengl.rendering.BasicMaterial.BumpComponent;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;
import barsan.opengl.util.FPCameraAdapter;

public class LightTest extends Scene {

	ModelInstance plane, chosenOne;
	PointLight testLight;
	SpotLight sl;
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		
		try {
			ResourceLoader.loadObj("monkey", "res/models/monkey.obj");
			ResourceLoader.loadObj("sphere", "res/models/sphere.obj");
			
			ResourceLoader.loadTexture("floor", "res/tex/floor.jpg");
			ResourceLoader.loadTexture("floor.bump", "res/tex/floor.bump.jpg");
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		Model quad = Model.buildPlane(200.0f, 200.0f, 200, 200);
		BasicMaterial monkeyMat = new BasicMaterial(new Color(0.0f, 0.00f, 1.0f));
		monkeyMat.setAmbient(new Color(0.11f, 0.11f, 0.11f));
		
		BasicMaterial floorMat = new BasicMaterial(new Color(1.0f, 1.0f, 1.0f));
		floorMat.setTexture(ResourceLoader.texture("floor"));
		floorMat.addComponent(new BumpComponent(ResourceLoader.texture("floor.bump")));
		floorMat.setAmbient(new Color(0.1f, 0.1f, 0.1f));
		
		modelInstances.add(plane = new ModelInstance(quad, floorMat, new Matrix4()));
		
		float step = 10.0f;
		float lightX = -35.0f;
		float lightZ = 0.0f;
		for(int i = -5; i < 5; i++) {
			for(int j = -5; j < 5; j++) {
				Matrix4 pm = new Matrix4().setTranslate(i * step, 2.0f, j * step);
				float a = (float) (Math.PI - Math.atan2(lightZ - j * step, lightX - i * step));
				System.out.println(a);
				pm.mul(new Matrix4().setRotate( MathUtil.RAD_TO_DEG * a, 0, 1.0f, 0));
				modelInstances.add(new ModelInstance(ResourceLoader.model("monkey"), monkeyMat, pm));
			}
		}
		modelInstances.add(chosenOne = new ModelInstance(ResourceLoader.model("monkey"), monkeyMat, new Matrix4().setScale(0.33f)));
		 
		sl = new SpotLight(new Vector3(0.0f, 2.50f, 0.0f), 
				new Vector3(-1.0f, -1.0f, 0.0f).normalize(),
				0.75f, 0.9f, 1.0f);
		sl.setDiffuse(new Color(0.95f, 0.95f, 0.95f));
		testLight = new PointLight(new Vector3(lightX, 2.50f, lightZ));
		//pointLights.add(sl);
		pointLights.add(testLight);
	}
	
	float a = 0.0f;
	@Override
	public void display(GLAutoDrawable drawable) {
		a += getDelta() / 2;
		//testLight.getPosition().z = -10 - (float)Math.sin(a * 2) * 20.0f;
		float lx = -25 + (float)Math.cos(a) * 25.0f;
		
		sl.getDirection().x =  (float)Math.sin(a) * 10.0f;
		sl.getDirection().z = -(float)Math.cos(a) * 10.0f;
		
		chosenOne.getTransform().setTranslateScale(lx, 2.5f, 0.0f, 1.0f);
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
