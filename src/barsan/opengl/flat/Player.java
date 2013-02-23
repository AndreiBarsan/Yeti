package barsan.opengl.flat;

import barsan.opengl.math.Rectangle;
import barsan.opengl.math.Vector2;
import barsan.opengl.rendering.AnimatedModelInstance;
import barsan.opengl.rendering.StaticModelInstance;
import barsan.opengl.resources.ResourceLoader;

public class Player extends Entity2D {

	public boolean wantsToWalk = false;
	public boolean wantsToJump = false;
	
	public Player(Vector2 position) {
		super(new Rectangle(position.x, position.y, 0.9f, 8f), true, true,
				ResourceLoader.animatedModel("planetHeadAnimated"));
				//ResourceLoader.model("planetHead"));
		
		graphicsOffset.y = -0.50f;
		
		physics.friction = 200.0f;
	}
	
	int i = 0;
	float acc = 0.0f;
	
	@Override
	public void update(float delta) {
		if(physics.bounds.y < -100.0f) {
			physics.bounds.y = 10.0f;
			physics.bounds.x = 0.0f;
			
			physics.velocity.x = 0.0f;
			physics.velocity.y = 0.0f;
		}
		
		physics.jumpInput = wantsToJump;
		
		((AnimatedModelInstance)graphics).playing = wantsToWalk && physics.onGround;
		
		if(wantsToWalk && physics.velocity.x < -0.05f) {
			graphics.getTransform().updateRotation(0.0f, 1.0f, 0.0f, -90.0f);
		} else if(wantsToWalk && physics.velocity.x > 0.05f){
			graphics.getTransform().updateRotation(0.0f, 1.0f, 0.0f, 90.0f);
		}
		
		super.update(delta);
	}
}
