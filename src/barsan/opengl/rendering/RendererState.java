package barsan.opengl.rendering;

import java.util.ArrayList;

import javax.media.opengl.GL2;

/**
 * 	The current state of the renderer
 *	Contains a list of lights, a gl context, a readonly camera state
 */
public class RendererState {
	
	private GL2 gl;
	private ArrayList<PointLight> pointLights;
	private ArrayList<DirectionalLight> directionalLights;
	
	private AmbientLight ambientLight;
	private Fog fog; 
	
	// Default material - Gouraud with basic white color
	private Material defaultMaterial = new BasicMaterial();
	
	private Camera camera;

	public RendererState(GL2 gl) {
		this.gl = gl;
	}

	public RendererState(GL2 gl, ArrayList<PointLight> pointLights,
			ArrayList<DirectionalLight> directionalLights,
			AmbientLight ambientLight, Camera camera) {
		this.gl = gl;
		this.pointLights = pointLights;
		this.directionalLights = directionalLights;
		this.ambientLight = ambientLight;
		this.camera = camera;
	}

	public void setPointLights(ArrayList<PointLight> pointLights) {
		this.pointLights = pointLights;
	}

	public void setDirectionalLights(ArrayList<DirectionalLight> directionalLights) {
		this.directionalLights = directionalLights;
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
	
	public void setGl(GL2 gl) {
		this.gl = gl;
	}
	
	public GL2 getGl() {
		return gl;
	}

	public ArrayList<PointLight> getPointLights() {
		return pointLights;
	}

	public ArrayList<DirectionalLight> getDirectionalLights() {
		return directionalLights;
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
}
