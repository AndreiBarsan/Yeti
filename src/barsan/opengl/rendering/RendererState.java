package barsan.opengl.rendering;

import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL3;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.Renderer.ShadowQuality;
import barsan.opengl.rendering.cameras.Camera;
import barsan.opengl.rendering.lights.AmbientLight;
import barsan.opengl.rendering.lights.Light;
import barsan.opengl.rendering.lights.Light.LightType;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.util.GLHelp;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.texture.Texture;

/**
 * The current state of the renderer, i.e. elements that change throughout 
 * the rendering process. Pending refactoring.
 */
public class RendererState {

	public final GL4              gl;
	private      ArrayList<Light> lights;
	private      Renderer         renderer;

	private AmbientLight ambientLight;
	private Fog          fog;

	private Material         forcedMaterial         = null;
	private AnimatedMaterial forcedAnimatedMaterial = null;

	private Camera camera;

	int maxAnisotropySamples = -1;
	int anisotropySamples    = 1;

	public Matrix4 depthView;
	public Matrix4 depthProjection;
	public int     shadowTexture;
	/* pp */ Texture cubeTexture;

	private Scene scene;

	protected float omniShadowNear = 0.1f;
	protected float omniShadowFar  = 100.0f;

	public RendererState(Renderer renderer, GL4 gl) {
		this.renderer = renderer;
		this.gl = gl;
	}

	/**
	 * Binds the proper shadow map to the active material. 
	 */
	public void shadowMapBindings(Shader program, Matrix4 modelMatrix) {
		if (scene.shadowsEnabled) {
			if (lights.get(0).getType() != LightType.Point) {
				// We don't need this matrix if we're working with point lights and cube maps.
				Matrix4 projection = depthProjection;
				Matrix4 view = depthView;

				Matrix4 MVP = new Matrix4(projection).mul(view).mul(modelMatrix);

				// Really important! Converts the z-values from [-1, 1] to [0, 1]
				Matrix4 biasMVP = new Matrix4(Renderer.shadowBiasMatrix).mul(MVP);
				program.setUMatrix4("mvpMatrixShadows", biasMVP);

			}
			else {
				program.setU1f("far", getOmniShadowFar());
			}
			
			program.setU1i("useShadows", true);
			program.setU1i("shadowQuality", renderer.getShadowQuality().getFlag());
		}
	}
	
	/** @see #shadowMapBindings(Material m)  */
	public int shadowMapTextureBindings(Shader program, int slot) {
		// This binds the maps anyway, even if the scene doesn't have shadows
		// enabled, in order to prevent errors caused by unassigned samplers of
		// different types.
		if(lights.get(0).getType() == LightType.Point) {
			program.setU1i("samplingCube", true);
		}
		else {
			program.setU1i("samplingCube", false);
		}
		
		gl.glActiveTexture(GLHelp.textureSlot[slot]);
		program.setU1i("cubeShadowMap", slot);
		cubeTexture.bind(gl);
			
		gl.glActiveTexture(GLHelp.textureSlot[slot + 1]);
		program.setU1i("shadowMap", slot + 1);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, shadowTexture);
		return 2;
	}
	
	public void setAnisotropySamples(int value) {
		if(value > maxAnisotropySamples) {
			Yeti.warn("Requested %d anisotropic samples, only supporting %d - clamping!", value, maxAnisotropySamples);
			anisotropySamples = maxAnisotropySamples;
		} else {
			anisotropySamples = value;
		}
	}
	
	public int getAnisotropySamples() {
		return anisotropySamples;
	}

	public void setLights(ArrayList<Light> pointLights) {
		this.lights = pointLights;
	}
	
	public void setAmbientLight(AmbientLight ambientLight) {
		this.ambientLight = ambientLight;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}
	
	public ArrayList<Light> getLights() {
		return lights;
	}

	public AmbientLight getAmbientLight() {
		return ambientLight;
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
	
	public AnimatedMaterial getForcedAnimatedMaterial() {
		return forcedAnimatedMaterial;
	}
	
	public void forceAnimatedMaterial(AnimatedMaterial material) {
		forcedAnimatedMaterial = material;
	}
	
	public boolean hasForcedAnimatedMaterial() {
		return forcedAnimatedMaterial != null;
	}
	
	public Material getForcedMaterial() {
		return forcedMaterial;
	}
	
	public Texture getShadowMapCube() {
		return cubeTexture;
	}

	public Scene getScene() {
		return scene;
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}
	
	public ShadowQuality getShadowQuality() {
		return renderer.getShadowQuality();
	}
	
	public float getOmniShadowNear() {
		return omniShadowNear;
	}

	public void setOmniShadowNear(float omniShadowNear) {
		this.omniShadowNear = omniShadowNear;
	}

	public float getOmniShadowFar() {
		return omniShadowFar;
	}

	public void setOmniShadowFar(float omniShadowFar) {
		this.omniShadowFar = omniShadowFar;
	}

	// FIXME: dude, come on!
	public int getGipsyWagonCubeTex() {
		return ((Nessie)renderer).texCube;
	}

}
