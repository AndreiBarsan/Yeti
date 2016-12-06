/**
 *  YETI Engine Copyright (c) 2012-2013, Andrei Barsan All rights reserved.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL3;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix3;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Vector2;
import barsan.opengl.math.Vector3;
import barsan.opengl.util.Color;
import com.jogamp.opengl.GL3;

/**
 * Shader wrapper class that facilitates material interactions with the underlying
 * GPU program.
 * 
 * @note It caches all the uniform locations, indexed by their name, in a 
 * HashMap to limit the number of native calls being performed (VisualVM showed
 * this as one of the top 10 time consumers).
 * 
 * @author Andrei B�rsan
 */
public class Shader {
	public static final String A_POSITION = "vVertex";
	public static final String A_NORMAL = "vNormal";
	public static final String A_TEXCOORD = "vTexCoord";
	
	private static final List<String> groks = Arrays.asList("No errors.");
	
	
	// Utility buffers
	static final IntBuffer i_buff = IntBuffer.allocate(8);
	static final ByteBuffer b_buff = ByteBuffer.allocate(1024);
	static final int[] result = new int[1];
	
	public static HashMap<String, Shader> compileBulk(GL3 gl, String[] vertexData, String[]
		fragmentData) {
		assert vertexData.length == fragmentData.length;
		
		HashMap<String, Shader> result = new HashMap<>(vertexData.length);
		
		// TODO: implement
		
		return result;
	}
	
	/* pp */ int handle = -1;
	/* pp */ String name = "";
	private HashMap<String, Integer> uLocCache = new HashMap<>(); 
	
	public Shader(GL3 gl, String name, String vertexSrc, String fragmentSrc) {
		this(gl, name, vertexSrc, fragmentSrc, null, new String[] {
			A_POSITION,
			A_NORMAL
		});
	}
	
	public Shader(GL3 gl, String name, String vertexSrc, String fragmentSrc, String geometrySrc) {
		this(gl, name, vertexSrc, fragmentSrc, geometrySrc, new String[] {
			A_POSITION,
			A_NORMAL
		});
	}
	
	public int getHandle() {
		return handle;
	}
	
	public Shader(GL3 gl, String name, String vertexSrc, String fragmentSrc, String geometrySrc, String[] args) {
		
		int vertex = gl.glCreateShader(GL3.GL_VERTEX_SHADER);
		int fragment = gl.glCreateShader(GL3.GL_FRAGMENT_SHADER);
		int geometry = 0;
		boolean hasGeometry = (geometrySrc != null);
		
		gl.glShaderSource(vertex, 1, new String[] { vertexSrc }, (int[])null, 0);
		gl.glCompileShader(vertex);
		checkShader("Vertex shader [" + name + "]", vertex);
		
		if(hasGeometry) {
			geometry = gl.glCreateShader(GL3.GL_GEOMETRY_SHADER);
			gl.glShaderSource(geometry, 1, new String[] { geometrySrc }, (int[])null, 0);
			gl.glCompileShader(geometry);
			checkShader("Geometry shader [" + name + "]", geometry);		
		}
		
		// The null int[] is required -> otherwise the source code somehow
		// gets corrupted and causes strange errors.
		gl.glShaderSource(fragment, 1, new String[] { fragmentSrc }, (int[])null, 0);
		gl.glCompileShader(fragment);
		checkShader("Fragment shader [" + name + "]", fragment);
		
		int shaderProgram = gl.glCreateProgram();
		gl.glAttachShader(shaderProgram, vertex);
		gl.glAttachShader(shaderProgram, fragment);
		if(hasGeometry) {
			gl.glAttachShader(shaderProgram, geometry);
		}
		
		// Maybe pre-bind attributes here?
		
		gl.glLinkProgram(shaderProgram);
		checkProgram("Shader [" + name + "]", shaderProgram);
		
		gl.glValidateProgram(shaderProgram);
		gl.glDeleteShader(vertex);
		gl.glDeleteShader(fragment);
		if(hasGeometry) {
			gl.glDeleteShader(geometry);
		}
		
		this.name = name;
		this.handle = shaderProgram;
	}

	public int getAttribLocation(String name) {
		GL3 gl = Yeti.get().gl;
		return gl.glGetAttribLocation(handle, name);
	}
	
	public boolean setUMatrix4(String uniformName, Matrix4 matrix) {
		GL3 gl = Yeti.get().gl;
		int pos = grabUniform(uniformName);
		if(pos == -1) return false;
		
		gl.glUniformMatrix4fv(pos, 1, false, matrix.getData(), 0);
		return true;
	}
	
	public boolean setUMatrix4a(String uniformName, Matrix4[] matrix) {
		GL3 gl = Yeti.get().gl;
		int pos = grabUniform(uniformName);
		if(pos == -1) return false;
		
		float[] buffer = new float[matrix.length * 16];
		for(int i = 0; i < matrix.length; i++) {
			System.arraycopy(matrix[i].getData(), 0, buffer, 16 * i, 16);
		}
		
		gl.glUniformMatrix4fv(pos, matrix.length, false, buffer, 0);
		
		return true;
	}
	
	public boolean setUVector2f(String uniformName, Vector2 value) {
		return setUVector2f(uniformName, value.x, value.y);
	}
	
