package barsan.opengl.rendering;

import java.util.Collections;
import java.util.Comparator;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.math.Vector2;
import barsan.opengl.math.Vector3;
import barsan.opengl.resources.ModelLoader;
import barsan.opengl.util.GLHelp;

/**
 * Functionality every renderer type has to implement.
 * 
 * @author Andrei Bârsan
 */
public abstract class Renderer {

	public static final Matrix4 shadowBiasMatrix = new Matrix4(new float[] {
		0.5f, 0.0f, 0.0f, 0.0f,
		0.0f, 0.5f, 0.0f, 0.0f,
		0.0f, 0.0f, 0.5f, 0.0f,
		0.5f, 0.5f, 0.5f, 1.0f
	});

	public static boolean renderDebug = true;
	
	protected Matrix4Stack matrixstack = new Matrix4Stack();
	
	public enum ShadowQuality {
		Low		(1, "Plain shadow mapping"),
		Medium	(2, "Added normal dependency"),
		High	(3, "16 Poisson Disk samples"),
		Ultra	(4, "4x Randomized Poisson sampling");
		
		private int shaderFlag;
		private String description;
		private ShadowQuality(int shaderFlag, String description) {
			this.shaderFlag = shaderFlag;
			this.description = description;
		}
		
		public int getFlag() {
			return shaderFlag;
		}
		
		public String getDescription() {
			return description;
		}
	}
	protected ShadowQuality shadowQuality = ShadowQuality.Medium;
	
	protected float omniShadowNear = 0.1f;
	protected float omniShadowFar = 100.0f;
	
	protected Vector3 directionalShadowCenter = new Vector3();
	protected Vector2 directionalShadowSize = new Vector2(100, 100);
	protected Vector2 directionalShadowDepth = new Vector2(-80, 100);
	
	protected boolean sortBillboards = true;
	protected GL3 gl;
	protected RendererState state;

	protected StaticModel screenQuad;
	
	public Renderer(GL3 gl) {
		state = new RendererState(this, gl);
		state.maxAnisotropySamples = (int)GLHelp.get1f(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT);
		
		this.gl = gl;
		
		// Setup the initial GL state
		gl.setSwapInterval(1);
		gl.glClearColor(0.33f, 0.33f, 0.33f, 1.0f);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_CULL_FACE);
		gl.glCullFace(GL2.GL_BACK);
		
		// Used in post-processing and debug rendering
		screenQuad = ModelLoader.buildQuadXY(2.0f, 2.0f);
		
	}
	
	public RendererState getState() {
		return state;
	}
	
	/**
	 * The heart of the renderer. Gathers any required data, sets the gl state\
	 * and performs the draw call(s).
	 */
	public abstract void render(Scene scene);
	
	/**
	 * Helper to sort a scene's billboards based on their distance to the camera.
	 */
	protected void sortBillboards(final Scene scene) {
		Collections.sort(scene.billboards, new Comparator<Billboard>() {
			@Override
			public int compare(Billboard o1, Billboard o2) {
				Vector3 cpos = scene.getCamera().getPosition();
				Float d1 = o1.getTransform().getTranslate().dist(cpos);
				Float d2 = o2.getTransform().getTranslate().dist(cpos);
				return d2.compareTo(d1);
			}
		});
	}
	
	public void dispose() {
		state.cubeTexture.destroy(gl);
	}
	
	public ShadowQuality getShadowQuality() {
		return shadowQuality;
	}

	public void setShadowQuality(ShadowQuality shadowQuality) {
		this.shadowQuality = shadowQuality;
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

	public Vector2 getDirectionalShadowDepth() {
		return directionalShadowDepth;
	}

	public void setDirectionalShadowDepth(Vector2 directionalShadowDepth) {
		assert directionalShadowDepth.x < directionalShadowDepth.y : "x = near; y = far; x must be smaller than y";
		this.directionalShadowDepth = directionalShadowDepth;
	}

	public Vector2 getDirectionalShadowSize() {
		return directionalShadowSize;
	}

	public void setDirectionalShadowSize(Vector2 directionalShadowSize) {
		this.directionalShadowSize = directionalShadowSize;
	}

	public boolean isSortBillboards() {
		return sortBillboards;
	}

	public void setSortBillboards(boolean sortBillboards) {
		this.sortBillboards = sortBillboards;
	}

	public Vector3 getDirectionalShadowCenter() {
		return directionalShadowCenter;
	}

	public void setDirectionalShadowCenter(Vector3 directionalShadowCenter) {
		this.directionalShadowCenter = directionalShadowCenter;
	}
}