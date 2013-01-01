package barsan.opengl.rendering;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import javax.media.opengl.GL2;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix3;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Vector3;

public class Shader {
		
	int handle = -1;
	
	static final String A_POSITION = "vVertex";
	static final String A_NORMAL = "vNormal";
	static final String A_TEXCOORD = "vTexCoord";
	
	// Utility buffers
	static final IntBuffer i_buff = IntBuffer.allocate(8);
	static final ByteBuffer b_buff = ByteBuffer.allocate(1024);
	static final int[] result = new int[1];
	
	
	public static HashMap<String, Shader> compileBulk(GL2 gl, String[] vertexData, String[] fragmentData) {
		assert vertexData.length == fragmentData.length;
		
		HashMap<String, Shader> result = new HashMap<>(vertexData.length);
		
		// TODO: implement
		
		return result;
	}
	
	public Shader(GL2 gl, String name, String vertexSrc, String fragmentSrc) {
		this(gl, name, vertexSrc, fragmentSrc, new String[] {
			A_POSITION,
			A_NORMAL
		});
	}
	
	public int getHandle() {
		return handle;
	}
	
	public Shader(GL2 gl, String name, String vertexSrc, String fragmentSrc, String[] args) {
		
		int vertex = gl.glCreateShader(GL2.GL_VERTEX_SHADER);
		int fragment = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);
		
		gl.glShaderSource(vertex, 1, new String[] { vertexSrc }, (int[])null, 0);
		gl.glCompileShader(vertex);
		
		gl.glGetShaderiv(vertex, GL2.GL_COMPILE_STATUS, result, 0);
		if(result[0] == GL2.GL_FALSE) {
			shaderError("Vertex shader failed to compile: ", vertex);
		}
		
		// The null int[] is required -> otherwise the source code somehow
		// gets corrupted and causes strange errors.
		gl.glShaderSource(fragment, 1, new String[] { fragmentSrc }, (int[])null, 0);
		gl.glCompileShader(fragment);
		
		gl.glGetShaderiv(fragment, GL2.GL_COMPILE_STATUS, result, 0);
		if(result[0] == GL2.GL_FALSE) {
			shaderError("Fragment shader failed to compile: ", fragment);
		}
		
		int shaderProgram = gl.glCreateProgram();
		gl.glAttachShader(shaderProgram, vertex);
		gl.glAttachShader(shaderProgram, fragment);
		
		// Bind the attributes
		/*
		 * It's not mandatory, but it's good practice. If you don't do it, OpenGL
		 * will assign them automatically, and you'll be forced to use getAttribLocation
		 * if you ever need one. This solution is more elegant.
		 */
		// NOTE: must be done BEFORE linking
		/*
		for(int i = 0; i < args.length; i++) {
			gl.glBindAttribLocation(shaderProgram, i, args[i]);
		}*/
		
		gl.glLinkProgram(shaderProgram);
		gl.glGetProgramiv(shaderProgram, GL2.GL_LINK_STATUS, result, 0);
		if(result[0] == GL2.GL_FALSE) {
			shaderLinkError(String.format("Shader \"%s\" failed to link", name), shaderProgram);
		}
		
		gl.glValidateProgram(shaderProgram);
		gl.glDeleteShader(vertex);
		gl.glDeleteShader(fragment);
		
		Yeti.debug(String.format("Shader \"%s\" loaded OK", name));
		handle = shaderProgram;
	}

	void dispose() {
		GL2 gl = Yeti.get().gl; 
		gl.glDeleteShader(handle);
	}
	
	private void shaderError(String message, int handle) {
		GL2 gl = Yeti.get().gl; 
		gl.glGetShaderInfoLog(handle, 128, i_buff, b_buff);
		Yeti.screwed(message + "\n\t" + new String(b_buff.array(), 0, 128));
	}
	
	private void shaderLinkError(String message, int handle) {
		Yeti.get().gl.glGetProgramInfoLog(handle, 128, i_buff, b_buff);
		Yeti.screwed("\n\t" + new String(b_buff.array(), 0, 256));
	}
	
	public boolean setUMatrix4(String uniformName, Matrix4 matrix) {
		GL2 gl = Yeti.get().gl; 
		int pos = gl.glGetUniformLocation(handle, uniformName);
		if(pos == -1) return false;
		gl.glUniformMatrix4fv(pos, 1, false, matrix.getData(), 0);
		return true;
	}
	
	public boolean setUVector4f(String uniformName, float[] value) {
		GL2 gl = Yeti.get().gl; 
		int pos = gl.glGetUniformLocation(handle, uniformName);
		if(pos == -1) return false;
		
		gl.glUniform4fv(pos, 1, value, 0);
		return true;
	}

	public boolean setUMatrix3(String uniformName, Matrix3 matrix) {
		GL2 gl = Yeti.get().gl; 
		int pos = gl.glGetUniformLocation(handle, uniformName);
		if(pos == -1) return false;
		
		gl.glUniformMatrix3fv(pos, 1, false, matrix.getData(), 0);
		return true;
		
	}

	public boolean setUVector3f(String uniformName, Vector3 value) {
		GL2 gl = Yeti.get().gl; 
		int pos = gl.glGetUniformLocation(handle, uniformName);
		if(pos == -1) return false;
		
		gl.glUniform3f(pos, value.x, value.y, value.z);
		return true;
		
	}
	
	public boolean setU1i(String uniformName, boolean value) {
		return setU1i(uniformName, (value) ? 1 : 0);
	}
	
	public boolean setU1i(String uniformName, int value) {
		GL2 gl = Yeti.get().gl; 
		int pos = gl.glGetUniformLocation(handle, uniformName);
		if(pos == -1) return false;

		gl.glUniform1i(pos, value);
		return true;
	}

	public int getAttribLocation(String name) {
		GL2 gl = Yeti.get().gl; 
		return gl.glGetAttribLocation(handle, name);
	}

	public boolean setU1f(String uniformName, float value) {
		GL2 gl = Yeti.get().gl; 
		int pos = gl.glGetUniformLocation(handle, uniformName);
		if(pos == -1) return false;

		gl.glUniform1f(pos, value);
		return true;
	}
}
