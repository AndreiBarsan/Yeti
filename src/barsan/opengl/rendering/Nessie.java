package barsan.opengl.rendering;

import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import barsan.opengl.Yeti;
import barsan.opengl.math.MathUtil;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.lights.Light;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.lights.SpotLight;
import barsan.opengl.rendering.techniques.DRGeometryPass;
import barsan.opengl.rendering.techniques.DRLightPass;
import barsan.opengl.rendering.techniques.NullTechnique;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.GLHelp;
import barsan.opengl.util.Settings;

/**
 * Nessie is our Deferred Renderer. The development process will involve several
 * stages before it gets on par with the forward renderer, in terms of features.
 * In terms of speed it will already be blazingly fast!
 * 
 * Current bottleneck: GPU bandwidth
 * 
 * @author Andrei Bârsan
 */
public class Nessie extends Renderer {

	class GBuffer {
		private static final int POSITION_TEXTURE 	= 0;
		private static final int DIFFUSE_TEXTURE 	= 1;
		private static final int NORMAL_TEXTURE 	= 2;
		private static final int TEXCOORD_TEXTURE 	= 3;
		private static final int FINAL_TEXTURE		= 4;
		
		private static final int COMPONENT_COUNT 	= 5; 
		
		private int fboHandle = -1;	
		private int dtHandle = -1;
		private int handles[] = new int[COMPONENT_COUNT - 1];
		private int finalTexture = -1;
				
		private int width, height;
		
		public GBuffer(GL3 gl, int width, int height) {
			this.width = width;
			this.height = height;
			
			IntBuffer buff = IntBuffer.allocate(4);
			gl.glGenFramebuffers(1, buff);
			fboHandle = buff.get();
			if(fboHandle < 0) {
				fail("FBO creation failure.");
			}
			buff.clear();
			
			// Note: use GL2.GL_FRAMEBUFFER instead of GL2.GL_DRAW_FRAMEBUFFER
			gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fboHandle);
			gl.glGenTextures(handles.length, buff);
			
