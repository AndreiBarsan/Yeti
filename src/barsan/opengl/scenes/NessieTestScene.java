package barsan.opengl.scenes;

import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.input.CameraInput;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.Nessie;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.StaticModelInstance;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.materials.BasicMaterial;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;
import barsan.opengl.util.DebugGUI;

public class NessieTestScene extends Scene {

	CameraInput cameraInput;
	PointLight mainLight;
	PointLight l2;
	ModelInstance box;
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		renderer = new Nessie(Yeti.get().gl);
		Yeti.get().gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		addInput(cameraInput = new CameraInput(camera));
		gui = new DebugGUI(this, drawable.getAnimator());
		
		camera.setPosition(new Vector3(0.0f, 0.25f, -4.0f));
		camera.setDirection(new Vector3(0.0f, 0.0f, -1.0f));
		
		lights.add(mainLight = new PointLight(new Vector3(-0.25f, 0f, 0.0f), new Color(0.0f, 0.0f, 0.9f, 1.0f)));
		lights.add(new PointLight(new Vector3(1.2f, -.1f, 1f), new Color(0.9f, 0.9f, 0.9f, 1.0f)));
		lights.add(new PointLight(new Vector3(3f, 10.0f, 0.0f), new Color(0.9f, 0.9f, 0.9f, 1.0f)));
		PointLight l = new PointLight(new Vector3(0.1f, 0.75f, 0.33f), new Color(1.0f, 1.0f, 1.0f, 1.0f));
		l.setAttenuation(0.0f, 0.25f, 15.0f, 0.0f);
		lights.add(l);
		
		l2 = new PointLight(new Vector3(-0.5f, 0.75f, -0.33f), new Color(1.0f, 1.0f, 0.9f, 0.44f));
		l.setAttenuation(0.0f, 0.25f, 15.0f, 0.0f);
		lights.add(l2);
		
		ResourceLoader.loadObj("box", "texcube.obj");
		ResourceLoader.loadObj("monkey", "monkey.obj");
		ResourceLoader.loadObj("DR_sphere", "sphere.obj");
		ResourceLoader.loadTexture("cubetex", "cubetex.png");
		box = new StaticModelInstance(ResourceLoader.model("box"));
		box.getTransform().updateScale(4.0f).updateTranslate(2.0f, -2.5f, 0.0f);
		addModelInstance(box);
		
		BasicMaterial monkeyMat = new BasicMaterial(new Color(0.1f, 0.1f, 1.0f));
		addModelInstance(new StaticModelInstance(ResourceLoader.model("monkey"), monkeyMat));
		
		camera.lookAt(new Vector3(-3.0f, 4.0f, -3.0f), box.getTransform().getTranslate(), Vector3.UP.copy());
	}
	
	float time;
	
	@Override
	public void display(GLAutoDrawable drawable) {
		super.display(drawable);
		box.getTransform().updateRotation(0.0f, 1.0f, 0.0f, time * 3);
		((DebugGUI)gui).info = "Testing deferred rendering. " + 
		 String.format("%d lights in the scene.", lights.size());
		time += Yeti.get().getDelta();
		l2.getPosition().x = (float)Math.sin(time) * 0.33f;
	}
}
