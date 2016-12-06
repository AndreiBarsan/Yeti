package barsan.opengl.scenes;

import java.io.File;
import java.io.IOException;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.input.CameraInput;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.Shader;
import barsan.opengl.rendering.VBO;
import barsan.opengl.resources.ResourceLoader;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class TextureScene extends Scene {

	VBO geometry, textureCoords;
	Shader shader;
	
	int gindex, tindex, samplerIndex;
	
	// JOGL helper
	Texture texture;
	protected CameraInput cameraInput;
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);

		shader = ResourceLoader.shader("basicTex");
		
		geometry = new VBO(GL2.GL_ARRAY_BUFFER, 3);
		textureCoords = new VBO(GL2.GL_ARRAY_BUFFER, 3, 2);
		geometry.open();
		geometry.append(new float[] {
			0.0f, 0.0f, 0.0f,
			0.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f,
		});
		geometry.close();
		
		textureCoords.open();
		textureCoords.append(new float[] {
				0.0f, 0.0f,
				0.0f, 1.0f,
				1.0f, 1.0f
			});
		textureCoords.close();
		
		gindex = shader.getAttribLocation("vVertex");
		tindex = shader.getAttribLocation("vTexCoords");
		
		try {
			File in = new File("rustedmetal.jpg");
			texture = TextureIO.newTexture(in, true);
			
		} catch (IOException e) {
			Yeti.screwed("Could not load texture...", e);
		}

		camera.setPosition(new Vector3(0.0f, 0.0f, -3.0f));
		camera.setDirection(new Vector3(0.0f, 0.0f, -1.0f));
		Yeti.get().gl.glUseProgram(shader.getHandle());
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		super.display(drawable);
		
		shader.setU1i("colorMap", 0);
		GL3 gl = Yeti.get().gl;
		texture.bind(gl);
		
		geometry.use(gindex);
		textureCoords.use(tindex);
		gl.glDrawArrays(GL2.GL_TRIANGLES, 0, 3);
	}
}
