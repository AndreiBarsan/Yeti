package barsan.opengl.rendering;

import java.util.Collections;
import java.util.Comparator;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GL3;
import javax.media.opengl.GLException;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Model.Face;
import barsan.opengl.rendering.lights.Light;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.lights.SpotLight;
import barsan.opengl.rendering.lights.Light.LightType;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.FPCameraAdapter;
import barsan.opengl.util.GLHelp;

import com.jogamp.opengl.FBObject;
import com.jogamp.opengl.FBObject.Attachment;
import com.jogamp.opengl.FBObject.Attachment.Type;
import com.jogamp.opengl.FBObject.ColorAttachment;
import com.jogamp.opengl.FBObject.RenderAttachment;
import com.jogamp.opengl.FBObject.TextureAttachment;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;

public class Renderer {

	private RendererState state;
	private FBObject fbo_tex;
	private FBObject fbo_shadows;
	private Matrix4Stack matrixstack = new Matrix4Stack();
	
	TextureAttachment tta;
	
	int texType = -1;
	int regTexHandle = -1;
	
	boolean MSAAEnabled = true;
	private int MSAASamples = 4;
	private Model screenQuad;
	
	public boolean shadowsEnabled = false;
	
	// TODO: multiply the lightMVP with this before sending it to the 
	// main rendering pass
	private static final Matrix4 shadowBiasMatrix = new Matrix4(new float[] 
			{
				0.5f, 0.0f, 0.0f, 0.0f,
				0.0f, 0.5f, 0.0f, 0.0f,
				0.0f, 0.0f, 0.5f, 0.0f,
				0.0f, 0.0f, 0.0f, 1.0f
			}); 
		
