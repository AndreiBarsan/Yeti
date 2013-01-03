package barsan.opengl.scenes;

import java.io.IOException;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.AmbientLight;
import barsan.opengl.rendering.BasicMaterial;
import barsan.opengl.rendering.Cube;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.PointLight;
import barsan.opengl.rendering.Scene;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;

import com.jogamp.opengl.util.gl2.GLUT;

public class ModelGraphScene extends Scene {
	
	long start = System.currentTimeMillis();
	ModelInstance s1, s2, s3;

	PointLight light = new PointLight(new Vector3(0, 10, 0));
	AmbientLight ambientLight = new AmbientLight(Color.WHITE);
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		
		BasicMaterial red = new BasicMaterial(new Color(0.9f, 0.2f, 0.4f, 1.0f));
		BasicMaterial blue = new BasicMaterial(new Color(0.4f, 0.3f, 0.9f, 1.0f));
		BasicMaterial yellow = new BasicMaterial(new Color(0.8f, 0.9f, 0.2f, 1.0f));
		
		GL2 gl = Yeti.get().gl;
		
		try {
			ResourceLoader.loadObj("bunny", "res/models/bunny.obj");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		s1 = new ModelInstance(
				new Cube(gl, 1.0f),
				red,
				new Matrix4()
				);
		modelInstances.add(s1);
		
		s2 =  new ModelInstance(
				ResourceLoader.model("bunny"),
				blue,
				new Matrix4()
				);
		s1.addChild(s2);
		
		s3 = new ModelInstance(
				new Cube(gl, 2.0f),
			yellow,
			new Matrix4().setTranslate(0.0f, 3.0f, 0.0f)
		);
		modelInstances.add(s3);
		
		
		camera.setPosition(new Vector3(0.0f, 0.0f, -4.0f));
		camera.setDirection(new Vector3(0.0f, 0.0f, -1.0f).normalize());
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		super.display(drawable);
	}
}
