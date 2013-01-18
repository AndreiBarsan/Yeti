package barsan.opengl.rendering.materials;

import javax.media.opengl.GL2;

import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.util.GLHelp;

import com.jogamp.opengl.util.texture.Texture;

public class BumpComponent implements MaterialComponent {
	
	Texture normalMap;
	
	public BumpComponent(Texture normalMap) {
		this.normalMap = normalMap;
	}
	
	@Override
	public void setup(Material m, RendererState rs, Matrix4 modelMatrix) {
		m.shader.setU1i("useBump", true);
	}
	
	@Override
	public int setupTexture(Material m, RendererState rs, int slot) {
		m.shader.setU1i("normalMap", slot);	
		rs.gl.glActiveTexture(GLHelp.textureSlot[slot]);
		normalMap.bind(rs.gl);
		normalMap.setTexParameterf(rs.gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, rs.getAnisotropySamples());
		
		// We only used one slot
		return 1;
	}
	
	@Override
	public void cleanup() {	}
	@Override
	public void dispose() { }
}