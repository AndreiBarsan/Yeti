package barsan.opengl.scenes;

import java.awt.event.KeyEvent;

import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.input.CameraInput;
import barsan.opengl.input.InputAdapter;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.Nessie;
import barsan.opengl.rendering.Nessie.Mode;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.StaticModelInstance;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.materials.BasicMaterial;
import barsan.opengl.resources.ModelLoader;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;
import barsan.opengl.util.DebugGUI;

public class NessieTestScene extends Scene {

	CameraInput cameraInput;
	PointLight mainLight;
	PointLight l2;
	ModelInstance box;
	Nessie nessie;
	
	@Override
	public void init(GLAutoDrawable drawable) {
		nessie = new Nessie(Yeti.get().gl);
		renderer = nessie;
		
		super.init(drawable);
		
		Yeti.get().gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		addInput(cameraInput = new CameraInput(camera));
		gui = new DebugGUI(this, drawable.getAnimator());
		gui.setPosition(10, 10);
		
		camera.setPosition(new Vector3(0.0f, 0.25f, -4.0f));
		camera.setDirection(new Vector3(0.0f, 0.0f, -1.0f));
		
		lights.add(mainLight = new PointLight(new Vector3(-0.25f, 0f, 0.0f), new Color(0.0f, 0.0f, 0.9f, 1.0f)));
		//lights.add(new PointLight(new Vector3(1.2f, -.1f, 1f), new Color(0.9f, 0.9f, 0.9f, 1.0f)));
		//lights.add(new PointLight(new Vector3(3f, 10.0f, 0.0f), new Color(0.9f, 0.9f, 0.9f, 1.0f)));
		mainLight.setAttenuation(1.0f, 2.5f, 1.0f, 0.0f);
		PointLight l = new PointLight(new Vector3(0.1f, 0.75f, 0.33f), new Color(1.0f, 1.0f, 1.0f, 1.0f));
		l.setAttenuation(1.0f, 0.25f, 1.0f, 0.0f);
		lights.add(l);
		
		l2 = new PointLight(new Vector3(-0.5f, 0.75f, -0.33f), new Color(1.0f, 1.0f, 0.9f, 0.44f));
		l2.setAttenuation(0.0f, 0.25f, 1.0f, 0.0f);
		lights.add(l2);
		
		ResourceLoader.loadObj("box", "texcube.obj");
		ResourceLoader.loadObj("monkey", "monkey.obj");
		ResourceLoader.loadObj("DR_sphere", "sphere.obj");
		ResourceLoader.loadTexture("cubetex", "cubetex.png");
		box = new StaticModelInstance(ResourceLoader.model("box"));
		box.getMaterial().setTexture(ResourceLoader.texture("cubetex"));
		box.getTransform().updateScale(4.0f).updateTranslate(2.0f, -2.5f, 0.0f);
		addModelInstance(box);
		
		BasicMaterial monkeyMat = new BasicMaterial(new Color(0.1f, 0.1f, 1.0f));
		addModelInstance(new StaticModelInstance(ResourceLoader.model("monkey"), monkeyMat));
		
		ModelInstance aux = new StaticModelInstance(ModelLoader.buildPlane(50.0f, 50.0f, 10, 10));
		aux.getTransform().updateTranslate(0.0f, -10.0f, 0.0f);
		addModelInstance(aux);
		
		camera.lookAt(new Vector3(10.0f, 10.0f, -12.0f), box.getTransform().getTranslate(), Vector3.UP.copy());
		
		addInput(new InputAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == KeyEvent.VK_SPACE) {
					Mode[] m = Nessie.Mode.values();
					nessie.mode = m[(nessie.mode.ordinal() + 1) % m.length];
				}
				super.keyTyped(e);
			}
		});
	}
	
	float time;
	
	@Override
	public void display(GLAutoDrawable drawable) {
		super.display(drawable);
		gui.render();
		box.getTransform().updateRotation(0.0f, 1.0f, 0.0f, time * 3);
		
		((DebugGUI)gui).info = "Testing deferred rendering. " + 
		 String.format("%d lights in the scene.", lights.size()) + "\n" +
		 "Rendering: " + nessie.mode.toString();
		
		time += Yeti.get().getDelta();
		l2.getPosition().x = (float)Math.sin(time) * 0.33f;
	}

	@Override
	public void play() {
		cameraInput.setMouseControlled(true);
	}
	
	@Override
	public void pause() {
		cameraInput.setMouseControlled(false);
	}
}
