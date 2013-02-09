package barsan.opengl;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.lang.Thread.UncaughtExceptionHandler;

import javax.media.opengl.DebugGL3bc;
import javax.media.opengl.GL3bc;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;

import barsan.opengl.editor.App;
import barsan.opengl.flat.GameScene;
import barsan.opengl.input.GlobalConsoleInput;
import barsan.opengl.rendering.Scene;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.scenes.DemoScene;
import barsan.opengl.scenes.LightTest;
import barsan.opengl.scenes.ProceduralScene;
import barsan.opengl.util.Settings;

import com.jogamp.opengl.util.Animator;

/**
 * @author Andrei Barsan
 * 
 * TO-DO LIST OF THINGS TO DO
 * =============================================================================
 * FIXME: only bump but no texture -> tries to render bumpmap without having
 * texture coords!!!
 * 
 * TODO: figure out what to with textures - if I'm going to move them to the
 * respective component altogether, how will we interact with the VBO with the
 * texture coords? Maybe some sort of VBO channel interface that belongs to a
 * component? Texture -> UV, WorldTransform -> Geometry, WTNormals -> Geometry + normals
 * Bump component (example) -> Tangents
 * 
 * TODO: continuation of the above - right now some unbound texture slots cause
 * crashes - for instance the shadow maps - if you render something with the 
 * shadow map bound to pos 2, then render something else with the shadow map
 * bound to pos 3, it crashes
 * 
 * FIXME: view matrix straight down? BUG. FIX IT!
 * 
 * TODO: optimize omnidirectional shadow maps with dot products instead of lengths, maybe?
 * TODO: check exactly when we need to enable the shader; the current setup 
 * might actually be broken and only working through dumb luck
 * TODO: use shadow samplers for the shadow computations, they're faster
 * TODO: use input polling for the camera (needed later for the char controls)
 * 
 * TODO: more math; were the previous bugs really only caused by poorly configured
 * shadowmap generation projection matrices? (the black stripe ones for instance)
 * 
 * TODO: if so, start working on binding volumes to start doing lighting the right
 * way!
 * TODO: standardized texture units - only bind things like the shadow map ONCE
 * TODO: camera update() method (automatically called by the scene - keep everything
 * in sync, prevent recalculations of the view matrix etc.)
 * TODO: perpixel fog & fix fog computation
 * TODO: actually find and write down the matrix multiplication BUG !!!
 * TODO: light lists
 * TODO: optional utility to draw:
 * 			- pie chart render data
 * 			- axes
 * 			- NORMALS!
 * TODO: when creating post-process effects, compile basic vertex shader, get 
 * all other fragment shaders, and link all fragments to the same vertex shader,
 * saving (n-1) useless recompilations of the postprocess vertex shaders
 * TODO: global rendering settings should be part of every material (think
 * gamma correction and tone mapping); should gamma correction be part of a
 * post-processing system?
 * TODO: editor GUI						~
 * TODO: smooth camera movement 		~
 * TODO: depth of field
 * TODO: uniform blocks for shaders (with possibility of loading multiple items
 * in a single action from the App)
 * TODO: global cel-shading effect (with no double drawing, just take depth-buffer
 * 			slap an uncanny edge detection, render found edges black on color-buffer)
 * TODO: list of all lights and entities
 * TODO: edit lights and entities (entity transforms)
 * TODO: load .obj dialog with "recents" list
 * TODO: log object
 * =============================================================================
 */
public class Yeti implements GLEventListener {
	
	// Miscellaneous settings
	public Settings settings;
	// Logging flags
	public boolean warnings = true;
	public boolean debug = true;
	
	// TODO: scene manager with a stack / graph of scenes
	private Scene currentScene;
	private Frame frame;
	private Cursor blankCursor;
	
	// I draw on this
	private Component canvasHost;
	
	// Swing application Yeti is hosted in
	private App hostApp;
	
