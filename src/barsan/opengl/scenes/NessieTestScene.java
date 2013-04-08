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
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;
import barsan.opengl.util.DebugGUI;

public class NessieTestScene extends Scene {

	CameraInput cameraInput;
	PointLight mainLight;
	
	@Override
	public void init(GLAutoDrawable drawable) {
		renderer = new Nessie(Yeti.get().gl);
		
		super.init(drawable);
		
		//addInput(cameraInput = new CameraInput(camera));
		gui = new DebugGUI(this, drawable.getAnimator());
		
		camera.setPosition(new Vector3(0.0f, 0.25f, -4.0f));
		camera.setDirection(new Vector3(0.0f, 0.0f, -1.0f));
		lights.add(mainLight = new PointLight(new Vector3(0f, 20f, 10f), new Color(0.9f, 0.9f, 0.9f, 1.0f)));
		lights.add(new PointLight(new Vector3(0f, 50f, 10f), new Color(0.9f, 0.9f, 0.9f, 1.0f)));
		lights.add(new PointLight(new Vector3(0f, 30f, 20f), new Color(1.0f, 0.0f, 0.9f, 1.0f)));
		
		ResourceLoader.loadObj("monkey", "monkey.obj");
		ModelInstance monkey = new StaticModelInstance(ResourceLoader.model("monkey"));
		addModelInstance(monkey);
		
		camera.lookAt(Vector3.ZERO.copy(), monkey.getTransform().getTranslate(), Vector3.UP.copy());
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		super.display(drawable);
		
		((DebugGUI)gui).info = "Testing deferred rendering. " + 
		 String.format("%d lights in the scene.", lights.size());
	}
}
