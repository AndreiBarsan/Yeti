package barsan.opengl.util;

import barsan.opengl.resources.Face;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL3;

import barsan.opengl.Yeti;
import barsan.opengl.rendering.Shader;
import barsan.opengl.rendering.StaticModel;
import barsan.opengl.resources.ModelLoader;
import barsan.opengl.resources.ResourceLoader;

import com.jogamp.opengl.FBObject;

public class GLHelp {
	
	private static float[] out = new float[] { -1 };
	
	private static StaticModel screenQuad = ModelLoader.buildQuadXY(2.0f, 2.0f);
	
	/** Lookup table for converting between integer-defined texture slots
	 * 	and GL flags.
	 */
	public static final int[] textureSlot = new int[] {
		GL.GL_TEXTURE0, GL.GL_TEXTURE1, GL.GL_TEXTURE2,
		GL.GL_TEXTURE3,	GL.GL_TEXTURE4,	GL.GL_TEXTURE5,
		GL.GL_TEXTURE6,	GL.GL_TEXTURE7,	GL.GL_TEXTURE8
	};

	public static void checkError(GL gl) {
		int code = gl.glGetError();
		if (code == GL.GL_NO_ERROR) {
			Yeti.debug("No error!");
		} else {
			Yeti.screwed("GL error found: #0x" + Integer.toHexString(code));
		}
	}

	public static void assertOK(GL gl) {
		int code = gl.glGetError();
		if (code != GL.GL_NO_ERROR) {
			Yeti.screwed("GL fatal error: #" + code);
		}
	}
	
	public static float get1f(GL gl, int name) {
		gl.glGetFloatv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, out, 0);
		return out[0];
	}

	public static void fboErr(GL gl) {
		int result = gl.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER);
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		/*
		 *  Explanation of the 2:
		 *   - 0 = the inside of the getStackTrace method
		 *   - 1 = this method
		 *   - 2 = this method's caller 
		 */
		String caller = stackTraceElements[2].getClassName();
		Yeti.debug("FBO status check (" + caller + "): " + FBObject.getStatusString(result));
	}
	
	public static void dumpDepthBuffer(int x, int y, int w, int h, float depthRenFactor, int handle) {
		int oldDim[] = new int[4];
		GL3 gl = Yeti.get().gl;
		gl.glGetIntegerv(GL3.GL_VIEWPORT, oldDim, 0);
		
		Shader dr = ResourceLoader.shader("depthRender");
		gl.glUseProgram(dr.getHandle());
		dr.setU1i("colorMap", 0);
		
		dr.setU1f("factor", depthRenFactor);
		
		gl.glActiveTexture(GLHelp.textureSlot[0]);
		gl.glBindTexture(GL3.GL_TEXTURE_2D, handle);
		
		int sqi = dr.getAttribLocation(Shader.A_POSITION);
		gl.glViewport(x, y, w, h);
		screenQuad.getVertices().use(sqi);
		
		gl.glDisable(GL3.GL_DEPTH_TEST);
		gl.glDrawArrays(GL3.GL_QUADS, 0, screenQuad.getVertices().getSize());
		gl.glEnable(GL3.GL_DEPTH_TEST);
		
		screenQuad.getVertices().cleanUp(sqi);
		gl.glViewport(0, 0, oldDim[2], oldDim[3]);
	}
	
	public static void dumpDepthCubeBuffer(int x, int y, int w, int h, float depthRenFactor, int handle) {
		int oldDim[] = new int[4];
		GL3 gl = Yeti.get().gl;
		gl.glGetIntegerv(GL3.GL_VIEWPORT, oldDim, 0);
		
		Shader dr = ResourceLoader.shader("depthCubeRender");
		gl.glUseProgram(dr.getHandle());
		dr.setU1i("colorMap", 0);
		dr.setU1f("factor", depthRenFactor);
	
		gl.glActiveTexture(GLHelp.textureSlot[0]);
		gl.glBindTexture(GL3.GL_TEXTURE_CUBE_MAP, handle);
		
		int sqi = dr.getAttribLocation(Shader.A_POSITION);
		gl.glViewport(x, y, w, h);
		screenQuad.getVertices().use(sqi);
		
		gl.glDisable(GL3.GL_DEPTH_TEST);
		gl.glDrawArrays(GL3.GL_QUADS, 0, screenQuad.getVertices().getSize());
		gl.glEnable(GL3.GL_DEPTH_TEST);
		
		screenQuad.getVertices().cleanUp(sqi);
		gl.glViewport(0, 0, oldDim[2], oldDim[3]);
	
		// Note: blog post with openGL snipped;
		// every line has a comment describing what happens when a particular
		// line is commented out, possibly also with a screenshot
		
		/* Remove this and the engine crashes with a dreaded 0x502 error */
		gl.glBindTexture(GL3.GL_TEXTURE_CUBE_MAP, 0);
	}
}
