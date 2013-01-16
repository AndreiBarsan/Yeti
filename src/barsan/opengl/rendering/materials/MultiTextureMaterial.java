package barsan.opengl.rendering.materials;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.resources.ResourceLoader;

import com.jogamp.opengl.util.texture.Texture;

/**
 * @author Andrei Bârsan
 *
 */
public class MultiTextureMaterial extends BasicMaterial {

	private float minHeight;
	private float maxHeight;
	private Texture upperTexture;
	
	public MultiTextureMaterial(Texture lowerTexture, Texture upperTexture,
			float minHeight, float maxHeight) {
		shininess = 0;
		setTexture(lowerTexture);
		this.upperTexture = upperTexture;
		this.minHeight = minHeight;
		this.maxHeight = maxHeight;
		shader = ResourceLoader.shader("heightMapPhong"); 
	}
	
	@Override
	public void setup(RendererState rendererState, Matrix4 modelMatrix) {
		super.setup(rendererState, modelMatrix);
		GL2 gl = rendererState.gl;
		
		shader.setU1f("minHeight", minHeight);
		shader.setU1f("maxHeight", maxHeight);
		shader.setU1i("colorMapB", 1);
		
		// TODO: register this as an extra component
		gl.glActiveTexture(GL.GL_TEXTURE1);
		upperTexture.bind(rendererState.gl);
		gl.glActiveTexture(GL.GL_TEXTURE0);
	}
	
}
