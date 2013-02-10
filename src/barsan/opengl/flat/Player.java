package barsan.opengl.flat;

import barsan.opengl.math.Rectangle;
import barsan.opengl.math.Vector2;
import barsan.opengl.resources.ResourceLoader;

public class Player extends Entity2D {

	public Player(Vector2 position) {
		super(new Rectangle(position.x, position.y, 1, 2), true, true, ResourceLoader.model("planetHead"));
	}
	
	@Override
	public void update(float delta) {
		if(physics.bounds.y < -100.0f) {
			physics.bounds.y = 10.0f;
			physics.bounds.x = 0.0f;
			
			physics.velocity.x = 0.0f;
			physics.velocity.y = 0.0f;
		}
		super.update(delta);
	}
	
	public void jump() {
		physics.jump();
	}

}
