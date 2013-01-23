package barsan.opengl.rendering;

import java.security.interfaces.RSAKey;
import java.util.Collections;
import java.util.Comparator;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GL3;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.lights.DirectionalLight;
import barsan.opengl.rendering.lights.Light;
import barsan.opengl.rendering.lights.Light.LightType;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.lights.SpotLight;
import barsan.opengl.rendering.materials.DepthWriterDirectional;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.FPCameraAdapter;
import barsan.opengl.util.GLHelp;

import com.jogamp.opengl.FBObject;
import com.jogamp.opengl.FBObject.Attachment;
import com.jogamp.opengl.FBObject.Attachment.Type;
import com.jogamp.opengl.FBObject.RenderAttachment;
import com.jogamp.opengl.FBObject.TextureAttachment;
import com.jogamp.opengl.util.gl2.GLUT;

public class Renderer {

	private RendererState state;
	private FBObject fbo_tex;
	private FBObject fbo_shadows;
	private Matrix4Stack matrixstack = new Matrix4Stack();
	
	TextureAttachment tta;
	
	int texType = -1;
	int regTexHandle = -1;
	
	int shadowMapW = 4096;
	int shadowMapH = 4096;
	
	boolean MSAAEnabled = true;
	private int MSAASamples = 4;
	private Model screenQuad;
	
	public static final Matrix4 shadowBiasMatrix = new Matrix4(new float[] 
			{
				0.5f, 0.0f, 0.0f, 0.0f,
				0.0f, 0.5f, 0.0f, 0.0f,
				0.0f, 0.0f, 0.5f, 0.0f,
				0.5f, 0.5f, 0.5f, 1.0f
			});
	// 18.01.2013 - make sure you write your matrices down right! If this matrix,
	// for instance, is missing the 0.5fs from the last line, you won't see any
	// shadows!
	
	public Renderer(GL3 gl) {	
		state = new RendererState(gl);
		state.maxAnisotropySamples = (int)GLHelp.get1f(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT);
		
		System.out.println("DERP: " + state.maxAnisotropySamples);
		
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
		//*
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
        
		gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0 + 0, texType, regTexHandle, 0);
		//*/
		
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
		
		screenQuad = Model.buildQuad(2.0f, 2.0f, false);
		
		// Prepare shadow mapping
		fbo_shadows = new FBObject();
		fbo_shadows.reset(gl, shadowMapW, shadowMapH, 0);
		fbo_shadows.bind(gl);
		
		gl.glGenTextures(1, name, 0);
		state.shadowTexture = name[0];
		gl.glBindTexture(GL2.GL_TEXTURE_2D, state.shadowTexture);
		gl.glTexImage2D(GL2.GL_TEXTURE_2D,
				0,
				GL2.GL_DEPTH_COMPONENT16, 
				shadowMapW, shadowMapH,
				0,
				GL2.GL_DEPTH_COMPONENT,
				GL2.GL_UNSIGNED_BYTE, //GL2.GL_FLOAT, 
				null);
		 gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		 gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		 gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
		 gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
		 gl.glTexParameterfv(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_BORDER_COLOR, new float[] {0.0f, 0.0f, 0.0f, 0.0f }, 0);
		 //gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_COMPARE_FUNC, GL2.GL_LEQUAL);
		 //gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_COMPARE_MODE, GL2.GL_COMPARE_R_TO_TEXTURE);	
		 
