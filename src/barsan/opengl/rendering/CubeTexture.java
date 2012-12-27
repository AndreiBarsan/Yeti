package barsan.opengl.rendering;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;

/**
 * TODO: refactor this horror!
 * 
 * @author Andrei Bârsan
 *
 */
public class CubeTexture {
	
	private Texture texture;
	
	public String[] names = new String[] {
			"xpos", "xneg", "ypos", "yneg", "zpos", "zneg"
	};
	
	public int[] cubeSlots = new int[] {
		GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X,
		GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
		GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
		GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
		GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
		GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
	};
	
	public CubeTexture() {
		texture = new Texture(GL2.GL_TEXTURE_CUBE_MAP);
	}
	
	public Texture getTexture() {
		return texture;
	}
	
	public void bind(GL2 gl) {
		texture.bind(gl);
	}
}
