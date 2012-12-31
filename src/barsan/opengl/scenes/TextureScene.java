package barsan.opengl.scenes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.Shader;
import barsan.opengl.rendering.VBO;
import barsan.opengl.resources.ResourceLoader;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class TextureScene extends Scene {

	VBO geometry, textureCoords;
	GL2 gl;
	Shader shader;
	
	int gindex, tindex, samplerIndex;
	
	// JOGL helper
	Texture texture;
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		gl = drawable.getGL().getGL2();
		try {
			ResourceLoader.loadShader("basicTex", "res/basicTex");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		shader = ResourceLoader.shader("basicTex");
		
		geometry = new VBO(GL2.GL_ARRAY_BUFFER, 3);
		textureCoords = new VBO(GL2.GL_ARRAY_BUFFER, 3, 2);
		geometry.put(new float[] {
			0.0f, 0.0f, 0.0f,
			0.0f, 1.0f, 0.0f,
			1.0f, 1.0f, 0.0f,
		});
		
		textureCoords.put(new float[] {
				0.0f, 0.0f,
				0.0f, 1.0f,
				1.0f, 1.0f
			});
		
		gindex = shader.getAttribLocation("vVertex");
		tindex = shader.getAttribLocation("vTexCoords");
		
		try {
			// TODO: switch to manual texture loading here; performance reasons explained:
			// http://stackoverflow.com/questions/1927419/loading-pngs-into-opengl-performance-issues-java-jogl-much-slower-than-c-sha
			File in = new File("res/tex/rustedmetal.jpg");
			texture = TextureIO.newTexture(in, true);
			
		} catch (IOException e) {
			Yeti.screwed("Could not load texture...", e);
		}

		camera.setPosition(new Vector3(0.0f, 0.0f, -3.0f));
		camera.setDirection(new Vector3(0.0f, 0.0f, -1.0f));
		gl.glUseProgram(shader.getHandle());
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		super.display(drawable);
		
		shader.setU1i("colorMap", 0);
		
		texture.bind(gl);
		
		geometry.use(gindex);
		textureCoords.use(tindex);
		gl.glDrawArrays(GL2.GL_TRIANGLES, 0, 3);
	}
}