	public Renderer(GL2 gl) {	
		state = new RendererState(gl);
		state.maxAnisotropySamples = (int)GLHelp.get1f(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT);
		
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
		
		int fboWidth = Yeti.get().settings.width;
		int fboHeight = Yeti.get().settings.height;
		
		fbo_tex = new FBObject();
		fbo_tex.reset(gl, fboWidth, fboHeight, 0);
		fbo_tex.bind(gl);
		
		fbo_tex.attachTexture2D(gl, 0, true);
		
		if(MSAAEnabled) {
			texType = GL2.GL_TEXTURE_2D_MULTISAMPLE;
		} else {
			texType = GL2.GL_TEXTURE_2D;
		}
		
		final int[] name = new int[] { -1 };
		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, fbo_tex.getWriteFramebuffer());
        gl.glGenTextures(1, name, 0);
        regTexHandle = name[0];
        gl.glBindTexture(texType, regTexHandle);
        if(MSAAEnabled) {
        	gl.glTexImage2DMultisample(texType, MSAASamples, GL.GL_RGBA8, fboWidth, fboHeight, true);
        } else {
        	gl.glTexImage2D(texType, 0, GL.GL_RGBA8, fboWidth, fboHeight, 0, GL2.GL_BGRA, GL2GL3.GL_UNSIGNED_INT_8_8_8_8_REV, null);
        	gl.glTexParameteri(texType, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(texType, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(texType, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(texType, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        }
        
		gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0 + 0, texType, name[0], 0);
		
        //fbo_tex.attachRenderbuffer(gl, Attachment.Type.DEPTH, 32);
		/* 
		 * NOPE, this doesn't work with multisampling. JOGL doesn't help you here.
		 * In JOGL, in order to use the default FBObject functionality, one has
		 *  to specify a number of samples when creating the FBObject and stick
		 *  with it. This makes sense, of course. To an extent.
		 *  If you want to use MS with FBOs (which you kind of have to, if you 
		 *  plan to do multi-pass rendering), you have to render on multi-sampled
		 *  textures. And you can't use those textures with JOGL framebuffers, since
		 *  it complains when you use textures and more than 0 samples. 
		 *  
		 *  And don't even think about just binding MS textures to the FBO using
		 *  the JOGL functionality. GL_TEXTURE_2D is hardcoded everywhere. :(
		 *  
		 *  So if you want to roll your own MS texture support, you also need to
		 *  roll your own color/ depth texture support.
		 *  
		 *   Dang.
		 */
		if(MSAAEnabled) {
			RenderAttachment depth = new RenderAttachment(Type.DEPTH, GL.GL_DEPTH_COMPONENT32, MSAASamples, fboWidth, fboHeight, 0);
			depth.initialize(gl);
			gl.glFramebufferRenderbuffer(GL.GL_FRAMEBUFFER, GL.GL_DEPTH_ATTACHMENT,	GL.GL_RENDERBUFFER, depth.getName());
		} else {
			fbo_tex.attachRenderbuffer(gl, Attachment.Type.DEPTH, 32);
		}
		
		
		fbo_tex.unbind(gl);
		
		screenQuad = new Model(gl, "derp");
		Face mainFace = new Face();
		mainFace.points = new Vector3[] {
				new Vector3(-1.0f, -1.0f, 0.0f),
				new Vector3(-1.0f, 1.0f,  0.0f),
				new Vector3( 1.0f, 1.0f,  0.0f),
				new Vector3( 1.0f, -1.0f, 0.0f)
		};
		mainFace.texCoords = new Vector3[] {
				new Vector3(0.0f, 0.0f, 0.0f),
				new Vector3(0.0f, 1.0f, 0.0f),
				new Vector3(1.0f, 1.0f, 0.0f),
				new Vector3(1.0f, 0.0f, 0.0f)
		};
		screenQuad.master.faces.add(mainFace);
		screenQuad.setPointsPerFace(4);
		screenQuad.buildVBOs();
		
		// Prepare shadow mapping
		fbo_shadows = new FBObject();
		int shadowMapW = 2048;
		int shadowMapH = 2048;
		fbo_shadows.reset(gl, shadowMapW, shadowMapH, 0);
		
	}
	
	public RendererState getState() {
		return state;
	}
		
	public void render(final Scene scene) {
		GL2 gl = state.gl;
		state.setAnisotropySamples(Yeti.get().settings.anisotropySamples);
		gl.glDepthMask(true);
		
		if(shadowsEnabled) {
			gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fbo_shadows.getWriteFramebuffer());
			renderShadows(gl, scene);
			gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fbo_shadows.getWriteFramebuffer());
			
			// Bind the shadowmap to a certain slot; 
			// TODO: rewrite system to allow this - right slots are allocated 
			// starting from 0 by the materials; the renderer needs to be 
			// able to use slots too!
		}
		
		// Render to our framebuffer
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fbo_tex.getWriteFramebuffer());
		renderScene(gl, scene);
		renderDebug(gl, scene);		
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);	// Unbind
		
		// Clear the main (screen) FrameBuffer
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		// Begin post-processing
		Shader pps;
		
		if(MSAAEnabled) {
			pps = ResourceLoader.shader("postProcessMSAA");
			gl.glUseProgram(pps.handle);
			
			pps.setU1i("sampleCount", MSAASamples);
		} else {
			pps = ResourceLoader.shader("postProcess");
			gl.glUseProgram(pps.handle);
		}
		pps.setU1i("colorMap", 0);
		
		int pindex = pps.getAttribLocation(Shader.A_POSITION);
		screenQuad.getVertices().use(pindex);

		int tindex = pps.getAttribLocation(Shader.A_TEXCOORD);
		screenQuad.getTexcoords().use(tindex);
		
		// This is where the magic happens!
		// The texture we rendered on is passed as an input to the second stage!
		gl.glActiveTexture(GLHelp.textureSlot[0]);
		gl.glBindTexture(texType, regTexHandle);
		
		gl.glDrawArrays(GL2.GL_QUADS, 0, screenQuad.getVertices().getSize());
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		
		fbo_tex.unuse(gl);
		gl.glBindTexture(texType, 0); 
		
		screenQuad.getVertices().cleanUp(pindex);
		screenQuad.getTexcoords().cleanUp(tindex);
	}
	
	public void dispose(GL2 gl) {
		fbo_tex.destroy(gl);
	}
	
	private void renderScene(GL2 gl, final Scene scene) {
		/*		    _.' :  `._                                            
       		    .-.'`.  ;   .'`.-.                                        
       __      / : ___\ ;  /___ ; \      __                               
     ,'_ ""--.:__;".-.";: :".-.":__;.--"" _`,                             
     :' `.t""--.. '<@.`;_  ',@>` ..--""j.' `;                             
		  `:-.._J '-.-'L__ `-- ' L_..-;'                                  
		    "-.__ ;  .-"  "-.  : __.-"    Clear the bound buffer 
		        L ' /.------.\ ' J           you must!
		         "-.   "--"   .-"         
		        __.l"-:_JL_;-";.__        
		     .-j/'.;  ;""""  / .'\"-.                                     
		*/
		// At first I was clearing the default fbo, then binding the auxiliary
		// one, forcing the depth and color bits to never actually get cleared!
		// Nice one. 26.12.2012
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		// The transparent fog needs this, among other things
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);		
		
		for(ModelInstance modelInstance : scene.modelInstances) {
			modelInstance.render(state, matrixstack);
			// Make sure everyone pops what they push. If you know what I mean! ;)
			// ...It's matrices :(
			assert matrixstack.getSize() == 1;
		}
		
		// Sort and render the non-additionally-blended billboards
		Collections.sort(scene.billbords, new Comparator<Billboard>() {
			@Override
			public int compare(Billboard o1, Billboard o2) {
				Vector3 cpos = scene.getCamera().getPosition();
				Float d1 = o1.getTransform().getTranslate().dist(cpos);
				Float d2 = o2.getTransform().getTranslate().dist(cpos);
				return d2.compareTo(d1);
			}
		});
		
		// Render the billboards separately
		for(Billboard b : scene.billbords) {
			b.render(state, matrixstack);
		}
	}
	
	private void renderShadows(GL2 gl, Scene scene) {
		// TODO: simple render pass FORCING everything to use the depthWriter material
	}
	
	private void renderDebug(GL2 gl, Scene scene) {
		FPCameraAdapter ca = new FPCameraAdapter(scene.camera);
		gl.glUseProgram(0);
		GLUT glut = new GLUT();
		ca.prepare(gl);
		for(Light l : scene.lights) {
			if(l.getType() != LightType.Directional) {
				PointLight pl = (PointLight)l;
				if(l.getType() == LightType.Point) {
					gl.glTranslatef(pl.getPosition().x, pl.getPosition().y, pl.getPosition().z);
					glut.glutSolidSphere(0.5d, 64, 64);
				}
				// need quaternion slerp to align a spotlight cone to the 
				// spotlight direction
			} 
		}
		gl.glPopMatrix();

	}
}
