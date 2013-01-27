package barsan.opengl.rendering.materials;

import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;

/**
 * @author Andrei Barsan
 */
public class BasicMaterial extends Material {	
	
	// TODO: consistent uniform names to ease automatic material management in the future
	
	static final String PHONG_NAME 		= "phong";
	
	static final Color blank = new Color(0.0f, 0.0f, 0.0f, 0.0f);
	
	public BasicMaterial(Color diffuse) {
		this(Color.WHITE, diffuse, Color.WHITE);
	}

	public BasicMaterial() {
		this(Color.WHITE, Color.WHITE, Color.WHITE);
	}
	
	public BasicMaterial(Color ambient, Color diffuse, Color specular) {
		super(ResourceLoader.shader(PHONG_NAME), ambient, diffuse, specular);
		
		addComponent(new WorldTransformNormals());
		//addComponent(new FogComponent());
		addComponent(new LightComponent());
	}
	
	@Override
	public void setup(RendererState rendererState, Matrix4 modelMatrix) {
		// Silly bug: 2 hours wasted 22.11.2012 because I forgot to actually
		// set a shader... :|
		enableShader(rendererState);
		
		
		shader.setUVector4f("matAmbient", ambient.getData());
		shader.setUVector4f("matDiffuse", diffuse.getData());
		shader.setUVector4f("matSpecular", specular.getData());
		shader.setU1i("shininess", shininess);
		
		super.setup(rendererState, modelMatrix);
	}
}