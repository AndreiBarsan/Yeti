package barsan.opengl.rendering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import barsan.opengl.Yeti;
import barsan.opengl.input.CameraInput;
import barsan.opengl.math.Vector3;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;

public class Scene implements GLEventListener {

	protected ArrayList<ModelInstance> modelInstances = new ArrayList<>();
	
	// Billboards get special treatment as they're transparent
	protected ArrayList<Billboard> billbords = new ArrayList<>();
	
	protected Renderer renderer;
	protected Camera camera;
	protected CameraInput cameraInput; 
	private boolean exiting = false;
	
	/** Timing ****************************************************************/
	long lastTime;
	
	/** Lights ****************************************************************/
	protected ArrayList<PointLight> pointLights = new ArrayList<>();
	// TODO: implement
	protected ArrayList<DirectionalLight> directionalLights = new ArrayList<>();
	// TODO: implement
	//protected ArrayList<SpotLight> spotLights = new ArrayList<>();
	
	protected AmbientLight ambientLight = new AmbientLight(new Color(0.1f, 0.1f, 0.1f, 1.0f));
	
	/** Fog *******************************************************************/
	protected boolean fogEnabled = false;
	protected Fog fog;

	
	
	@Override
	public void init(GLAutoDrawable drawable) {
		// Setup basic elements
		camera = new Camera(Yeti.get().settings.width, Yeti.get().settings.height);
		GL2 gl = drawable.getGL().getGL2();
		gl = drawable.getGL().getGL2();
		
		// Setup the initial GL state
		gl.setSwapInterval(1);
		gl.glClearColor(0.33f, 0.33f, 0.33f, 1.0f);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDepthFunc(GL2.GL_LEQUAL);
		gl.glEnable(GL2.GL_CULL_FACE);
		gl.glCullFace(GL2.GL_BACK);
		gl.glFrontFace(GL2.GL_CCW);
		gl.glClearDepth(1.0d);
		
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
		
		try {
			ResourceLoader.loadAllShaders("res");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Prepare the renderer
		renderer = new Renderer(gl);
		
		lastTime = System.currentTimeMillis();
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {	}

	/**
	 * Coming soon: actual optimizations!!!
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		
		if(exiting) {
			exit();
			return;
		}
		
		// Setup the renderer
		RendererState rs = renderer.getState();
		rs.setGl(gl);
		rs.setAmbientLight(ambientLight);
		rs.setCamera(camera);
		rs.setPointLights(pointLights);
		rs.setDirectionalLights(directionalLights);
		if(fogEnabled) {
			rs.setFog(fog);
		} else {
			rs.setFog(null);
		}
		
		renderer.render(this);
		
		lastTime = System.currentTimeMillis();
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		//System.out.printf("Scene reshape: x=%d y=%d\n", x, y);
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
	
	public void postExitFlag(Yeti engine, Scene next) {
		exiting = true;
		this.engine = engine;
	}
	
	private Yeti engine;
	
	private void exit() {
		// Temporary cleanup behavior - at the moment, scenes are independent of each other
		ResourceLoader.cleanUp();
		
		unregisterInputSources(engine);
		engine.transitionFinished();
		
		exiting = false;
	}

}
