package barsan.opengl.rendering;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GL3;

import barsan.opengl.Yeti;
import barsan.opengl.math.MathUtil;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.cameras.Camera;
import barsan.opengl.rendering.cameras.OrthographicCamera;
import barsan.opengl.rendering.cameras.PerspectiveCamera;
import barsan.opengl.rendering.lights.DirectionalLight;
import barsan.opengl.rendering.lights.Light;
import barsan.opengl.rendering.lights.Light.LightType;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.lights.SpotLight;
import barsan.opengl.rendering.materials.DepthWriterDirectional;
import barsan.opengl.rendering.materials.DepthWriterPoint;
import barsan.opengl.resources.ModelLoader;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.FPCameraAdapter;
import barsan.opengl.util.GLHelp;

import com.jogamp.opengl.FBObject;
import com.jogamp.opengl.FBObject.Attachment;
import com.jogamp.opengl.FBObject.Attachment.Type;
import com.jogamp.opengl.FBObject.RenderAttachment;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;

public class ForwardRenderer extends Renderer {

	private FBObject fbo_tex;
	private FBObject fbo_shadows;
	
	int texType = -1;
	int regTexHandle = -1;
	
	int shadowMapW = 2048;
	int shadowMapH = 2048;
	
	int cubeMapSide = 1024;
	
	// TODO: refactor this into self-contained helper
	private int	fbo_pointShadows;	// FBObject doesn't support cubemaps boo
	boolean MSAAEnabled = true;
	private int MSAASamples = 4;
	private StaticModel screenQuad;
	
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
	
	public ForwardRenderer(GL3 gl) {	
		super(gl);
		
		int fboWidth = Yeti.get().settings.width;
		int fboHeight = Yeti.get().settings.height;
		//*
		fbo_tex = new FBObject();
		fbo_tex.reset(gl, fboWidth, fboHeight);
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
		
		screenQuad = ModelLoader.buildQuad(2.0f, 2.0f, false);
		
		// Prepare shadow mapping
		fbo_shadows = new FBObject();
		fbo_shadows.reset(gl, shadowMapW, shadowMapH);
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
		 
		 gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, GL2.GL_TEXTURE_2D, state.shadowTexture, 0);	
		 gl.glDrawBuffer(GL2.GL_NONE);
		 fbo_shadows.unbind(gl);
		 
		 // Build the point light cubemap FBO
		 gl.glGenFramebuffers(1, name, 0);
		 fbo_pointShadows = name[0];
		 
