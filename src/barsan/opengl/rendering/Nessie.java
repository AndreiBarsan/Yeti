package barsan.opengl.rendering;

import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.rendering.lights.Light;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.materials.DRGeometryMaterial;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.GLHelp;
import barsan.opengl.util.Settings;

/**
 * Nessie is our Deferred Renderer. The development process will involve several
 * stages before it gets on par with the forward renderer, in terms of features.
 * In terms of speed it will already be blazingly fast!
 * 
 * @author Andrei Bârsan
 */
public class Nessie extends Renderer {

	class GBuffer {
		private static final int POSITION_TEXTURE 	= 0;
		private static final int DIFFUSE_TEXTURE 	= 1;
		private static final int NORMAL_TEXTURE 	= 2;
		private static final int TEXCOORD_TEXTURE 	= 3;
		
		private static final int COMPONENT_COUNT 	= 4; 
		
		private int fboHandle = -1;	
		private int dtHandle = -1;
		private int handles[] = new int[COMPONENT_COUNT];
				
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
			gl.glGenTextures(COMPONENT_COUNT, buff);
			
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
				gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGB32F, width, height, 0, GL2.GL_RGB, GL2.GL_FLOAT, null);
				gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		        gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
				// Bind the texture to the FBO
				gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0 + k - 1, GL2.GL_TEXTURE_2D, h, 0);
			}
			buff.clear();
			
			gl.glGenTextures(1, buff);
			dtHandle = buff.get();
			if(dtHandle < 0) {
				fail("Could not create depth texture!");
			}
			
			gl.glBindTexture(GL2.GL_TEXTURE_2D, dtHandle);
			gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_DEPTH_COMPONENT32F, width, height, 0, GL2.GL_DEPTH_COMPONENT, GL2.GL_FLOAT, null);
			gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, GL2.GL_TEXTURE_2D, dtHandle, 0);

			IntBuffer colorBuffers = IntBuffer.wrap(new int[] { 
					GL2.GL_COLOR_ATTACHMENT0,
					GL2.GL_COLOR_ATTACHMENT1,
					GL2.GL_COLOR_ATTACHMENT2,
					GL2.GL_COLOR_ATTACHMENT3
			});
			// Actually enables Multiple Render Targets, which we need for deferred rendering
			gl.glDrawBuffers(COMPONENT_COUNT, colorBuffers);
			GLHelp.fboErr(gl);
			gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
		}
		
		public void bindForReading(GL3 gl) {
			if(mode == Mode.DrawGBuffer) {
				// Bind the FBO so we can blit from it
				gl.glBindFramebuffer(GL2.GL_READ_FRAMEBUFFER, fboHandle);
			} else {
				// Bind the textures themselves so we can sample from them
				gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
				for(int i = 0; i < COMPONENT_COUNT; ++i) {
					gl.glActiveTexture(GL2.GL_TEXTURE0 + i);	
					gl.glBindTexture(GL2.GL_TEXTURE_2D, handles[POSITION_TEXTURE + i]);
				}
			}
		}
		
		public void bindForWriting(GL3 gl) {
			gl.glBindFramebuffer(GL2.GL_DRAW_FRAMEBUFFER, fboHandle);
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
	
	public Nessie(GL3 gl) {
		// Start in debug mode by default
		//this(gl, Mode.DrawGBuffer);
		this(gl, Mode.DrawComposedScene);
	}
	
	public Nessie(GL3 gl, Mode mode) {
		super(gl);
		this.mode = mode;
		Settings s = Yeti.get().settings;
		gbuffer = new GBuffer(gl, s.width, s.height);
		Yeti.debug(pre + "Created GBuffer.");
	}

	@Override
	public void render(Scene scene) {
		geometryPass(scene);
		lightingPass(scene);
		postProcessPass();
	}

	@Override
	public void dispose() {
		gbuffer.dispose(gl);		
	}
	
	private void geometryPass(Scene scene) {
		state.setCamera(scene.getCamera());
		gbuffer.bindForWriting(gl);
		
		// Only the geometry pass updates the depth buffer
	    gl.glDepthMask(true);
	    gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
	    gl.glEnable(GL2.GL_DEPTH_TEST);
	    gl.glDisable(GL2.GL_BLEND);		
		
		// Always use the same material designed to render to the GBuffer's MRT format
		state.forceMaterial(new DRGeometryMaterial());
		for(ModelInstance modelInstance : scene.modelInstances) {
			modelInstance.render(state, matrixstack);
			assert matrixstack.getSize() == 1 : "Matrix stack should be back to 1, instead was " + matrixstack.getSize();
		}
		
		// No more writing to the depth buffer this frame!
	    gl.glDepthMask(false);
	    gl.glDisable(GL2.GL_DEPTH_TEST);
	}
	
	private void lightingPass(Scene scene) {
		// Note: technically, here we should draw on another framebuffer, in order
		// to support post-processing
		switch(mode) {
	    
	    case DrawGBuffer:
	    	gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
			gbuffer.bindForReading(gl);
			
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
	    	break;
	    	
	    case DrawComposedScene:
	    	gl.glEnable(GL2.GL_BLEND);
	      	gl.glBlendEquation(GL2.GL_FUNC_ADD);
	      	gl.glBlendFunc(GL2.GL_ONE, GL2.GL_ONE);

	      	gbuffer.bindForReading(gl);
	       	gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
	    	
	       	DRLightPass lightPass = new DRLightPass();
	       	lightPass.setup(gbuffer, state);
	       	// TODO: technically, the whole loop could go into the technique
			for(Light l : scene.lights) {
				switch(l.getType()) {
				case Directional:
					// TODO
					break;
					
				case Point:
					lightPass.drawPointLight((PointLight)l, state);
					break;
					
				case Spot:
					// TODO
					break;
				}
			}

	    	break;
	    }
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
