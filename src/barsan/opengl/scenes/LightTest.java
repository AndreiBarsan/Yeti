package barsan.opengl.scenes;

import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.rendering.Model;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.Scene;

public class LightTest extends Scene {

	ModelInstance plane;
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		
		Model quad = Model.buildQuad(Yeti.get().gl, 1.0f, 1.0f);
		plane = new ModelInstance(quad);
	}
	
	
	@Override
	public void display(GLAutoDrawable drawable) {
		
		super.display(drawable);
	}
}