		 state.cubeTexture = new Texture(GL.GL_TEXTURE_CUBE_MAP);
		 state.cubeTexture.bind(gl);
		 state.cubeTexture.setTexParameterf(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		 state.cubeTexture.setTexParameterf(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		 state.cubeTexture.setTexParameterf(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
		 state.cubeTexture.setTexParameterf(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
		 
		 for(int face = 0; face < 6; face++) {
			 /* How do you generate empty textures in JOGL? You need to feed
			  * them TextureData, which need a Buffer. Allocating an empty buffer
			  * for this seems silly, when I don't *want* any data in my texture.
			  * 
			  * Moreover, the way FBOS do texture binding is with texture 
			  * attachments, which are tightly coupled to FBOS - this is done
			  * because you can't actually use the Texture object with fbos
			  * unless you're willing to hack the system a little.
			  */
			 gl.glTexImage2D(CubeTexture.cubeSlots[face], 0, GL.GL_DEPTH_COMPONENT16,
					 cubeMapSide, cubeMapSide, 0, GL2.GL_DEPTH_COMPONENT, 
					 GL.GL_FLOAT, null);
			 
		 }
		 
		 gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fbo_pointShadows);
		 // We obviously don't want to use glFramebufferTexture2D over here
		 gl.glFramebufferTexture(GL2.GL_FRAMEBUFFER,
					GL2.GL_DEPTH_ATTACHMENT, 
					state.cubeTexture.getTextureObject(gl),
					0);
			
		 // Don't bind any texture here
		 gl.glDrawBuffer(GL2.GL_NONE); 
		 gl.glReadBuffer(GL2.GL_NONE);
		 gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
		 
		 GLHelp.fboErr(gl);		
	}
			
	@Override
	public void render(final Scene scene) {
		GL3 gl = state.gl;
		state.setAnisotropySamples(Yeti.get().settings.anisotropySamples);
		
		// Get the original viewport size; We cannot rely on Yeti's dimensions
		// since the GLJPanel is doing witchcraft which results in a viewport
		// with a greater height than it's supposed to
		int oldDim[] = new int[4];
		gl.glGetIntegerv(GL2.GL_VIEWPORT, oldDim, 0);
		Light light = state.getLights().get(0);
		
		if(sortBillboards) {
			sortBillboards(scene);
		}
		
		float delta = Yeti.get().getDelta();
		for(ModelInstance mi : scene.getModelInstances()) {
			if(mi instanceof AnimatedModelInstance) {
				((AnimatedModelInstance)mi).updateAnimation(delta);
			}
		}
		
		if(scene.shadowsEnabled) {
			//gl.glCullFace(GL2.GL_FRONT);
			Camera aux = state.getCamera();
			
			if(light.getType() == LightType.Directional) {
				
				// Directional light shadow casting
				
				state.forceMaterial(new DepthWriterDirectional());
				//state.forceAnimatedMaterial(new DepthWriterDirectionalAnim());
				gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fbo_shadows.getWriteFramebuffer());
				gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
				
				DirectionalLight dlight = (DirectionalLight)light;
				OrthographicCamera oc = new OrthographicCamera(
						(int) directionalShadowSize.x,
						(int) directionalShadowSize.y
						);
				oc.setFrustumNear(directionalShadowDepth.x);
				oc.setFrustumFar(directionalShadowDepth.y);
				
				Vector3 ld = dlight.getDirection();
				oc.lookAt(directionalShadowCenter.copy().add(ld),
						directionalShadowCenter,
						Vector3.UP);
				gl.glViewport(0, 0, shadowMapW, shadowMapH);
				renderShadowMap(gl, scene, oc);
				
			} else if(light.getType() == LightType.Spot) {
				
				// Spot light shadow casting
				
				state.forceMaterial(new DepthWriterDirectional());
				gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fbo_shadows.getWriteFramebuffer());
				gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
				
				SpotLight slight = (SpotLight)light;
				Vector3 camDir = slight.getDirection().copy();
				
				PerspectiveCamera pc = new PerspectiveCamera(
						slight.getPosition().copy(),
						camDir,
						shadowMapW, 
						shadowMapH);
				// Theta is the cos of the outer angle of the cone
				// Note that to get the FOV, we need to double that
				float th = slight.getCosOuter();
				float angle = (float) (2.0 * Math.acos(th) * MathUtil.RAD_TO_DEG);
				pc.setFOV(angle);
				pc.setFrustumNear(1f);
				pc.setFrustumFar(240.0f);
				
				gl.glViewport(0, 0, shadowMapW, shadowMapH);
				renderShadowMap(gl, scene, pc);
				
			} else {
				
				// Point light shadow casting

				PointLight pl = (PointLight)light;
				
				state.forceMaterial(new DepthWriterPoint(pl.getPosition(), omniShadowNear, omniShadowFar));
				gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fbo_pointShadows);
				
				// Render to a cubemap
				gl.glViewport(0, 0, cubeMapSide, cubeMapSide);
				gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
				
				// TODO: clean up this code
				renderOccluders(gl, scene);
			}
			
			// Restore old state
			//gl.glCullFace(GL2.GL_BACK);
			gl.glViewport(0, 0, oldDim[2], oldDim[3]);
			state.setCamera(aux);
			gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
		}
		/*
		 *  STATUS: slot computations aren't being done right, so a 2D sampler
		 *  is being bound to a samplerCube slot.
		 *  
		 *  UPDATE: the shadow cube works, unless it's bound to texture unit 1. wat.
		 *  
		 *  CURRENT STATE: everything works, but super-shaders are a no-no since
		 *  different sampler types try to sample from samplers they're not
		 *  supposed to;
		 */
		
		state.forceMaterial(null);
		
