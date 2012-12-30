package barsan.opengl.util;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.jogamp.opengl.FBObject;

import barsan.opengl.Yeti;

public class GLHelp {
	public static void checkError(GL gl) {
		int code = gl.glGetError();
		if (code == GL.GL_NO_ERROR) {
			System.out.println("No error!");
		} else {
			System.err.println("Error found: #" + code);
		}
	}

	public static void assertOK(GL2 gl) {
		int code = gl.glGetError();
		if (code != GL.GL_NO_ERROR) {
			Yeti.screwed("GL fatal error: #" + code);
		}
	}

	public static void fboErr(GL2 gl) {
		int result = gl.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER);
		System.out.println(FBObject.getStatusString(result));
	}
}
