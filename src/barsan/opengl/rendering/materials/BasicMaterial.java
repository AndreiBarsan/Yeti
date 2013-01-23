package barsan.opengl.rendering.materials;

import java.util.Collections;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;


import barsan.opengl.math.MathUtil;
import barsan.opengl.math.Matrix3;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Fog;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.rendering.lights.AmbientLight;
import barsan.opengl.rendering.lights.DirectionalLight;
import barsan.opengl.rendering.lights.Light;
import barsan.opengl.rendering.lights.Light.LightType;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.lights.SpotLight;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;
import barsan.opengl.util.GLHelp;

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