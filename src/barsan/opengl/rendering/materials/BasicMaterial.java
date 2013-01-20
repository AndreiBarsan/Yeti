package barsan.opengl.rendering.materials;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;


import barsan.opengl.Yeti;
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
 * Note - Gouraud is pretty much 100% replaceable with Phong. They're the same
 * anyway, so only the actual shader programs differ. The uniforms are the same.
 * @author Andrei Barsan
 */
public class BasicMaterial extends Material {	
	
	// TODO: consistent uniform names to ease automatic material management in the future
	
	static final String GOURAUD_NAME 	= "basic";
	static final String PHONG_NAME 		= "phong";
	static final String PHONG_NAME_FLAT	= "phongFlat";
	
	static final Color blank = new Color(0.0f, 0.0f, 0.0f, 0.0f);
	
	public enum ShadingModel {
		Phong,
		Gouraud
	}
	
	private ShadingModel mode = ShadingModel.Phong;
	
	// TODO: move up!
	private List<MaterialComponent> components = new ArrayList<MaterialComponent>();
	
	public BasicMaterial(Color diffuse) {
		this(Color.WHITE, diffuse, Color.WHITE);
	}

	public BasicMaterial() {
		this(Color.WHITE, Color.WHITE, Color.WHITE);
	}
	
	public BasicMaterial(Color ambient, Color diffuse, Color specular) {
		super(ResourceLoader.shader(PHONG_NAME), ambient, diffuse, specular);
	}
	
	public void addComponent(MaterialComponent component) {
		components.add(component);
	}
	
	public void removeComponent(MaterialComponent component) {
		if(!components.remove(component)) {
			Yeti.screwed("Tried to remove non-existing material component!");
		}
	}
	
	public boolean containsComponent(MaterialComponent component) {
		return components.contains(component);
	}
	
	public void setMode(ShadingModel mode) {
		this.mode = mode;
		if(mode == ShadingModel.Phong) {
			shader = ResourceLoader.shader(PHONG_NAME);
		} else {
			shader = ResourceLoader.shader(GOURAUD_NAME);
		}
	}
	
	public void toggleMode() {
		if(mode == ShadingModel.Phong) {
			setMode(ShadingModel.Gouraud);
		} else {
			setMode(ShadingModel.Phong);
		}
	}

	@Override
	public void setup(RendererState rendererState, Matrix4 modelMatrix) {
		view.set(rendererState.getCamera().getView());
		projection.set(rendererState.getCamera().getProjection());
		viewModel.set(view).mul(modelMatrix);
		
		// WARNING: A * B * C != A * (B * C) with matrices
		// The following line does not equal projection * viewModel
		MVP.set(projection).mul(view).mul(modelMatrix);
		
		// Silly bug: 2 hours wasted 22.11.2012 because I forgot to actually
		// set a shader... :|
		enableShader(rendererState);
		
		shader.setUMatrix4("mvpMatrix", MVP);
		shader.setUMatrix4("mvMatrix", viewModel);
		shader.setUMatrix4("vMatrix", view);
		shader.setUMatrix3("normalMatrix", MathUtil.getNormalTransform(viewModel));
		
		shader.setUMatrix3("vMatrix3x3", new Matrix3(view));
		
		// TODO: implement ARRAYS OF LIGHTS here!
		Light light = rendererState.getLights().get(0);
		AmbientLight ambientLight = rendererState.getAmbientLight();
		
		if(light.getType() == LightType.Point || light.getType() == LightType.Spot) {
			Vector3 lp = ((PointLight)light).getPosition();
			float[] buff4f = new float[] { lp.x, lp.y, lp.z, 1.0f };
			shader.setUVector4f("vLightPosition", buff4f);
		} else {
			// Directional light
			Vector3 lp = ((DirectionalLight)light).getDirection();
			float[] buff4f = new float[] { lp.x, lp.y, lp.z, 0.0f };
			shader.setUVector4f("vLightPosition", buff4f);
		}
		
		shader.setUVector4f("globalAmbient", ambientLight.getColor().getData());
		shader.setUVector4f("lightDiffuse", light.getDiffuse().getData());
		shader.setUVector4f("lightSpecular", light.getSpecular().getData());
		
		shader.setU1f("constantAt", light.getConstantAttenuation());
		shader.setU1f("linearAt", light.getLinearAttenuation());
		shader.setU1f("quadraticAt", light.getQuadraticAttenuation());
		shader.setU1f("cubicAt", light.getCubicAttenuation());
		
		// This isn't very clean - TODO: delegate this to special components
		if(light.getType() == LightType.Spot) {
			SpotLight sl = (SpotLight)light;
			shader.setU1f("lightTheta", sl.getTheta());
			shader.setU1f("lightPhi", sl.getPhi());
			shader.setU1f("lightExponent", sl.getExponent());
			shader.setUVector3f("spotDirection", sl.getDirection());		
		} else {
			shader.setU1f("lightTheta", 0.0f);
			shader.setU1f("lightPhi", 0.0f);
			shader.setU1f("lightExponent", 1.0f);
			shader.setUVector3f("spotDirection", Vector3.ZERO);
		}
		
		shader.setUVector4f("matAmbient", ambient.getData());
		shader.setUVector4f("matDiffuse", diffuse.getData());
		shader.setUVector4f("matSpecular", specular.getData());
		
		int textureIndex = 0;
		
		// Texture
		if(texture != null) {
			rendererState.gl.glActiveTexture(GLHelp.textureSlot[0]);
			textureIndex++;
			shader.setU1i("useTexture", 1);
			shader.setU1i("colorMap", 0);
			texture.bind(rendererState.gl);
			texture.setTexParameterf(rendererState.gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, rendererState.getAnisotropySamples());
		} else {
			shader.setU1i("useTexture", 0);
		}
		
		// This might be the problem that kills the whole component concept
		shader.setU1i("useShadows", false);
		
		shader.setU1i("useBump", 0);
		for (MaterialComponent c : components) {
			c.setup(this, rendererState, modelMatrix);
			textureIndex += c.setupTexture(this, rendererState, textureIndex);
		}
		
		// Fog
		if(rendererState.getFog() != null) {
			Fog fog = rendererState.getFog();
			shader.setU1i("fogEnabled", 1);
			shader.setU1f("minFogDistance", fog.minDistance);
			shader.setU1f("maxFogDistance", fog.maxDistance);
			shader.setUVector4f("fogColor", fog.color.getData());
		} else {
			shader.setU1i("fogEnabled", 0);
		}
		
		shader.setU1i("shininess", shininess);
	}
}