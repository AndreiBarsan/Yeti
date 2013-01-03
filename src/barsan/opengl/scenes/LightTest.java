package barsan.opengl.scenes;

import java.io.IOException;

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
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;

public class LightTest extends Scene {

	ModelInstance plane, chosenOne;
	PointLight testLight;
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		
		try {
			ResourceLoader.loadObj("monkey", "res/models/monkey.obj");
			ResourceLoader.loadObj("sphere", "res/models/sphere.obj");
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		//Model quad = Model.buildQuad(100.0f, 100.0f);
		Model quad = new Cube(Yeti.get().gl, 100.0f);
		BasicMaterial material = new BasicMaterial(new Color(1.0f, 0.33f, 0.33f), Color.WHITE);
		BasicMaterial monkeyMat = new BasicMaterial(new Color(0.0f, 0.00f, 1.0f), Color.WHITE);
		modelInstances.add(plane = new ModelInstance(quad, material, new Matrix4()));
		//plane.getTransform().setTranslate(10.0f, 0.0f, 0.0f);
		plane.getTransform().setTranslate(0.0f, -50.0f, 0.0f);
		
		float step = 10.0f;
		for(int i = -5; i < 5; i++) {
			for(int j = -5; j < 5; j++) {
				modelInstances.add(new ModelInstance(ResourceLoader.model("monkey"), monkeyMat, new Matrix4().setTranslate(i * step, 2.0f, j * step)));
			}
		}
		modelInstances.add(chosenOne = new ModelInstance(ResourceLoader.model("sphere"), monkeyMat, new Matrix4().setScale(0.33f)));
		 
		
		pointLights.add(testLight = new PointLight(new Vector3(35.0f, 2.50f, 0.0f)));
	}
	
	float a = 0.0f;
	@Override
	public void display(GLAutoDrawable drawable) {
		a += getDelta();
		//testLight.getPosition().z = -10 - (float)Math.sin(a * 2) * 20.0f;
		float lx = -25 + (float)Math.cos(a) * 25.0f;
		testLight.getPosition().x = lx;
		chosenOne.getTransform().setTranslateScale(lx, 2.5f, 0.0f, 1.0f);
		super.display(drawable);
	}
}
