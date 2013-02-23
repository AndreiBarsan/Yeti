package barsan.opengl.flat;

import barsan.opengl.math.Rectangle;
import barsan.opengl.math.Vector2;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.AnimatedMaterial;
import barsan.opengl.rendering.AnimatedModel;
import barsan.opengl.rendering.AnimatedModelInstance;
import barsan.opengl.rendering.Model;
import barsan.opengl.rendering.StaticModel;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.StaticModelInstance;

public class Entity2D {
	// has graphics & position components
	protected ModelInstance graphics;
	protected Physics2D physics;
	protected World2D world;
	
	protected Vector3 graphicsOffset = new Vector3();
	
	public Entity2D(Vector2 position, Model model) {
		this(new Rectangle(position.x, position.y, 1, 1), false, false, model);
	}
	
	public Entity2D(Rectangle bounds, boolean solid, boolean hasWeight, Model model) {
		physics = new Physics2D(this, bounds, solid, hasWeight);
		if(model instanceof StaticModel) {
			graphics = new StaticModelInstance((StaticModel)model);
		} else {
			graphics = new AnimatedModelInstance((AnimatedModel)model, new AnimatedMaterial());
		}
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
	
	public ModelInstance getGraphics() {
		return graphics;
	}
	
	public Physics2D getPhysics2d() {
		return physics;
	}
	
	/* pp */ void hitWallSide() { }
	/* pp */ void jumped() { }
	/* pp */ void landed() { }
	/* pp */ void hitCeiling() { }
}