	// Active OpenGL context. Use GL3.0 by default, with backwards compatibility
	// for the fixed pipeline to allow debug drawing.
	public GL3bc gl;
	
	// Keeps everything in sync.
	private final Animator animator;
	
	//private CameraInput cameraInput;
	volatile boolean pendingInit = false;	// TODO: better interaction with input thread
	private Scene pendingScene;	// Used in transitions
	
	/**
	 * Iterating through a package to find its classes is not as trivial
	 * as it might seem, so this will do. It's just tempament anyway.
	 */
	static Class<?>[] availableScenes = new Class[] {
			DemoScene.class,
			//TextScene.class,
			ProceduralScene.class,
			//ModelGraphScene.class,
			LightTest.class,
			//ZRenderTest.class
			GameScene.class
	};
	static {
		for(Class<?> c : availableScenes) {
			assert Scene.class.isAssignableFrom(c) : "Only instances of Scene allowed!";
		}
	}
	
	public static Class<?>[] getAvailableScenes() {
		return availableScenes;
	}
	
	private Yeti() {
		animator = new Animator();
	}
	
	private void startup() {
		settings = Settings.load();
		debug("Starting up Yeti...");		
		
		// Create the default scene
		debug("Available scenes: ");
		for(Class<?> c : availableScenes) {
			debug(c.getCanonicalName());
		}
		
		// Setup transient fields
		settings.width = 1024;
		settings.height = 768;
		settings.playing = false;
		
		// Create blank cursor
		// Transparent 16 x 16 pixel cursor image.
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

		// Create a new blank cursor.
		blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
		    cursorImg, new Point(0, 0), "blank cursor");

	}
	
	public void transitionFinished() {
		currentScene = pendingScene;
		pendingInit = true;
	}
	
	public void loadScene(Scene newScene) {
		pendingScene = newScene;
		
		if(currentScene != null) {	
			currentScene.postExitFlag(this, newScene);
		} else {
			// No scene to transition out of, so we're instantly finished
			transitionFinished();
		}
	}
	
	public void hackStartLoop(App app, Frame frame, Container hostContainer) {
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				new Thread(new Runnable() {
					public void run() {
						animator.stop();
						System.exit(0);
					}
				}).start();
			}
		});
				
		this.frame = frame;
		this.hostApp = app;
		
		final GLJPanel glpanel = createCanvasPanel();
		//glpanel.setContextCreationFlags(GL2.GL_MULTISAMPLE);
		glpanel.addGLEventListener(this);
		glpanel.setSize(settings.width, settings.height);
		animator.add(glpanel);
		canvasHost = glpanel;
		hostContainer.add(glpanel);
		
		hostContainer.setVisible(true);
		glpanel.setVisible(true);		
		
		hostContainer.setPreferredSize(new Dimension(
				settings.width, settings.height + frame.getPreferredSize().height));
		frame.pack();
		frame.setVisible(true);
		
		
		animator.setUpdateFPSFrames(30, null);
		animator.start();
		
		glpanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				focusMouse();
			}
		});
		
		focusMouse();
	}
	
	public void focusMouse() {
		canvasHost.requestFocus();
		canvasHost.setCursor(blankCursor);
		settings.playing = true;
		if(currentScene != null)
			currentScene.play();
	}
	
	/**
	 * Pause the interactive components of the running simulation.
	 */
	public void pause() {
		settings.playing = false;
		canvasHost.setCursor(Cursor.getDefaultCursor());
		currentScene.pause();		
	}
	
	public void startRenderLoop(Frame frame, Container hostContainer) {
		// Create and setup the canvas
		GLCanvas canvas = createCanvas();
		canvas.addGLEventListener(this);
		animator.add(canvas);
		this.canvasHost = canvas;
		
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				new Thread(new Runnable() {
					public void run() {
						animator.stop();
						System.exit(0);
					}
				}).start();
			}
		});
				
		this.frame = frame;
		
		hostContainer.add(canvas);
		
		// ...and away we go!
		frame.setVisible(true);
		animator.setUpdateFPSFrames(10, null);
		animator.start();
	}
	
	private static Yeti instance;
	public static Yeti get() {
		if(instance == null) {
			instance = new Yeti();
			instance.startup();
		}
		return instance;
	}
	
	public static void debug(String message) {
		if(get().debug) System.out.printf("[DEBUG] %s\n", message);
	}
	
	public static void debug(String format, Object... stuff) {
		if(get().debug) System.out.printf("[DEBUG] " + format + "\n", stuff);
	}
	
	public static void warn(String message) {
		if(get().warnings) System.err.printf("[WARNING] %s\n", message);
	}
	
	public static void warn(String format, Object... stuff) {
		if(get().warnings) System.out.printf("[WARNING] " + format + "\n", stuff);
	}
	
	public static void screwed(String message) {
		System.err.flush();
		System.out.flush();
		System.err.printf("[FATAL] %s\n", message);
		if(Yeti.get().debug) {
			System.err.printf("Thread: %s\n", Thread.currentThread());
			StackTraceElement sad[] = new Throwable().getStackTrace();
			final int maxStack = 5;
			for(int i = 1; i < Math.min(maxStack + 1, sad.length); i++) {
				StackTraceElement el = sad[i];
				System.err.printf("\t- %s\n", el);
			}
		}
		System.exit(-1);
	}
	
	public static void screwed(String message, Throwable cause) {
		cause.printStackTrace();
		screwed(message);
	}
	
	public static void quit() {
		Yeti.debug("Shutting down...");
		Yeti.debug("Saving settings...");
		Settings.save(get().settings);
		Yeti.debug("Settings saved.");
		System.exit(0);		
	}
	
	private class QuickSceneSwitcher implements KeyListener {
		public void keyTyped(KeyEvent e) { }
		public void keyPressed(KeyEvent e) { }
		
		public void keyReleased(KeyEvent e) {
			char in = e.getKeyChar();
			if(in >= '0' && in <= '9') {
				int selection = in - '0';
				if(selection < availableScenes.length) {
					try {
						loadScene( (Scene)availableScenes[selection].newInstance());
						settings.lastSceneIndex = selection;
					} catch (InstantiationException | IllegalAccessException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}
	
	// Combating JGLCanvas weirdness
	boolean engineInitialized = false;
	
	@Override
	public void init(GLAutoDrawable drawable) {

		// Get the new context
		if(debug) {
			drawable.setGL(new DebugGL3bc(drawable.getGL().getGL3bc()));
		}
		gl = drawable.getGL().getGL3bc();
			
		if(engineInitialized) {
			Yeti.screwed("GL Context was reset. Yeti cannot handle that yet. :(");
			return;
		}
		
		if(hostApp != null) {
			// TODO: list of callbaks (also maybe make host apps implement
			// some hook functionality)
			hostApp.generateGLKnobs(this);
		}
		
		// Runing in debug mode
		Yeti.debug("Running in debug GL mode"); 
		
		final int lastLoadedScene = settings.lastSceneIndex;
		ResourceLoader.init();	
		
		addKeyListener(this.new QuickSceneSwitcher());

		// Soon-to-be global controller of GL settings
		addKeyListener(new GlobalConsoleInput());

		/*
		Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				if(e instanceof GLException) {
					Yeti.screwed("GLException: \n" + e.getMessage(), e);
				} else {
					Yeti.screwed("Yeti general error: \n" + e);
				}
			}
		});
		//*/
		
		try {
			loadScene((Scene)availableScenes[lastLoadedScene].newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		engineInitialized = true;
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		if(pendingInit) {
			System.out.println("Pending init - so doing init! (display)");
			currentScene.init(drawable);
			currentScene.registerInputSources(this);
			pendingInit = false;
		}
		currentScene.display(drawable);
	}


	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		if(pendingInit) {
			System.out.println("Pending init - so doing init! (reshape)");
			currentScene.init(drawable);
			currentScene.registerInputSources(this);
			pendingInit = false;
		}
		// TODO: if hosted in an application - make sure the sizes stay right
		// FIXME: currently makes the panel higher, although the originally 
		// requested size is 1024 x 768 (->1024x801)
		// Increasing toolbar height doesn't increase the height of the gray 
		// band. Then it has to be the window decorations.
		currentScene.reshape(drawable, x, y, width, height);
	}
	
	@Override
	public void dispose(GLAutoDrawable drawable) {
		currentScene.dispose(drawable);
		ResourceLoader.cleanUp();
	}
	
	private GLCanvas createCanvas() {
		GLProfile.initSingleton();
		GLProfile glp = GLProfile.get(GLProfile.GL3bc);
		GLCapabilities capabilities = new GLCapabilities(glp);
		GLCanvas canvas = new GLCanvas(capabilities);
		
		return canvas;
	}
	
	// TODO: refactor this and fix
	private GLJPanel createCanvasPanel() {
		GLProfile.initSingleton();
		GLProfile glp = GLProfile.get(GLProfile.GL3bc);
		GLCapabilities capabilities = new GLCapabilities(glp);
		GLJPanel gljp = new GLJPanel(capabilities);		
		return gljp;
	}
	
	public void addKeyListener(KeyListener keyListener) {
		frame.addKeyListener(keyListener);
		canvasHost.addKeyListener(keyListener);		
	}
	
	public void removeKeyListener(KeyListener keyListener) {
		frame.removeKeyListener(keyListener);
		canvasHost.removeKeyListener(keyListener);
	}
	
	public void addMouseListener(MouseListener mouseListener) {
		frame.addMouseListener(mouseListener);
		canvasHost.addMouseListener(mouseListener);
	}
	
	public void removeMouseListener(MouseListener mouseListener) {
		frame.removeMouseListener(mouseListener);
		canvasHost.removeMouseListener(mouseListener);		
	}
	
	public void addMouseMotionListener(MouseMotionListener mouseListener) {
		frame.addMouseMotionListener(mouseListener);
		canvasHost.addMouseMotionListener(mouseListener);		
	}
	
	public void removeMouseMotionListener(MouseMotionListener mouseListener) {
		frame.removeMouseMotionListener(mouseListener);
		canvasHost.removeMouseMotionListener(mouseListener);		
	}
	
	public void addMouseWheelListener(MouseWheelListener mouseListener) {
		frame.addMouseWheelListener(mouseListener);
		canvasHost.addMouseWheelListener(mouseListener);		
	}
	
	public void removeMouseWheelListener(MouseWheelListener mouseListener) {
		frame.removeMouseWheelListener(mouseListener);
		canvasHost.removeMouseWheelListener(mouseListener);		
	}
	
	public Component getHostComponent() {
		return canvasHost;
	}
	
	public Frame getHostFrame() {
		return frame;
	}
	
	/**
	 * Returns the absolute X position of the canvas that the engine is 
	 * rendering on.
	 * 
	 * @return X position in pixels
	 */
	public int canvasX() {
		return canvasHost.getX() + frame.getX();
	}
	
	/**
	 * Returns the absolute X position of the canvas that the engine is 
	 * rendering on.
	 * 
	 * @return Y position in pixels
	 */
	public int canvasY() {
		return canvasHost.getY() + frame.getY();
	}
	
	boolean fullscreen = false;
	public void toggleFullscreen() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		gd.setFullScreenWindow(frame);
	}
	
	/**
	 * Entry point for the basic application. At the moment, broken.
	 * @param args Unused.
	 */
	/*
	public static void main(String[] args) {
		
		Yeti yeti = Yeti.get();
		
		Frame frame = new Frame("Sup");
		frame.setSize(yeti.settings.width, yeti.settings.height);
		
		yeti.startRenderLoop(frame, frame, true);
	}//*/
}
