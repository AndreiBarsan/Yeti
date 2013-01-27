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
 * 	The current state of the renderer, i.e. elements that change throughout 
 * the rendering process. Pending refactoring.
 */
public class RendererState {
	
	public final GL3 gl;
	private ArrayList<Light> pointLights;
	private Renderer renderer;

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
	
	public RendererState(Renderer renderer, GL3 gl) {
		this.renderer = renderer;
		this.gl = gl;
	}
	
	/**
	 * Binds the proper shadow map to the active material. This might be made
	 * obsolete by the implementation of multiple light casters that can be
	 * occluded, but that's a long way down the road.
	 */
	public void shadowMapBindings(Material m, Matrix4 modelMatrix) {
		if(pointLights.get(0).getType() != LightType.Point) {
			// We don't need this matrix if we're working with point lights
			// and cube maps.
			Matrix4 projection = depthProjection;
			Matrix4 view = depthView;
			
			Matrix4 MVP = new Matrix4(projection).mul(view).mul(modelMatrix);
			
			// Really important! Converts the z-values from [-1, 1] to [0, 1]
			Matrix4 biasMVP = new Matrix4(Renderer.shadowBiasMatrix).mul(MVP);
			
			m.getShader().setUMatrix4("mvpMatrixShadows", biasMVP);
			
		} else {
			m.getShader().setU1f("far", renderer.getOmniShadowFar());
		}
		
		m.getShader().setU1i("useShadows", true);
		m.getShader().setU1i("shadowQuality", renderer.getShadowQuality());
	}
	
	/** @see #shadowMapBindings(Material m)  */
	public int shadowMapTextureBindings(Material m, int slot) {
		gl.glActiveTexture(GLHelp.textureSlot[slot]);
		
		if(pointLights.get(0).getType() == LightType.Point) {
			m.getShader().setU1i("samplingCube", true);
			m.getShader().setU1i("cubeShadowMap", slot);
			cubeTexture.bind(gl);
		} else {
			m.getShader().setU1i("samplingCube", false);
			m.getShader().setU1i("shadowMap", slot);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, shadowTexture);
		}		
		
		return 1;
	}
	
	public void setAnisotropySamples(int value) {
		if(value > maxAnisotropySamples) {
			Yeti.warn("Given %d anisotropic samples, only supporting %d - clamping!", value, maxAnisotropySamples);
			anisotropySamples = maxAnisotropySamples;
		} else {
			anisotropySamples = value;
		}
	}
	
	public int getAnisotropySamples() {
		return anisotropySamples;
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
	
}
