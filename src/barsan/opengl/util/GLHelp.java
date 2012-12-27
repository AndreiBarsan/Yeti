package barsan.opengl.util;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

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
		int result = gl.glCheckFramebufferStatus(GL2.GL_DRAW_FRAMEBUFFER);
		switch (result) {
		case GL2.GL_FRAMEBUFFER_UNDEFINED:
			System.out.println("FBO Undefined\n");
			break;
		case GL2.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
			System.out.println("FBO Incomplete Attachment\n");
			break;
		case GL2.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
			System.out.println("FBO Missing Attachment\n");
			break;
		case GL2.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
			System.out.println("FBO Incomplete Draw Buffer\n");
			break;
		case GL2.GL_FRAMEBUFFER_UNSUPPORTED:
			System.out.println("FBO Unsupported\n");
			break;
		case GL2.GL_FRAMEBUFFER_COMPLETE:
			System.out.println("FBO OK\n");
			break;
		default:
			System.out.println("FBO Problem?\n");
		}
	}

}