		// Render to our framebuffer
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fbo_tex.getWriteFramebuffer());
		renderScene(gl, scene);
		if(renderDebug) { 
			renderDebug(Yeti.get().gl.getGL2(), scene);		
		}
		
		//Render to the screen
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);	// Unbind
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
		// TODO: maybe used glBlitFramebuffer?
		if(scene.shadowsEnabled && renderDebug) {
			Shader dr;
			if(light.getType() == LightType.Point) {
				dr = ResourceLoader.shader("depthCubeRender");
			} else {
				dr = ResourceLoader.shader("depthRender");
			}
			gl.glUseProgram(dr.handle);
			dr.setU1i("colorMap", 0);
			
			float depthRenFactor = 1.0f;
			if(light.getType() != LightType.Directional) {
				depthRenFactor = 15.0f;
			}
			dr.setU1f("factor", depthRenFactor);
			
			gl.glActiveTexture(GLHelp.textureSlot[0]);
			if(light.getType() == LightType.Point) {
				state.cubeTexture.bind(gl);
			} else {
				gl.glBindTexture(GL2.GL_TEXTURE_2D, state.shadowTexture);
			}
			
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
	
	@Override
	public void dispose() {
		super.dispose();
		
		fbo_tex.destroy(gl);
		fbo_shadows.destroy(gl);
		gl.glDeleteTextures(2, new int[] {
				regTexHandle,
				state.shadowTexture
		}, 0);
		
		gl.glDeleteBuffers(1, new int[] {
			fbo_pointShadows
		}, 0);
	}
	
	private void renderOccluders(GL3 gl, final Scene scene) {
		gl.glDepthMask(true);
		// Quick tip: glClear(GL_DEPTH_BUFFER_BIT) does nothing if glDepthMask is not enabled!
		gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
		
		for(ModelInstance modelInstance : scene.modelInstances) {
			if(! modelInstance.castsShadows()) continue;			
			modelInstance.render(state, matrixstack);
			assert matrixstack.getSize() == 1 : "Matrix stack should be back to 1, instead was " + matrixstack.getSize();
		}
		
		// Billboards cannot cast shadows
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
			assert matrixstack.getSize() == 1 : "Matrix stack should be back to 1, instead was " + matrixstack.getSize();
		}
		
		// Render the billboards separately (always forward)
		for(Billboard b : scene.billboards) {
			b.render(state, matrixstack);
			assert matrixstack.getSize() == 1 : "Matrix stack should be back to 1, instead was " + matrixstack.getSize();
		}
	}
		
	private void renderShadowMap(GL3 gl, Scene scene, Camera camera) {
		state.setCamera(camera);
		state.depthProjection = camera.getProjection().cpy();
		state.depthView = camera.getView().cpy();
		renderOccluders(gl, scene);
	}
	
	private void renderDebug(GL2 gl, Scene scene) {
		FPCameraAdapter ca = new FPCameraAdapter(scene.camera);
		gl.glUseProgram(0);
		GLUT glut = new GLUT();
		ca.prepare(gl);
				
		gl.glPushMatrix();
		gl.glBegin(GL2.GL_LINES);
			float len = 250.0f;
			gl.glColor3f(1.0f, 0.0f, 0.0f);
			gl.glVertex3f(-len, 0.0f, 0.0f);
			gl.glVertex3f(len, 0.0f, 0.0f);
			
			gl.glColor3f(0.0f, 1.0f, 0.0f);
			gl.glVertex3f(0.0f, -len, 0.0f);
			gl.glVertex3f(0.0f,  len, 0.0f);
			
			gl.glColor3f(0.0f, 0.0f, 1.0f);
			gl.glVertex3f(0.0f, 0.0f, -len);
			gl.glVertex3f(0.0f, 0.0f, len);
		gl.glEnd();
		gl.glPopMatrix();
		
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		
		for(Light l : scene.lights) {
			if(l.getType() != LightType.Directional) {
				PointLight pl = (PointLight)l;
				gl.glPushMatrix();
				if(l.getType() == LightType.Point) {
					gl.glTranslatef(pl.getPosition().x, pl.getPosition().y, pl.getPosition().z);
					glut.glutSolidSphere(0.5d, 5, 5);
					
				} else if(l.getType() == LightType.Spot) {
					SpotLight sl = (SpotLight)pl;
					
					gl.glTranslatef(pl.getPosition().x, pl.getPosition().y, pl.getPosition().z);
					
					// TODO: utility from this witchcraft (glut has z+ as the up-axis of the cones derp)
					Vector3 d = sl.getDirection().copy();
					Vector3 up = new Vector3(0.0f, 0.0f, 1.0f);
					Vector3 axis = new Vector3(up).cross(d).normalize();
					
					float a = 180.0f + MathUtil.RAD_TO_DEG * (float)Math.acos(d.dot(up));
					gl.glRotatef(a, axis.x, axis.y, axis.z);
					
					glut.glutWireCone(0.33f, 1.0f, 15, 3);
				}
				gl.glPopMatrix();
			} 
		}
		gl.glPopMatrix();

	}

}
