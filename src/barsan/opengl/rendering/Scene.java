package barsan.opengl.rendering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.input.InputProvider;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.cameras.Camera;
import barsan.opengl.rendering.cameras.PerspectiveCamera;
import barsan.opengl.rendering.lights.AmbientLight;
import barsan.opengl.rendering.lights.Light;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;
import barsan.opengl.util.GUI;

public class Scene {

	protected ArrayList<ModelInstance> modelInstances = new ArrayList<>();
	
	// Billboards get special treatment as they're transparent
	protected ArrayList<Billboard> billboards = new ArrayList<>();
	
	protected Renderer renderer;
	protected Camera camera;
	protected boolean exiting = false;
	
	/** Timing ****************************************************************/
	long lastTime;
	
	/** Lights ****************************************************************/
	protected ArrayList<Light> lights = new ArrayList<>();
	protected AmbientLight globalAmbientLight = new AmbientLight(new Color(0.1f, 0.1f, 0.1f, 1.0f));
	
	/** Fog *******************************************************************/
	protected boolean fogEnabled = false;
	protected Fog fog;
	
	protected GUI gui;
	
	public boolean shadowsEnabled = false;
	List<InputProvider> inputProviders = new ArrayList<>();

	
	public void init(GLAutoDrawable drawable) {
		// Setup basic elements
		camera = new PerspectiveCamera(Yeti.get().settings.width, Yeti.get().settings.height);
		
		try {
			ResourceLoader.loadAllShaders(ResourceLoader.RESBASE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// TODO: a more elegat way to specify a renderer
		// Prepare the renderer; use the default renderer
		if(renderer == null) {
			throw new IllegalArgumentException("Renderer cannot be null.");
		}
		
		Renderer.renderDebug = Yeti.get().debug;
		
		lastTime = System.nanoTime();
	}

	public void display(GLAutoDrawable drawable) {

		if(exiting) {
			exit();
			return;
		}
		
		// Setup the renderer
		RendererState rs = renderer.getState();
		rs.setAmbientLight(globalAmbientLight);
		rs.setCamera(camera);
		rs.setLights(lights);
		rs.setScene(this);
		if(fogEnabled) {
			rs.setFog(fog);
		} else {
			rs.setFog(null);
		}
		
		renderer.render(this);
		
		if(gui != null) {
			Yeti.get().gl.glDisable(GL4.GL_DEPTH_TEST);
			gui.render();
			Yeti.get().gl.glEnable(GL4.GL_DEPTH_TEST);
		}
		
		lastTime = System.nanoTime();
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		// TODO: this isn't enough - the renderer needs to resize its postprocess
		// buffers as well - not a priority
		/*
		camera.width = width;
		camera.height = height;
		camera.refreshProjection();
		*/
	}
	
	public void setCamera(Camera camera) {
		// FIXME: connected camera inputs might have a problem with this
		this.camera = camera;
	}
	
	public Camera getCamera() {
		return camera;
	}
	
	public List<ModelInstance> getModelInstances() {
		return modelInstances;
	}
	
	public void addModelInstance(ModelInstance modelInstance) {
		assert (! (modelInstance instanceof Billboard)) : "Billboards should be handled separately!"; 
		modelInstances.add(modelInstance);
	}
	
	public void removeModelInstance(ModelInstance modelInstance) {
		modelInstances.remove(modelInstance);
	}
	
	public void addBillboard(Billboard billboard) {
		billboards.add(billboard);
	}
	
	/**
	 * Adds a billboard and sets its z position. Useful in 2D scenes for positioning
	 * sprites "on top" of each other.
	 */
	public void addBillboard(Billboard billboard, float z) {
		Vector3 p = billboard.getTransform().getTranslate();
		billboard.getTransform().updateTranslate(p.x, p.y, z);
		billboards.add(billboard);
	}

	public void addInput(InputProvider inputProvider) {
		inputProviders.add(inputProvider);
		Yeti.get().addInputProvider(inputProvider);
	}
	
	private void unregisterInputSources() {
		Yeti y = Yeti.get();
		for(InputProvider ip : inputProviders) {
			y.removeInputProvider(ip);
		}
	}
	
	public void setRenderer(Renderer renderer) {
		this.renderer = renderer;
	}
	
	public Renderer getRenderer() {
		return renderer;
	}

	/**
	 * Called when the scene simulation is to become interactive.
	 */
	public void play() { }
	
	/**
	 * Called when the scene simulation should halt its interactivity and,
	 * among other things, release the cursor (if captured).
	 */
	public void pause() { }
	
	/**
	 * Tells the OpenGL engine to shutdown before its next render cycle.
	 * Note: this might just begin a transition to quit the scene - the shutdown
	 * is not guaranteed to be immediate.
	 * 
	 * @param engine	Reference to the central engine.
	 * @param next		(NYI) The next scene to transition to.
	 */
	public void beginExit(Yeti engine, Scene next) {
		exiting = true;
		this.engine = engine;
	}
	private Yeti engine;
	
	protected void exit() {
		// Temporary cleanup behavior - at the moment, scenes are independent 
		// of each other; for games, this doesn't make sense; 
		pause();
		ResourceLoader.cleanUp();
		renderer.dispose();
		engine.transitionFinished();
		unregisterInputSources();
		exiting = false;
	}
	
	public List<Light> getLights() {
		return lights;
	}

}
