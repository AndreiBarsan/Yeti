package barsan.opengl.planetHeads;

import barsan.opengl.flat.Entity2D;
import barsan.opengl.math.Vector2;
import barsan.opengl.rendering.materials.TextureComponent;
import barsan.opengl.resources.ResourceLoader;

public class Coin extends Entity2D {

	final float omega = -60.0f;
	float angle = 0.0f;
	int value = 5;
	
	public Coin(Vector2 position) {
		super(position, ResourceLoader.model("coin"));
		graphics.getMaterial().addComponent(new TextureComponent());
		graphics.getMaterial().setDiffuseMap(ResourceLoader.texture("coin"));
		graphics.getTransform().updateScale(0.25f, 0.25f, -0.25f);
	}
	
	@Override
	public void update(float delta) {
		super.update(delta);
		
		angle += omega * delta;
		graphics.getTransform().updateRotation(0.0f, 1.0f, 0.0f, angle);
	}

	public int getValue() {
		return value;
	}
}
