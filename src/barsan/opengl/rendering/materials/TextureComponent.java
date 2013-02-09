package barsan.opengl.rendering.materials;

import javax.media.opengl.GL2;

import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.util.GLHelp;

public class TextureComponent implements MaterialComponent {

	@Override
	public void setup(Material m, RendererState rs, Matrix4 modelMatrix) {		
	}

	@Override
	public int setupTexture(Material m, RendererState rs, int slot) {
		rs.gl.glActiveTexture(GLHelp.textureSlot[slot]);
		m.shader.setU1i("useTexture", true);
		m.shader.setU1i("colorMap", slot);
		m.texture.bind(rs.gl);
		m.texture.setTexParameterf(rs.gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, rs.getAnisotropySamples());
		
		return 1;
	}

	@Override
	public void cleanUp(Material m, RendererState rs) {
		m.shader.setU1i("useTexture", false);
	}

	@Override
	public void dispose() {	}

}
