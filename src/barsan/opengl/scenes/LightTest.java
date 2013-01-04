package barsan.opengl.scenes;

import java.io.IOException;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.BasicMaterial;
import barsan.opengl.rendering.Cube;
import barsan.opengl.rendering.Model;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.PointLight;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.SpotLight;
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
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		Model quad = Model.buildQuad(200.0f, 200.0f);
		//Model quad = new Cube(Yeti.get().gl, 100.0f);
		BasicMaterial material = new BasicMaterial(new Color(1.0f, 0.33f, 0.33f));
		BasicMaterial monkeyMat = new BasicMaterial(new Color(0.0f, 0.00f, 1.0f));
		modelInstances.add(plane = new ModelInstance(quad, material, new Matrix4()));
		//plane.getTransform().setTranslate(10.0f, 0.0f, 0.0f);
		
		float step = 10.0f;
		for(int i = -5; i < 5; i++) {
			for(int j = -5; j < 5; j++) {
				modelInstances.add(new ModelInstance(ResourceLoader.model("monkey"), monkeyMat, new Matrix4().setTranslate(i * step, 2.0f, j * step)));
			}
		}
		modelInstances.add(chosenOne = new ModelInstance(ResourceLoader.model("sphere"), monkeyMat, new Matrix4().setScale(0.33f)));
		 
		sl = new SpotLight(new Vector3(0.0f, 2.50f, 0.0f), 
				new Vector3(-1.0f, -1.0f, 0.0f).normalize(),
				0.75f, 0.9f, 1.0f);
		testLight = new PointLight(new Vector3(35.0f, 2.50f, 0.0f));
		pointLights.add(sl);
		//pointLights.add(testLight);
	}
	
	float a = 0.0f;
	@Override
	public void display(GLAutoDrawable drawable) {
		a += getDelta();
		//testLight.getPosition().z = -10 - (float)Math.sin(a * 2) * 20.0f;
		float lx = -25 + (float)Math.cos(a) * 25.0f;
		testLight.getPosition().x = lx;
		
		sl.getDirection().x =  (float)Math.sin(a) * 10.0f;
		sl.getDirection().z = -(float)Math.cos(a) * 10.0f;
		
		chosenOne.getTransform().setTranslateScale(lx, 2.5f, 0.0f, 1.0f);
		super.display(drawable);
		
		GL2 gl = Yeti.get().gl;
		FPCameraAdapter ca = new FPCameraAdapter(camera);
		ca.prepare(gl);
		gl.glBegin(GL2.GL_TRIANGLES);
			// helper stuff
		gl.glEnd();
		gl.glPopMatrix();
	}
}