			int k = 0;
			while(buff.hasRemaining()) {
				int h = buff.get();
				if(h < 0) {
					fail("Color texture creation error.");
				}
				handles[k++] = h;
				// Bind the texture so we can work on it
				gl.glBindTexture(GL2.GL_TEXTURE_2D, h);
				// Actually allocate the texture data
				gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA32F, width, height, 0, GL2.GL_RGBA, GL2.GL_FLOAT, null);
				gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		        gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
				// Bind the texture to the FBO
				gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0 + k - 1, GL2.GL_TEXTURE_2D, h, 0);
			}
			buff.clear();
			
			gl.glGenTextures(1, buff);
			dtHandle = buff.get();
			if(dtHandle < 0) {
				fail("Could not create depth & stencil texture!");
			}
			
			gl.glBindTexture(GL2.GL_TEXTURE_2D, dtHandle);
			gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_DEPTH32F_STENCIL8, width, height, 0, GL2.GL_DEPTH_COMPONENT, GL2.GL_FLOAT, null);
			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_STENCIL_ATTACHMENT, GL2.GL_TEXTURE_2D, dtHandle, 0);

			buff.clear();
			gl.glGenTextures(1, buff);
			finalTexture = buff.get();
			if(finalTexture < 0) {
				fail("Could not create final texture!");
			}
			gl.glBindTexture(GL2.GL_TEXTURE_2D, finalTexture);
			gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, width, height, 0, GL2.GL_RGB, GL2.GL_FLOAT, null);
			gl.glFramebufferTexture2D(GL2.GL_DRAW_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0 + FINAL_TEXTURE, GL2.GL_TEXTURE_2D, finalTexture, 0);
			
			IntBuffer colorBuffers = IntBuffer.wrap(new int[] { 
					GL2.GL_COLOR_ATTACHMENT0,
					GL2.GL_COLOR_ATTACHMENT1,
					GL2.GL_COLOR_ATTACHMENT2,
					GL2.GL_COLOR_ATTACHMENT3
			});
			// Actually enables Multiple Render Targets, which we need for deferred rendering
			gl.glDrawBuffers(colorBuffers.remaining(), colorBuffers);
			GLHelp.fboErr(gl);
			gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
		}
		
		public void startFrame() {
			gl.glBindFramebuffer(GL2.GL_DRAW_FRAMEBUFFER, fboHandle);
			gl.glDrawBuffer(GL2.GL_COLOR_ATTACHMENT4);
			gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
		}
		
		public void bindForGeometryPass() {
			gl.glBindFramebuffer(GL2.GL_DRAW_FRAMEBUFFER, fboHandle);
			gl.glDrawBuffers(3, new int[] {
					GL2.GL_COLOR_ATTACHMENT0 + POSITION_TEXTURE,
					GL2.GL_COLOR_ATTACHMENT0 + DIFFUSE_TEXTURE,
					GL2.GL_COLOR_ATTACHMENT0 + NORMAL_TEXTURE,
					//GL2.GL_COLOR_ATTACHMENT0 + TEXCOORD_TEXTURE
			}, 0);
		}
		
		public void bindForStencilPass() {
			// No actual rendering during the stencil pass
			gl.glDrawBuffer(GL2.GL_NONE);
		}
		
		public void bindForLightPass() {
			if(mode == Mode.DrawGBuffer) {
				// Bind the FBO so we can blit from it
				gl.glBindFramebuffer(GL2.GL_READ_FRAMEBUFFER, fboHandle);
				gl.glDrawBuffer(GL2.GL_COLOR_ATTACHMENT0 + FINAL_TEXTURE);
			}
			else {
				gl.glDrawBuffer(GL2.GL_COLOR_ATTACHMENT0 + FINAL_TEXTURE);
				for(int i = 0; i < handles.length; ++i) {
					gl.glActiveTexture(GL2.GL_TEXTURE0 + i);	
					gl.glBindTexture(GL2.GL_TEXTURE_2D, handles[POSITION_TEXTURE + i]);
				}
			}
		}
		
		public void bindForFinalPass() {
			gl.glBindFramebuffer(GL2.GL_DRAW_FRAMEBUFFER, 0);
			gl.glBindFramebuffer(GL2.GL_READ_FRAMEBUFFER, fboHandle);
			gl.glReadBuffer(GL3.GL_COLOR_ATTACHMENT0 + FINAL_TEXTURE);
		}
		
		public void setReadBuffer(GL3 gl, int textureIndex) {
			gl.glReadBuffer(GL2.GL_COLOR_ATTACHMENT0 + textureIndex);
		}
		
		public void blitComponent(GL3 gl, int component, int x1, int y1, int x2, int y2) {
			setReadBuffer(gl, component);
		    gl.glBlitFramebuffer(0, 0, width, height,					// src
		                    x1, y1, x2, y2,								// dst
		                    GL2.GL_COLOR_BUFFER_BIT, GL2.GL_LINEAR);	// params
		}
		
		public void dispose(GL3 gl) {
			gl.glDeleteTextures(4, handles, 0);
			gl.glDeleteTextures(1, new int[] { dtHandle }, 0);
			gl.glDeleteFramebuffers(1, IntBuffer.wrap(new int[] { fboHandle }));
		}
		
		private void fail(String msg) {
			Yeti.screwed("Error creating GBuffer for the deferred renderer.\n" + msg);
		}
	}
	
	public enum Mode {
		DrawGBuffer,
		DrawLightVolumes,
		DrawComposedScene
	}
	
	public Mode mode;
	private GBuffer gbuffer;
	private static final String pre = "[NESSIE] ";
	private NullTechnique nullTechnique;
   	private DRLightPass lightPassTechnique;
	private DRGeometryPass geomPassTechnique;
	
	private Matrix4Stack nullStack = new Matrix4Stack();
	ModelInstance plVolume;
	ModelInstance slVolume;

	public Nessie(GL3 gl) {
		this(gl, Mode.DrawComposedScene);		
	}
	
	public Nessie(GL3 gl, Mode mode) {
		super(gl);
		this.mode = mode;
	}

	/**
	 * Called after the GL context is set up. 
	 */
	public void init() {
		nullTechnique = new NullTechnique();
	   	lightPassTechnique = new DRLightPass();
		geomPassTechnique = new DRGeometryPass(GBuffer.COMPONENT_COUNT);
		
		Settings s = Yeti.get().settings;
		gbuffer = new GBuffer(gl, s.width, s.height);
		Yeti.debug(pre + "Created GBuffer.");
		
		gl.glStencilOpSeparate(GL.GL_BACK, GL.GL_KEEP, GL.GL_INCR, GL.GL_KEEP);
		gl.glStencilOpSeparate(GL.GL_FRONT, GL.GL_KEEP, GL.GL_DECR, GL.GL_KEEP);
		
		plVolume = new StaticModelInstance(ResourceLoader.model("DR_sphere"));
		slVolume = new StaticModelInstance(ResourceLoader.model("DR_cone"));
	}
	
	@Override
	public void render(Scene scene) {
		state.setAnisotropySamples(Yeti.get().settings.anisotropySamples);
		state.setCamera(scene.getCamera());
		
		gbuffer.startFrame();
		geometryPass(scene);
		lightingPass(scene);
		finalizePass(scene);
		
		postProcessPass();
	}

	@Override
	public void dispose() {
		gbuffer.dispose(gl);		
	}
	
	private void geometryPass(Scene scene) {
		geomPassTechnique.setup(state);
		gbuffer.bindForGeometryPass();
		
		// Only the geometry pass updates the depth buffer
	    gl.glDepthMask(true);
	    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
	    gl.glEnable(GL2.GL_DEPTH_TEST);
	    
		geomPassTechnique.renderModelInstances(state, scene.modelInstances);
		
		// When we get here the depth buffer is already populated and the stencil pass
	    // depends on it, but it does not write to it.
	    gl.glDepthMask(false);	
	}	
	
	private void lightingPass(Scene scene) {
		// Note: technically, here we should draw on another framebuffer, in order
		// to support post-processing
		
		// Disable depth writing for this step
	    gl.glDepthMask(false);
	    gl.glDisable(GL2.GL_DEPTH_TEST);
		gbuffer.bindForLightPass();
		
		switch(mode) {
	    
	    case DrawGBuffer:
	    	
			int w = Yeti.get().settings.width;
			int h = Yeti.get().settings.height;
			int halfW = w / 2;
		    int halfH = h / 2;

		    // Just render the components of the GBuffer for testing
		    // Bottom left: POSITION
	    	gbuffer.blitComponent(gl, GBuffer.POSITION_TEXTURE, 0, 0, halfW, halfH);
		    // Top left: DIFFUSE
	    	gbuffer.blitComponent(gl, GBuffer.DIFFUSE_TEXTURE, 0, halfH, halfW, h);
		    // Top right: NORMAL
		    gbuffer.blitComponent(gl, GBuffer.NORMAL_TEXTURE, halfW, halfH, w, h);	
		    // Bottom right: TEXCOORD
		    gbuffer.blitComponent(gl, GBuffer.TEXCOORD_TEXTURE, halfW, 0, w, halfH);
		break;
		
	    case DrawLightVolumes:
	       	gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
	       	
	    	break;
	    	
	    case DrawComposedScene:	    	
	       	gl.glEnable(GL2.GL_STENCIL_TEST);
	       	// TODO: technically, the whole loop could go into the technique
			for(Light l : scene.lights) {
				renderLightVolume(l);
			}
			gl.glDisable(GL2.GL_STENCIL_TEST);
	    	break;
	    }

		// Important to reset this, to allow font rendering and other stuff
		// that expect the default texture unit to be active to work
		gl.glActiveTexture(GL2.GL_TEXTURE0);
	}
			
	
	private void renderLightVolume(Light light) {
		// Perform the stencil step
		nullTechnique.setup(state);
		
		gbuffer.bindForStencilPass();
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_CULL_FACE);
		gl.glClear(GL2.GL_STENCIL_BUFFER_BIT);
		
		// Stencil operations are simply set once, in the init() method
		gl.glStencilFunc(GL2.GL_ALWAYS, 0, 0);
		
		switch(light.getType()) {
			case Directional:
				break;
				
			case Point:
				renderPLVol((PointLight)light);
				break;
				
			case Spot:
				renderSLVol((SpotLight)light);
				break;
		}
		
       	gl.glCullFace(GL2.GL_BACK);
       	gl.glDisable(GL2.GL_BLEND);
	}

	private void prepareLightPass(RendererState state) {
		// Render the actual light volume
		gbuffer.bindForLightPass();
       	lightPassTechnique.setup(state);
       	gl.glStencilFunc(GL2.GL_NOTEQUAL, 0, 0xFF);
       	gl.glDisable(GL2.GL_DEPTH_TEST);	// finally done with the depth test!
       	
    	gl.glEnable(GL2.GL_BLEND);
      	gl.glBlendEquation(GL2.GL_FUNC_ADD);
      	gl.glBlendFunc(GL2.GL_ONE, GL2.GL_ONE);
      	
      	gl.glEnable(GL2.GL_CULL_FACE);
      	gl.glCullFace(GL2.GL_FRONT);
	}
	
	private void renderPLVol(PointLight l) {
		// Compute transform for the null pass
		// TODO: cleaner code
		plVolume.getTransform()
			.setTranslate(l.getPosition())
			.setScale(l.getBoundingRadius())
			.refresh();
		
		nullTechnique.renderDude(plVolume, state, nullStack);
		
		prepareLightPass(state);
       	lightPassTechnique.drawPointLight(l, state);
	}
	 
	final static float SPOT_RED = 2.0f; 
	private void renderSLVol(SpotLight l) {
		float h = l.getBoundingRadius() * SPOT_RED;
		float w = (float)( h * (Math.tan( 1.25f * Math.acos(l.getCosOuter()))));
		
		Vector3 lightDir = l.getDirection();
		Vector3 pos = l.getPosition();//.copy().add(lightDir.copy().mul(-1.0f));
		Vector3 axis;
		if(lightDir.equals(Vector3.Y)) {
			axis = Vector3.X;
		} else if(lightDir.equals(Vector3.Y.copy().inv())) {
			axis = Vector3.X;			
		}
		else {
			axis = new Vector3(lightDir).cross(Vector3.Y);
		}
		
		float angle = 180.0f + MathUtil.RAD_TO_DEG * (float)Math.acos(Vector3.Y.dot(lightDir));
		
		// Compute cone scale and rotation based on the light
		slVolume.getTransform()
			.setTranslate(pos)
			.setScale(w, h, w)
			.setRotation(axis, angle)
			.refresh();
		
		nullTechnique.renderDude(slVolume, state, nullStack);
		prepareLightPass(state);
       	//gl.glDisable(GL2.GL_STENCIL_TEST);
		lightPassTechnique.drawSpotLight(slVolume, l, state);
	}
	
	public void finalizePass(Scene scene) {
		gbuffer.bindForFinalPass();
		gl.glBlitFramebuffer(	0, 0, gbuffer.width, gbuffer.height,
								0, 0, gbuffer.width, gbuffer.height,
								GL2.GL_COLOR_BUFFER_BIT, GL2.GL_LINEAR);
	}
	
	class Effect {
		private void apply(int srcHandle, int dstHandle) {
			// Perform necessary computations from src to dst
			// NOTE: should have n inputs and m outputs
		}
	}
		
	// Just blocking out what it's supposed to look like
	ArrayList<Effect> fx = new ArrayList<>();
	private void postProcessPass() {
		int srcHandle = 0,
			dstHandle = 0, 
			aux;
		
		for(Effect effect : fx) {
			effect.apply(srcHandle, dstHandle);
			aux = srcHandle;
			srcHandle = dstHandle;
			dstHandle = aux;
		}
		
		if(fx.size() % 2 == 0) {
			// render src to screen
		} else {
			// render dst to screen
		}
	}
}
