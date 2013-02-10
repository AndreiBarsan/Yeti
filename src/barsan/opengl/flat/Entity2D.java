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
	
	public Entity2D(Vector2 position, Model model) {
		physics = new Physics2D(this, position);
		graphics = new ModelInstance(model);
	}
	
	public void init(World2D world) {
		this.world = world;
	}
	
	public void update(float delta) {
		physics.update(delta);
		graphics.getTransform().updateTranslate(
				new Vector3(physics.bounds.x, physics.bounds.y, 0.0f));
	}
}
