package barsan.opengl.scenes;

import java.io.IOException;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Transform;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Cube;
import barsan.opengl.rendering.StaticModelInstance;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.lights.AmbientLight;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.materials.BasicMaterial;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;

public class ModelGraphScene extends Scene {
	
	long start = System.currentTimeMillis();
	StaticModelInstance s1, s2, s3;

	PointLight light = new PointLight(new Vector3(0, 10, 0));
	AmbientLight ambientLight = new AmbientLight(Color.WHITE);
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		
		Material red = new BasicMaterial(new Color(0.9f, 0.2f, 0.4f, 1.0f));
		Material blue = new BasicMaterial(new Color(0.4f, 0.3f, 0.9f, 1.0f));
		Material yellow = new BasicMaterial(new Color(0.8f, 0.9f, 0.2f, 1.0f));
		
		GL2 gl = Yeti.get().gl.getGL2();
		
		try {
			ResourceLoader.loadObj("bunny", "bunny.obj");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		s1 = new StaticModelInstance(
				new Cube(gl, 1.0f),
				red
				);
		modelInstances.add(s1);
		
		s2 =  new StaticModelInstance(
				ResourceLoader.model("bunny"),
				blue
				);
		s1.addChild(s2);
		
		s3 = new StaticModelInstance(
				new Cube(gl, 2.0f),
			yellow,
			new Transform().updateTranslate(0.0f, 3.0f, 0.0f)
		);
		modelInstances.add(s3);
		
		camera.setPosition(new Vector3(0.0f, 0.0f, -4.0f));
		camera.setDirection(new Vector3(0.0f, 0.0f, -1.0f).normalize());
		
		lights.add(light);
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		super.display(drawable);
	}
}
