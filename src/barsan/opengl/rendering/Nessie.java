package barsan.opengl.rendering;

import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import barsan.opengl.Yeti;
import barsan.opengl.rendering.lights.DirectionalLight;
import barsan.opengl.rendering.lights.Light;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.lights.SpotLight;
import barsan.opengl.rendering.materials.DRGeometryMaterial;
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
				
		public GBuffer(GL3 gl, int width, int height) {
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
			gl.glBindFramebuffer(GL2.GL_READ_FRAMEBUFFER, fboHandle);
			
		}
		
		public void bindForWriting(GL3 gl) {
			gl.glBindFramebuffer(GL2.GL_DRAW_FRAMEBUFFER, fboHandle);
		}
		
		public void setReadBuffer(GL3 gl, int textureIndex) {
			gl.glReadBuffer(GL2.GL_COLOR_ATTACHMENT0 + textureIndex);
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
	
	GBuffer gbuffer;
	Mode mode;
	private static final String pre = "[NESSIE] ";
	
	public Nessie(GL3 gl) {
		// Start in debug mode by default
		this(gl, Mode.DrawGBuffer);
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
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		// Always use the same material designed to render to the GBuffer's MRT format
		state.forceMaterial(new DRGeometryMaterial());
		for(ModelInstance modelInstance : scene.modelInstances) {
			modelInstance.render(state, matrixstack);
			assert matrixstack.getSize() == 1 : "Matrix stack should be back to 1, instead was " + matrixstack.getSize();
		}
	}
	
	private void lightingPass(Scene scene) {
		// Note: technically, here we should draw on another framebuffer, in order
		// to support post-processing
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
		gbuffer.bindForReading(gl);
		gbuffer.setReadBuffer(gl, GBuffer.POSITION_TEXTURE);
		
		int width = Yeti.get().settings.width;
		int height = Yeti.get().settings.height;
		int HalfWidth = width / 2;
	    int HalfHeight = height / 2;

	    switch(mode) {
	    
	    case DrawGBuffer:
		    // Just render the components of the GBuffer for testing
		    // Bottom left: POSITION
		    gbuffer.setReadBuffer(gl, GBuffer.POSITION_TEXTURE);
		    gl.glBlitFramebuffer(0, 0, width, height,
		                    0, 0, HalfWidth, HalfHeight, GL2.GL_COLOR_BUFFER_BIT, GL2.GL_LINEAR);
	
		    // Top left: DIFFUSE
		    gbuffer.setReadBuffer(gl, GBuffer.DIFFUSE_TEXTURE);
		    gl.glBlitFramebuffer(0, 0, width, height, 
		                    0, HalfHeight, HalfWidth, height, GL2.GL_COLOR_BUFFER_BIT, GL2.GL_LINEAR);
	
		    // Top right: NORMAL
		    gbuffer.setReadBuffer(gl, GBuffer.NORMAL_TEXTURE);
		    gl.glBlitFramebuffer(0, 0, width, height, 
		                    HalfWidth, HalfHeight, width, height, GL2.GL_COLOR_BUFFER_BIT, GL2.GL_LINEAR);
	
		    // Bottom right: TEXCOORD
		    gbuffer.setReadBuffer(gl, GBuffer.TEXCOORD_TEXTURE);
		    gl.glBlitFramebuffer(0, 0, width, height, 
		                    HalfWidth, 0, width, HalfHeight, GL2.GL_COLOR_BUFFER_BIT, GL2.GL_LINEAR);
		break;
		
	    case DrawLightVolumes:
	    	break;
	    	
	    case DrawComposedScene:
			// Render a bunch of cones and spherers (the lights)
			// Read in the GBuffer in their FShaders and output the corresponding
			// light surfaces to the FrameBuffer (well, actually to the postProcess
			// control)
			
			// set super-uniforms (bind g-buffer textures, shadow map)
			// render all lights as spheres (point) / cones (spot) / quads (directional)
			
			for(Light l : scene.lights) {
				if(l instanceof PointLight) {
					// Render sphere with radius based on the light's fade parameters
					// TODO: point/stop should have an effect range function
					
					// Pass in light position, fade params, colors
				} else if(l instanceof SpotLight) {
					// Render cone with height based on the light's fade parameters
					// and top angle based on the light's angle
					
					// Pass in light position, orientation, angles, exp, fade params, colors
				} else if(l instanceof DirectionalLight) {
					// Render a full screen quad
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
