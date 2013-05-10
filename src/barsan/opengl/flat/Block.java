package barsan.opengl.flat;

import barsan.opengl.math.Rectangle;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.materials.TextureComponent;
import barsan.opengl.resources.ResourceLoader;

import com.jogamp.opengl.util.texture.Texture;

// Floating block you can jump on and stuff
public class Block extends Entity2D {
	
	public Block(Rectangle bounds) {
		this(bounds, null);
	}
	
	public Block(Rectangle bounds, Texture texture) {
		super(bounds, true, false, ResourceLoader.model("cube"));
		
		if(texture != null) {
			graphics.getMaterial().addComponent(new TextureComponent());
			graphics.getMaterial().setDiffuseMap(texture);
		}
	}
	
	@Override
	public void update(float delta) {
		super.update(delta);
		
		graphics.getTransform().updateScale(physics.bounds.width, physics.bounds.height, 4.0f);
		graphics.getTransform().updateTranslate(new Vector3(
				physics.bounds.x + physics.bounds.width / 2,
				physics.bounds.y + physics.bounds.height / 2, 0.0f));
	}

}
