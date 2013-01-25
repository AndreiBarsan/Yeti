package barsan.opengl.rendering;

import java.util.ArrayList;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.lights.AmbientLight;
import barsan.opengl.rendering.lights.Light;
import barsan.opengl.rendering.lights.Light.LightType;
import barsan.opengl.rendering.materials.BasicMaterial;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.util.GLHelp;

import com.jogamp.opengl.util.texture.Texture;

/**
 * 	The current state of the renderer
 *	Contains a list of lights, a gl context, a readonly camera state
 */
public class RendererState {
	
	public final GL3 gl;
	private ArrayList<Light> pointLights;
	
	private AmbientLight ambientLight;
	private Fog fog; 
	
	private Material defaultMaterial = new BasicMaterial();
	private Material forcedMaterial = null;
	
	private Camera camera;

	int maxAnisotropySamples = -1;
	int anisotropySamples = 1;
	
	public Matrix4 depthView;
	public Matrix4 depthProjection;
	public int shadowTexture;
	/* pp */ Texture cubeTexture;
	
	public RendererState(GL3 gl) {
		this.gl = gl;
	}
	
	/**
	 * Binds the proper shadow map to the active material. This might be made
	 * obsolete by the implementation of multiple light casters that can be
	 * occluded, but that's a long way down the road.
	 *  
	 * @param m
	 */
	public void shadowMapBindings(Material m) {
		m.getShader().setU1i("useShadows", true);
		m.getShader().setU1i("shadowQuality", 2);
	}
	
	public int shadowMapTextureBindings(Material m, int slot) {
		gl.glActiveTexture(GLHelp.textureSlot[5]);
		
		if(pointLights.get(0).getType() == LightType.Point) {
			m.getShader().setU1i("cubeShadowMap", 5);
			cubeTexture.bind(gl);
		} else {
			m.getShader().setU1i("shadowMap", slot);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, shadowTexture);
		}		
		
		return 1;
	}
	

	public RendererState(GL3 gl, ArrayList<Light> pointLights,
			AmbientLight ambientLight, Camera camera, int anisotropySamples) {
		this.gl = gl;
		this.pointLights = pointLights;
		this.ambientLight = ambientLight;
		this.camera = camera;
		this.anisotropySamples = anisotropySamples;
	}

	public void setLights(ArrayList<Light> pointLights) {
		this.pointLights = pointLights;
	}
	
	public void setAmbientLight(AmbientLight ambientLight) {
		this.ambientLight = ambientLight;
	}

	public void setDefaultMaterial(Material defaultMaterial) {
		this.defaultMaterial = defaultMaterial;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}
	
	public ArrayList<Light> getLights() {
		return pointLights;
	}

	public AmbientLight getAmbientLight() {
		return ambientLight;
	}

	public Material getDefaultMaterial() {
		return defaultMaterial;
	}
	
	public Camera getCamera() {
		return camera;
	}

	public void setFog(Fog fog) {
		this.fog = fog;
	}
	
	public Fog getFog() {
		return fog;
	}
	
	public int getAnisotropySamples() {
		return anisotropySamples;
	}
	
	public void forceMaterial(Material material) {
		forcedMaterial = material;
	}
	
	public boolean hasForcedMaterial() {
		return forcedMaterial != null;
	}
	
	public Material getForcedMaterial() {
		return forcedMaterial;
	}
	
	public Texture getShadowMapCube() {
		return cubeTexture;
	}
	
	public void setAnisotropySamples(int value) {
		if(value > maxAnisotropySamples) {
			Yeti.warn("Given %d anisotropic samples, only supporting %d - clamping!", value, maxAnisotropySamples);
			anisotropySamples = maxAnisotropySamples;
		} else {
			anisotropySamples = value;
		}
	}
}
