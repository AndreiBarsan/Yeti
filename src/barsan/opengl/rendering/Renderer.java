package barsan.opengl.rendering;

import java.util.Collections;
import java.util.Comparator;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GL3;
import javax.media.opengl.GLException;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Model.Face;
import barsan.opengl.rendering.lights.PointLight;
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
	private FBObject fbo_tex, fbo_ren;
	private Matrix4Stack matrixstack = new Matrix4Stack();
	
	TextureAttachment tta;
	final int[] name = new int[] { -1 };
	
	int texType = -1;
	int regTexHandle = -1;
	
	boolean MSAAEnabled = true;
	private int MSAASamples = 4;
		
	public Renderer(GL2 gl) {	
		state = new RendererState(gl);
		state.maxAnisotropySamples = (int)GLHelp.get1f(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT);
		
		int fboWidth = Yeti.get().settings.width;
		int fboHeight = Yeti.get().settings.height;
		
		fbo_tex = new FBObject();
		fbo_tex.reset(gl, fboWidth, fboHeight);
		fbo_tex.bind(gl);
		
		fbo_tex.attachTexture2D(gl, 0, true);
		//*
		TextureAttachment ta = new TextureAttachment(
				Attachment.Type.COLOR_TEXTURE,
				GL.GL_RGBA8,
				fboWidth,
				fboHeight, 
				GL2.GL_BGRA, // !!!
				GL2GL3.GL_UNSIGNED_INT_8_8_8_8_REV, // !!! GL.GL_UNSIGNED_BYTE for non-alpha stuff
				GL.GL_LINEAR, 
				GL.GL_LINEAR,	// ! Might get away with GL_NEAREST
				GL.GL_CLAMP_TO_EDGE,
				GL.GL_CLAMP_TO_EDGE,
				0);
		//*/
		//fbo_tex.attachTexture2D(gl, 0, ta);
		if(MSAAEnabled) {
			texType = GL2.GL_TEXTURE_2D_MULTISAMPLE;
		} else {
			texType = GL2.GL_TEXTURE_2D;
		}
		//*
		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, fbo_tex.getWriteFramebuffer());
        gl.glGenTextures(1, name, 0);
        gl.glBindTexture(texType, name[0]);
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
				//*/
		// FIXME: leaks the texture
		
		// Blah blah, basic
        //fbo_tex.attachRenderbuffer(gl, Attachment.Type.DEPTH, 32);
		/* NOPE, doesn't work, since the way samples are set -> JOGL == retarded
		 * (in JOGL, in order to use the default FBObject functionality, one has
		 *  to specify a number of samples and stick with it. makes sense, of course.
		 *  BUT if you want to use MS, you can't use textures with JOGL, since
		 *  it complains when you use textures and more than 0 samples. So if you
		 *  roll your own MS texture support, you need to roll your own depth
		 *  buffer/ depth texture support.
		 *  )
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
		
		quad = new Model(gl, "derp");
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
		quad.master.faces.add(mainFace);
		quad.setPointsPerFace(4);
		quad.buildVBOs();
	}
	
	public RendererState getState() {
		return state;
	}
	
	Model quad;
	public void render(final Scene scene) {
		GL2 gl = state.gl;
		state.setAnisotropySamples(Yeti.get().settings.anisotropySamples);
		gl.glDepthMask(true);
		
		// Render to our framebuffer
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fbo_tex.getWriteFramebuffer());
		renderScene(gl, scene);
		//renderDebug(gl, scene);		
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
		quad.getVertices().use(pindex);

		int tindex = pps.getAttribLocation(Shader.A_TEXCOORD);
		quad.getTexcoords().use(tindex);
		
		// This is where the magic happens!
		// The texture we rendered on is passed as an input to the second stage!
		gl.glActiveTexture(GLHelp.textureSlot[0]);
		gl.glBindTexture(texType, name[0]);
		
		gl.glDrawArrays(GL2.GL_QUADS, 0, quad.getVertices().getSize());
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		
		//fbo_tex.unuse(gl);
		gl.glBindTexture(texType, 0); 
		
		quad.getVertices().cleanUp(pindex);
		quad.getTexcoords().cleanUp(tindex);
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
	
	private void renderDebug(GL2 gl, Scene scene) {
		FPCameraAdapter ca = new FPCameraAdapter(scene.camera);
		GLUT glut = new GLUT();
		ca.prepare(gl);
		gl.glBegin(GL2.GL_TRIANGLES);
			for(PointLight pl : scene.pointLights) {
				gl.glTranslatef(pl.getPosition().x, pl.getPosition().y, pl.getPosition().z);
				glut.glutSolidSphere(0.5d, 10, 10);
			}
		gl.glEnd();
		gl.glPopMatrix();

	}
}
