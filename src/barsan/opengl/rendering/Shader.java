/**
 *  YETI Engine Copyright (c) 2012-2013, Andrei Bârsan All rights reserved.
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *    - Redistributions of source code must retain the above copyright notice, 
 *    	this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright notice, 
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package barsan.opengl.rendering;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import javax.media.opengl.GL2;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix3;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Vector3;

/**
 * Shader wrapper class that facilitates material interactions with the underlying
 * GPU program.
 * 
 * @note It caches all the uniform locations, indexed by their name, in a 
 * HashMap to limit the number of native calls being performed (VisualVM showed
 * this as one of the top 10 time consumers).
 * 
 * @author Andrei Bârsan
 */
public class Shader {
	public static final String A_POSITION = "vVertex";
	public static final String A_NORMAL = "vNormal";
	public static final String A_TEXCOORD = "vTexCoord";
	
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
	
	/* pp */ int handle = -1;
	/* pp */ String name = "";
	private HashMap<String, Integer> uLocCache = new HashMap<>(); 
	
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
			shaderError("Vertex shader [" + name + "] failed to compile: ", vertex);
		}
		
		// The null int[] is required -> otherwise the source code somehow
		// gets corrupted and causes strange errors.
		gl.glShaderSource(fragment, 1, new String[] { fragmentSrc }, (int[])null, 0);
		gl.glCompileShader(fragment);
		
		gl.glGetShaderiv(fragment, GL2.GL_COMPILE_STATUS, result, 0);
		if(result[0] == GL2.GL_FALSE) {
			shaderError("Fragment shader [" + name + "] failed to compile: ", fragment);
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
		this.name = name;
		this.handle = shaderProgram;
	}

	void dispose() {
		GL2 gl = Yeti.get().gl; 
		gl.glDeleteShader(handle);
	}
	
	private void shaderError(String message, int handle) {
		GL2 gl = Yeti.get().gl; 
		gl.glGetShaderInfoLog(handle, 512, i_buff, b_buff);
		Yeti.screwed(message + "\n\t" + new String(b_buff.array(), 0, 512));
	}
	
	private void shaderLinkError(String message, int handle) {
		Yeti.get().gl.glGetProgramInfoLog(handle, 128, i_buff, b_buff);
		Yeti.screwed("\n\t" + new String(b_buff.array(), 0, 256));
	}
	
	public int getAttribLocation(String name) {
		GL2 gl = Yeti.get().gl; 
		return gl.glGetAttribLocation(handle, name);
	}
	
	public boolean setUMatrix4(String uniformName, Matrix4 matrix) {
		GL2 gl = Yeti.get().gl; 
		int pos;
		if(uLocCache.containsKey(uniformName)) {
			pos = uLocCache.get(uniformName); 
		} else {
			pos = gl.glGetUniformLocation(handle, uniformName);
			if(pos == -1) Yeti.debug("Uniform not found: %s (in shader %s)", uniformName, name);
			if(pos != -1) {
				uLocCache.put(uniformName, pos);
			}
		}
		if(pos == -1) return false;
		
		gl.glUniformMatrix4fv(pos, 1, false, matrix.getData(), 0);
		return true;
	}
	
	public boolean setUVector4f(String uniformName, float[] value) {
		GL2 gl = Yeti.get().gl; 
		int pos;
		if(uLocCache.containsKey(uniformName)) {
			pos = uLocCache.get(uniformName); 
		} else {
			pos = gl.glGetUniformLocation(handle, uniformName);
			if(pos == -1) Yeti.debug("Uniform not found: %s (in shader %s)", uniformName, name);
			if(pos != -1) {
				uLocCache.put(uniformName, pos);
			}
		}
		if(pos == -1) return false;
		
		gl.glUniform4fv(pos, 1, value, 0);
		return true;
	}

	public boolean setUMatrix3(String uniformName, Matrix3 matrix) {
		GL2 gl = Yeti.get().gl; 
		int pos;
		if(uLocCache.containsKey(uniformName)) {
			pos = uLocCache.get(uniformName); 
		} else {
			pos = gl.glGetUniformLocation(handle, uniformName);
			if(pos == -1) Yeti.debug("Uniform not found: %s (in shader %s)", uniformName, name);
			if(pos != -1) {
				uLocCache.put(uniformName, pos);
			}
		}
		if(pos == -1) return false;
		
		gl.glUniformMatrix3fv(pos, 1, false, matrix.getData(), 0);
		return true;
		
	}

	public boolean setUVector3f(String uniformName, Vector3 value) {
		GL2 gl = Yeti.get().gl; 
		int pos;
		if(uLocCache.containsKey(uniformName)) {
			pos = uLocCache.get(uniformName); 
		} else {
			pos = gl.glGetUniformLocation(handle, uniformName);
			if(pos == -1) Yeti.debug("Uniform not found: %s (in shader %s)", uniformName, name);
			if(pos != -1) {
				uLocCache.put(uniformName, pos);
			}
		}
		if(pos == -1) return false;
		
		gl.glUniform3f(pos, value.x, value.y, value.z);
		return true;
		
	}
	
	public boolean setU1i(String uniformName, boolean value) {
		return setU1i(uniformName, (value) ? 1 : 0);
	}
	
	public boolean setU1i(String uniformName, int value) {
		GL2 gl = Yeti.get().gl; 
		int pos;
		if(uLocCache.containsKey(uniformName)) {
			pos = uLocCache.get(uniformName); 
		} else {
			pos = gl.glGetUniformLocation(handle, uniformName);
			if(pos == -1) Yeti.debug("Uniform not found: %s (in shader %s)", uniformName, name);
			if(pos != -1) {
				uLocCache.put(uniformName, pos);
			}
		}
		if(pos == -1) return false;

		gl.glUniform1i(pos, value);
		return true;
	}

	public boolean setU1f(String uniformName, float value) {
		GL2 gl = Yeti.get().gl;
		int pos;
		if(uLocCache.containsKey(uniformName)) {
			pos = uLocCache.get(uniformName); 
		} else {
			pos = gl.glGetUniformLocation(handle, uniformName);
			if(pos == -1) Yeti.debug("Uniform not found: %s (in shader %s)", uniformName, name);
			if(pos != -1) {
				uLocCache.put(uniformName, pos);
			}
		}
		if(pos == -1) return false;

		gl.glUniform1f(pos, value);
		return true;
	}
}
