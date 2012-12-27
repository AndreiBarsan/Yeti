package barsan.opengl.rendering;

import barsan.opengl.math.Matrix4;
import barsan.opengl.resources.ResourceLoader;

import com.jogamp.opengl.util.texture.Texture;

/**
 * Just like a regular material (responds to light, has a texture) but 
 * with the added option of mapping its color based on a heightmap.
 * @author Andrei Bârsan
 *
 */
public class HeightMapMaterial extends BasicMaterial {

	private float minHeight;
	private float maxHeight;
	
	private boolean useHeight = false;
	
	public HeightMapMaterial(Texture groundTexture) {
		this(groundTexture, -10, 10);
		
		shininess = 1;
	}
	
	public HeightMapMaterial(Texture heightTexture, float minHeight, float maxHeight) {
		super();
		setTexture(heightTexture);
		this.minHeight = minHeight;
		this.maxHeight = maxHeight;
		shader = ResourceLoader.shader("heightMapPhong"); 
	}
	
	@Override
	public void setup(RendererState rendererState, Matrix4 modelMatrix) {
		// Map everything
		super.setup(rendererState, modelMatrix);
		
		shader.setU1f("minHeight", minHeight);
		shader.setU1f("maxHeight", maxHeight);
		shader.setU1i("useHeight", useHeight);
		
	}
}
