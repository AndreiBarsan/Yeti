package barsan.opengl.scenes;

import java.io.IOException;

import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.BasicMaterial;
import barsan.opengl.rendering.Model;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.PointLight;
import barsan.opengl.rendering.Scene;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;

public class LightTest extends Scene {

	ModelInstance plane;
	PointLight testLight;
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		
		try {
			ResourceLoader.loadObj("monkey", "res/models/monkey.obj");
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		Model quad = Model.buildQuad(100.0f, 100.0f);
		BasicMaterial material = new BasicMaterial(new Color(1.0f, 0.33f, 0.33f), Color.WHITE);
		BasicMaterial monkeyMat = new BasicMaterial(new Color(0.0f, 0.00f, 1.0f), Color.WHITE);
		modelInstances.add(plane = new ModelInstance(quad, material, new Matrix4()));
		
		modelInstances.add(new ModelInstance(ResourceLoader.model("monkey"), monkeyMat, new Matrix4().setTranslate(0.0f, 2.0f, 0.0f)));
		//modelInstances.add(new ModelInstance(ResourceLoader.model("monkey"), monkeyMat, new Matrix4().setScale(20.0f)));
		
		pointLights.add(testLight = new PointLight(new Vector3(-35.0f, 5.0f, 0.0f)));
	}
	
	float a = 0.0f;
	@Override
	public void display(GLAutoDrawable drawable) {
		a += getDelta();
		//testLight.getPosition().z = -10 - (float)Math.sin(a * 2) * 20.0f;
		//testLight.getPosition().x = -10 + (float)Math.cos(a) * 20.0f;
		super.display(drawable);
	}
}
