package barsan.opengl.rendering;

import java.io.IOException;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import barsan.opengl.Yeti;
import barsan.opengl.input.CameraInput;
import barsan.opengl.rendering.lights.AmbientLight;
import barsan.opengl.rendering.lights.Light;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;
import barsan.opengl.util.GUI;

public class Scene implements GLEventListener {

	protected ArrayList<ModelInstance> modelInstances = new ArrayList<>();
	
	// Billboards get special treatment as they're transparent
	protected ArrayList<Billboard> billbords = new ArrayList<>();
	
	protected Renderer renderer;
	protected Camera camera;
	protected CameraInput cameraInput; 
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

	@Override
	public void init(GLAutoDrawable drawable) {
		// Setup basic elements
		camera = new PerspectiveCamera(Yeti.get().settings.width, Yeti.get().settings.height);
		
		try {
			ResourceLoader.loadAllShaders("res");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Prepare the renderer; use the default renderer
		renderer = new Renderer(Yeti.get().gl.getGL3());
		
		lastTime = System.currentTimeMillis();
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {	}

	@Override
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
		if(fogEnabled) {
			rs.setFog(fog);
		} else {
			rs.setFog(null);
		}
		
		renderer.render(this);
		
		if(gui != null) {
			gui.render();
		}
		
		lastTime = System.currentTimeMillis();
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		camera.reshape(x, y, width, height);
	}

	public Camera getCamera() {
		return camera;
	}
	
	public float getDelta() {
		return ((float)(System.currentTimeMillis() - lastTime)) / 1000.0f;
	}

	public void registerInputSources(Yeti yeti) {
		/// Handle camera input
		cameraInput = new CameraInput(camera);
		yeti.addKeyListener(cameraInput);
		yeti.addMouseListener(cameraInput);
		yeti.addMouseMotionListener(cameraInput);
	}
	
	public void unregisterInputSources(Yeti yeti) {
		yeti.removeKeyListener(cameraInput);
		yeti.removeMouseListener(cameraInput);
		yeti.removeMouseMotionListener(cameraInput);
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
	public void play() {
		cameraInput.setMouseControlled(true);
	}
	
	/**
	 * Called when the scene simulation should halt its interactivity and,
	 * among other things, release the cursor (if captured).
	 */
	public void pause() {
		cameraInput.setMouseControlled(false);
	}
	
	/**
	 * Tells the OpenGL engine to shutdown before its next render cycle.
	 * Note: this might just begin a transition to quit the scene - the shutdown
	 * is not guaranteed to be immediate.
	 * 
	 * @param engine	Reference to the central engine.
	 * @param next		(NYI) The next scene to transition to.
	 */
	public void postExitFlag(Yeti engine, Scene next) {
		exiting = true;
		this.engine = engine;
	}
	private Yeti engine;
	
	protected void exit() {
		// Temporary cleanup behavior - at the moment, scenes are independent of each other
		ResourceLoader.cleanUp();
		
		unregisterInputSources(engine);
		engine.transitionFinished();
		
		exiting = false;
	}

}
