package barsan.opengl.util;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.jogamp.opengl.FBObject;

import barsan.opengl.Yeti;

public class GLHelp {
	
	private static float[] out = new float[] { -1 };
	
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
			System.out.println("No error!");
		} else {
			System.err.println("Error found: #" + code);
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
		System.out.println(FBObject.getStatusString(result));
	}
}
