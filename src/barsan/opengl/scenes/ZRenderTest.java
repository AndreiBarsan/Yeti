package barsan.opengl.scenes;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.Shader;
import barsan.opengl.rendering.StaticModel;
import barsan.opengl.rendering.StaticModelInstance;
import barsan.opengl.rendering.lights.AmbientLight;
import barsan.opengl.rendering.lights.DirectionalLight;
import barsan.opengl.rendering.materials.BasicMaterial;
import barsan.opengl.rendering.materials.DepthWriterDirectional;
import barsan.opengl.resources.ModelLoader.Face;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;
import barsan.opengl.util.GLHelp;

import com.jogamp.opengl.FBObject;

public class ZRenderTest extends Scene {

	StaticModelInstance mi;
	private StaticModel screenQuad;
	int regTexHandle = -1;
	int texType = -1;
	private FBObject fbo_tex;
	private FBObject fbo_shadows;
	
	RendererState rs;
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		
		ResourceLoader.loadObj("monkey", "monkey.obj");
		
		rs = new RendererState(renderer, Yeti.get().gl);
		
		lights.add(new DirectionalLight(new Vector3(-1.0f, 1.0f, 0.0f).normalize()));
		GL3 gl = Yeti.get().gl;
		
		modelInstances.add(mi = new StaticModelInstance(ResourceLoader.model("monkey")));
		mi.setMaterial(new BasicMaterial(Color.RED));
		
		int fboWidth = Yeti.get().settings.width;
		int fboHeight = Yeti.get().settings.height;
		
		fbo_tex = new FBObject();
		fbo_tex.reset(gl, fboWidth, fboHeight, 0);
		fbo_tex.bind(gl);
		
		fbo_tex.attachTexture2D(gl, 0, true);
		
		texType = GL2.GL_TEXTURE_2D;
		
		final int[] name = new int[] { -1 };
		
		screenQuad = new StaticModel(gl, "derp");
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
		int shadowMapW = 1024;
		int shadowMapH = 1024;
		fbo_shadows.reset(gl, shadowMapW, shadowMapH, 0);
		fbo_shadows.bind(gl);
		
		gl.glGenTextures(1, name, 0);
		rs.shadowTexture = name[0];
		gl.glBindTexture(GL2.GL_TEXTURE_2D, rs.shadowTexture);
		gl.glTexImage2D(GL2.GL_TEXTURE_2D,
				0,
				GL2.GL_DEPTH_COMPONENT16, 
				shadowMapW, shadowMapH,
				0,
				GL2.GL_DEPTH_COMPONENT,
				GL2.GL_FLOAT, 			// both this and unsigned byte work!
				null);
		 gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		 gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		 gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
		 gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
		 //gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_COMPARE_FUNC, GL2.GL_LEQUAL);
		 //gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_COMPARE_MODE, GL2.GL_COMPARE_R_TO_TEXTURE);
		 
		 gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, GL2.GL_TEXTURE_2D, rs.shadowTexture, 0);	
		 
		 gl.glDrawBuffer(GL2.GL_NONE);
		 GLHelp.fboErr(gl);
		 
		 fbo_shadows.unbind(gl);
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		
		if(exiting) { exit(); }
		
		GL2 gl = Yeti.get().gl;
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		rs.setCamera(camera);
		rs.setAmbientLight(new AmbientLight(Color.TRANSPARENTBLACK));
		rs.setLights(lights);
		
		camera.setPosition(new Vector3(-6.0f, 1.2f, 0.0f));
		camera.setDirection(new Vector3(-1.0f, 0.0f, 0.0f));
		
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fbo_shadows.getWriteFramebuffer());
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		rs.forceMaterial(new DepthWriterDirectional());
		mi.render(rs, new Matrix4Stack());
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
		
		Shader pps = ResourceLoader.shader("depthRender");
		gl.glUseProgram(pps.getHandle());
		
		pps.setU1i("colorMap", 0);
		
		int pindex = pps.getAttribLocation(Shader.A_POSITION);
		screenQuad.getVertices().use(pindex);

		gl.glActiveTexture(GLHelp.textureSlot[0]);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, rs.shadowTexture);
		gl.glDrawArrays(GL2.GL_QUADS, 0, screenQuad.getVertices().getSize());
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);

		screenQuad.getVertices().cleanUp(pindex);
	}
}