		 gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, GL2.GL_TEXTURE_2D, state.shadowTexture, 0);	
		 
		 gl.glDrawBuffer(GL2.GL_NONE);
		 GLHelp.fboErr(gl);		
		 
		 fbo_shadows.unbind(gl);
	}
	
	public RendererState getState() {
		return state;
	}
		
	public void render(final Scene scene) {
		GL3 gl = state.gl;
		state.setAnisotropySamples(Yeti.get().settings.anisotropySamples);
		
		// Get the original viewport size; We cannot rely on Yeti's dimensions
		// since the GLJPanel is doing witchcraft which results in a viewport
		// with a greater height than it's supposed to
		int oldDim[] = new int[4];
		gl.glGetIntegerv(GL2.GL_VIEWPORT, oldDim, 0);
		
		prepareBillboards(scene);
		
		boolean canCast = false;
		if(state.getLights().get(0).getType() != LightType.Point) {
			// Placeholder test
			canCast = true;
		}
		
		if(scene.shadowsEnabled && canCast) {
			gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fbo_shadows.getWriteFramebuffer());
			gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
			//gl.glCullFace(GL2.GL_FRONT);
			state.forceMaterial(new DepthWriterDirectional());
			
			Light light = state.getLights().get(0);
			Camera aux = state.getCamera();
			
			if(light.getType() == LightType.Directional) {
				DirectionalLight dlight = (DirectionalLight)light;
				
				// TODO: alternative - this is quite dirty	
				OrthographicCamera oc = new OrthographicCamera(100, 100);
				oc.setFrustumFar(80);
				oc.setFrustumNear(-80);
				
				Vector3 ld = dlight.getDirection();
				oc.lookAt(ld, Vector3.ZERO, Vector3.UP);
				state.setCamera(oc);
				state.depthProjection = oc.getProjection().cpy();
				state.depthView = oc.getView().cpy();
			} else if(light.getType() == LightType.Spot) {
				SpotLight slight = (SpotLight)light;
				
				Vector3 camDir = slight.getDirection().copy();
				//camDir.y = -camDir.y;
				
				PerspectiveCamera pc = new PerspectiveCamera(
						slight.getPosition().copy(),
						camDir,
						shadowMapW, 
						shadowMapH); 	//..?
				pc.setFOV(80.0f);
				pc.setFrustumNear(0.5f);
				pc.setFrustumFar(80.0f);
				
				//pc.lookAt(slight.getPosition().copy(), 
					//	new Vector3(slight.getPosition()).add(camDir),
					//	new Vector3(0.0f, 1.0f, 0.0f));
				
				//System.out.println(pc.getDirection());
				
				pc.refreshProjection();
				
				state.setCamera(pc);
				state.depthProjection = pc.getProjection().cpy();
				state.depthView = pc.getView().cpy();
			} else {
				assert false : "Point lights not yet supported!";
			}
			
			gl.glViewport(0, 0, shadowMapW, shadowMapH);
			renderOccluders(gl, scene);
			
			// Restore old state
			gl.glViewport(0, 0, oldDim[2], oldDim[3]);
			//gl.glCullFace(GL2.GL_BACK);
			state.setCamera(aux);
			gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
		}
		
		state.forceMaterial(null);
		
		// Render to our framebuffer
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fbo_tex.getWriteFramebuffer());
		renderScene(gl, scene);
		renderDebug(Yeti.get().gl.getGL2(), scene);		
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);	// Unbind
		
		//Render to the screen
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
		int pindex = pps.getAttribLocation(Shader.A_POSITION);
		screenQuad.getVertices().use(pindex);

		int tindex = pps.getAttribLocation(Shader.A_TEXCOORD);
		screenQuad.getTexcoords().use(tindex);
		
		// This is where the magic happens!
		// The texture we rendered on is passed as an input to the second stage!
		pps.setU1i("colorMap", 0);
		gl.glActiveTexture(GLHelp.textureSlot[0]);
		gl.glBindTexture(texType, regTexHandle);
		
		gl.glDrawArrays(GL2.GL_QUADS, 0, screenQuad.getVertices().getSize());
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		
		fbo_tex.unuse(gl);
		gl.glBindTexture(texType, 0); 
		
		screenQuad.getVertices().cleanUp(pindex);
		screenQuad.getTexcoords().cleanUp(tindex);
		
		// Tiny debug renders
		if(scene.shadowsEnabled && canCast) {
			Shader dr = ResourceLoader.shader("depthRender");
			gl.glUseProgram(dr.handle);
			dr.setU1i("colorMap", 0);
			
			gl.glActiveTexture(GLHelp.textureSlot[0]);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, state.shadowTexture);
			
			int sqi = dr.getAttribLocation(Shader.A_POSITION);
			gl.glViewport(10, 10, 200, 200);
			screenQuad.getVertices().use(sqi);
			
			gl.glDisable(GL2.GL_DEPTH_TEST);
			gl.glDrawArrays(GL2.GL_QUADS, 0, screenQuad.getVertices().getSize());		
			gl.glEnable(GL2.GL_DEPTH_TEST);
			
			screenQuad.getVertices().cleanUp(sqi);
			gl.glViewport(0, 0, oldDim[2], oldDim[3]);
		}
	}
	
	public void dispose(GL3 gl) {
		fbo_tex.destroy(gl);
		fbo_shadows.destroy(gl);
		gl.glDeleteTextures(2, new int[] {
				regTexHandle,
				state.shadowTexture
		}, 0);
	}
	
	private void renderOccluders(GL3 gl, final Scene scene) {
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		for(ModelInstance modelInstance : scene.modelInstances) {
			if(! modelInstance.castsShadows()) continue;
			
			modelInstance.render(state, matrixstack);
			assert matrixstack.getSize() == 1;
		}
		
		// Render the billboards separately (always forward)
		for(Billboard b : scene.billbords) {
			b.render(state, matrixstack);
			assert matrixstack.getSize() == 1;
		}
	}
	
	private void renderScene(GL3 gl, final Scene scene) {
        /*          _.' :  `._                                            
                .-.'`.  ;   .'`.-.                                        
               / : ___\ ;  /___ ; \      __                               
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
		// one, forcing the depth and color bits that I drew the 3D geometry on
		// to never actually get cleared!
		// Nice one. 26.12.2012
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		// The transparent fog needs this, among other things
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);		
		
		for(ModelInstance modelInstance : scene.modelInstances) {
			modelInstance.render(state, matrixstack);
			assert matrixstack.getSize() == 1;
		}
		
		// Render the billboards separately (always forward)
		for(Billboard b : scene.billbords) {
			b.render(state, matrixstack);
			assert matrixstack.getSize() == 1;
		}
	}
	
	private void prepareBillboards(final Scene scene) {
		Collections.sort(scene.billbords, new Comparator<Billboard>() {
			@Override
			public int compare(Billboard o1, Billboard o2) {
				Vector3 cpos = scene.getCamera().getPosition();
				Float d1 = o1.getTransform().getTranslate().dist(cpos);
				Float d2 = o2.getTransform().getTranslate().dist(cpos);
				return d2.compareTo(d1);
			}
		});
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
					glut.glutSolidSphere(1.5d, 5, 5);
				}
				// need quaternion slerp to align a spotlight cone to the 
				// spotlight direction
			} 
		}
		gl.glPopMatrix();

	}
}
