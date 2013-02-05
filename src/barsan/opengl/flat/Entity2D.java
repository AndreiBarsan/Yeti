package barsan.opengl.flat;

import barsan.opengl.math.Vector2;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Model;
import barsan.opengl.rendering.ModelInstance;

public class Entity2D {
	// has graphics & position components
	ModelInstance graphics;
	Physics2D physics;
	World2D world;
	
	public Entity2D(World2D world, Vector2 position, Model model) {
		this.world = world;
		physics = new Physics2D(this, position);
		graphics = new ModelInstance(model);
		// Make sure to register ourselves so we get rendered!
		world.getScene().addModelInstance(graphics);
	}
	
	public void update(float delta) {
		physics.update(delta);
		graphics.getTransform().setTranslate(
				new Vector3(physics.bounds.x, physics.bounds.y, 0.0f));
	}
}
