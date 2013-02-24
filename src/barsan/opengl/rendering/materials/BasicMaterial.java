package barsan.opengl.rendering.materials;

import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;

/**
 * 
 * The basic material used for static meshes in the engine. Built in are Phong 
 * ASD lighting, simple texture support and shadow receiving.
 * 
 * Whether something casts a shadow is controlled in the model instance object,
 * not here, due to the way the shadow mapping implementation works.
 * 
 * Other available modules that can be attached are:
 *  - bumpmapping
 *  - fog
 *  - multitexturing (planned v2.0)
 *  - emmisive maps	 (planned v2.0)
 *  - specular maps  (planned v2.0)
 * 
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
		addComponent(new LightComponent());
		addComponent(new ShadowReceiver());
	}
	
	@Override
	public void setup(RendererState rendererState, Matrix4 modelMatrix) {
		super.setup(rendererState, modelMatrix);
		
		shader.setUVector4f("matAmbient", ambient.getData());
		shader.setUVector4f("matDiffuse", diffuse.getData());
		shader.setUVector4f("matSpecular", specular.getData());
		shader.setU1i("shininess", shininess);
	}
}