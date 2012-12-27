package barsan.opengl.rendering;

import java.util.Collections;
import java.util.Comparator;

import javax.media.opengl.GL2;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Model.Face;
import barsan.opengl.resources.ResourceLoader;

import com.jogamp.opengl.FBObject;
import com.jogamp.opengl.FBObject.Attachment;
import com.jogamp.opengl.FBObject.ColorAttachment;
import com.jogamp.opengl.FBObject.TextureAttachment;

public class Renderer {

	private RendererState state;
	private FBObject fbo_tex, fbo_ren;
	private Matrix4Stack matrixstack = new Matrix4Stack();
	
	ColorAttachment ca;
	
	public Renderer(GL2 gl) {	
		state = new RendererState(gl);
		
		int fboWidth = Yeti.get().settings.width;
		int fboHeight = Yeti.get().settings.height;
		
		fbo_tex = new FBObject();
		fbo_tex.reset(gl, fboWidth, fboHeight);
		fbo_tex.attachTexture2D(gl, 0, true);
        fbo_tex.attachRenderbuffer(gl, Attachment.Type.DEPTH, 32);
		fbo_tex.unbind(gl);
		
		fbo_ren = new FBObject();
		fbo_ren.reset(gl, fboWidth, fboHeight);
		fbo_ren.attachTexture2D(gl, 0, true);
		fbo_ren.attachRenderbuffer(gl, Attachment.Type.DEPTH, 32);
		fbo_ren.unbind(gl);
		
		
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
		GL2 gl = state.getGl();
		gl.glDepthMask(true);
		
		// Set the current target fbo
		fbo_tex.bind(gl);
		
		/*	                _.' :  `._                                            
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
				Float d1 = o1.getTransform().getPosition().dist(cpos);
				Float d2 = o2.getTransform().getPosition().dist(cpos);
				return d2.compareTo(d1);
			}
		});
		
		// Render the billboards separately
		for(Billboard b : scene.billbords) {
			b.render(state, matrixstack);
		}
		
		gl.glFinish();
		fbo_tex.unbind(gl);
		
		// Clear the main FrameBuffer
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		// Begin post-processing
		Shader pps = ResourceLoader.shader("postProcess");
		gl.glUseProgram(pps.handle);
		
		int pindex = pps.getAttribLocation(Shader.A_POSITION);
		quad.getVertices().use(pindex);

		int tindex = pps.getAttribLocation(Shader.A_TEXCOORD);
		quad.getTexcoords().use(tindex);
		
		pps.setU1i("colorMap", 0);
		float blurAmount = 2.0f;
		pps.setU1f("blurV", 1.0f / Yeti.get().settings.height * blurAmount);
		pps.setU1f("blurH", 1.0f / Yeti.get().settings.width * blurAmount);
		fbo_tex.use(gl, (TextureAttachment)fbo_tex.getColorbuffer(0));
		
		gl.glDrawArrays(GL2.GL_QUADS, 0, quad.getVertices().getSize());
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		fbo_tex.unuse(gl);
		
		quad.getVertices().cleanUp(pindex);
		quad.getTexcoords().cleanUp(tindex);
		
	}
	
	public void dispose(GL2 gl) {
		fbo_tex.destroy(gl);
	}
}