	public boolean setUVector2f(String uniformName, float x, float y) {
		GL3 gl = Yeti.get().gl;
		int pos = grabUniform(uniformName);
		if(pos == -1) return false;
		gl.glUniform2f(pos, x, y);
		return true;
	}
	
	public boolean setUVector3f(String uniformName, Vector3 value) {
		GL3 gl = Yeti.get().gl;
		int pos = grabUniform(uniformName);
		if(pos == -1) return false;
		
		gl.glUniform3f(pos, value.x, value.y, value.z);
		return true;
	}
	
	public boolean setUVector3f(String uniformName, Color value) {
		GL3 gl = Yeti.get().gl;
		int pos = grabUniform(uniformName);
		if(pos == -1) return false;
		
		gl.glUniform3f(pos, value.r, value.g, value.b);
		return true;
	}
	
	public boolean setUVector4f(String uniformName, float[] value) {
		GL3 gl = Yeti.get().gl;
		int pos = grabUniform(uniformName);
		if(pos == -1) return false;
		
		gl.glUniform4fv(pos, 1, value, 0);
		return true;
	}

	public boolean setUMatrix3(String uniformName, Matrix3 matrix) {
		GL3 gl = Yeti.get().gl;
		int pos = grabUniform(uniformName);
		if(pos == -1) return false;
		
		gl.glUniformMatrix3fv(pos, 1, false, matrix.getData(), 0);
		return true;
		
	}
	
	public boolean setU1i(String uniformName, boolean value) {
		return setU1i(uniformName, (value) ? 1 : 0);
	}
	
	public boolean setU1i(String uniformName, int value) {
		GL3 gl = Yeti.get().gl;
		int pos = grabUniform(uniformName);
		gl.glUniform1i(pos, value);
		return true;
	}

	public boolean setU1f(String uniformName, float value) {
		GL3 gl = Yeti.get().gl;
		int pos = grabUniform(uniformName);
		if(pos == -1) return false;

		gl.glUniform1f(pos, value);
		return true;
	}
	
	/**
	 * Called to release the native resource.
	 */
	public void dispose() {
		GL3 gl = Yeti.get().gl;
		gl.glDeleteShader(handle);
	}
	

	/**
	 * Helper method to get a handle to a uniform. First checks whether that value
	 * was already looked up, in which case it grabs it from the cache instead of
	 * performing a native call, avoiding the associated overhead.
	 */
	private int grabUniform(String uniformName) {
		GL3 gl = Yeti.get().gl;
		
		int pos;
		if(uLocCache.containsKey(uniformName)) {
			pos = uLocCache.get(uniformName); 
		} else {
			pos = gl.glGetUniformLocation(handle, uniformName);
			uLocCache.put(uniformName, pos);
			
			if(pos == -1) {
				Yeti.debug("Uniform not found: %s (in shader %s)", uniformName, name);
			}
		}
		
		return pos;
	}
	
	private void checkShader(String message, int handle) {
		check(message, handle, false);
	}
	
	private void checkProgram(String message, int handle) {
		check(message, handle, true);
	}
	
	/**
	 * Checks whether the entity (shader or shader program, specified by the
	 * isProgram parameter) compiled successfully. If an error occurred, crashes
	 * the engine to prompt the programmer to fix the error as soon as possible.
	 * 
	 * If warnings are detected, they are printed out.
	 */
	private void check(String message, int handle, boolean isProgram) {		
		GL3 gl = Yeti.get().gl;
		IntBuffer buff = IntBuffer.allocate(1);
		String logContents = "Empty log";
		boolean logEmpty = true;
		
		if(isProgram) {
			gl.glGetProgramiv(handle, GL3.GL_INFO_LOG_LENGTH, buff);
		} else {
			gl.glGetShaderiv(handle, GL3.GL_INFO_LOG_LENGTH, buff);
		}
		
		int l = buff.get();
		if(l > 1) {
			logEmpty = false;
			if(isProgram) {
				gl.glGetProgramInfoLog(handle, l, i_buff, b_buff);
			}
			else {
				gl.glGetShaderInfoLog(handle, l, i_buff, b_buff);
			}
			if(i_buff.remaining() == 0) {
				// Attempt to use the already-found version for drivers that don't
				// populate the int buffer when calling glGet[X]InfoLog
				logContents = new String(b_buff.array(), 0, l);
			} 
			else {
				logContents = new String(b_buff.array(), 0, i_buff.get());
			}
		}
		
		if(isProgram) {
			gl.glGetProgramiv(handle, GL3.GL_LINK_STATUS, result, 0);
		} else {
			gl.glGetShaderiv(handle, GL3.GL_COMPILE_STATUS, result, 0);
		}
		
		String action = isProgram ? "link" : "compile";
		String actionPT = isProgram ? "linked" : "compiled";
		if(result[0] == GL3.GL_FALSE) {
			Yeti.screwed(message + " failed to " + action + "!\n\t" + logContents);
			return;
		}
		
		if(!logEmpty) {
			if( ! groks.contains(logContents.trim())) {
				// Some drivers do fill the log even though there are no actual
				// warnings. We don't want to treat the a message like "No errors."
				// as a warning!
				Yeti.warn(message + " " + actionPT + " with warnings!\n\t" + logContents);
			}
		}
	}
	
	
}
