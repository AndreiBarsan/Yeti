package barsan.opengl.flat;

import barsan.opengl.math.Rectangle;
import barsan.opengl.math.Vector2;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.StaticModel;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.StaticModelInstance;

public class Entity2D {
	// has graphics & position components
	ModelInstance graphics;
	Physics2D physics;
	World2D world;
	
	protected Vector3 graphicsOffset = new Vector3();
	
	public Entity2D(Vector2 position, StaticModel model) {
		physics = new Physics2D(this, position);
		graphics = new StaticModelInstance(model);
	}
	
	public Entity2D(Rectangle bounds, boolean solid, boolean hasWeight, StaticModel model) {
		physics = new Physics2D(this, bounds, solid, hasWeight);
		graphics = new StaticModelInstance(model);
	}
	
	public void init(World2D world) {
		this.world = world;
	}
	
	public void update(float delta) {
		physics.update(delta);
		graphics.getTransform().updateTranslate(
				new Vector3(physics.bounds.x + graphicsOffset.x,
						physics.bounds.y + graphicsOffset.y,
						graphicsOffset.z));
	}
}
