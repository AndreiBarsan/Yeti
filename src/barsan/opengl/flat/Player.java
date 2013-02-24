package barsan.opengl.flat;

import barsan.opengl.math.Quaternion;
import barsan.opengl.math.Rectangle;
import barsan.opengl.math.Vector2;
import barsan.opengl.math.Vector3;
import barsan.opengl.planetHeads.Coin;
import barsan.opengl.planetHeads.HeavenBeam;
import barsan.opengl.rendering.AnimatedModelInstance;
import barsan.opengl.rendering.StaticModelInstance;
import barsan.opengl.resources.ResourceLoader;

public class Player extends Entity2D {

	enum State {
		Active,
		Hurt,
		LevelFinished,
		Cutscene
	}
	private State state = State.Active;
	
	public boolean wantsToWalk = false;
	public boolean wantsToJump = false;
	
	public int score = 0;
	
	public Player(Vector2 position) {
		super(new Rectangle(position.x, position.y, 0.9f, 8f), true, true,
				ResourceLoader.animatedModel("planetHeadAnimated"));
				//ResourceLoader.model("planetHead"));
		
		graphics.getTransform().updateRotation(0.0f, 1.0f, 0.0f, 90.0f);
		graphicsOffset.y = -0.75f;
		physics.friction = 200.0f;
	}
	
	private void pickUp(Coin coin) {
		score += coin.getValue();
		coin.destroy();
	}
	
	private void finishLevel() {
		System.out.println("LEVEL COMPLETE!");
		int s = score;
		reset();
		score = s + 500;
	}
	
	@Override
	protected void reset() {
		super.reset();
		score = 0;
		world.reset();
	}
	
	int i = 0;
	float acc = 0.0f;
	float floatTime = 1.2f;
	float floatTimeLeft = 0.0f;
	float floatSpeed = 40.0f;
	
	float roll = 0;
	
	@Override
	public void update(float delta) {
		
		switch (state) {
		case Active:
			if(physics.bounds.y < -100.0f) {
				reset();
			}
			
			physics.jumpInput = wantsToJump;
			
			((AnimatedModelInstance)graphics).playing = wantsToWalk && physics.onGround;
			
			if(wantsToWalk && physics.velocity.x < -0.05f) {
				graphics.getTransform().updateRotation(0.0f, 1.0f, 0.0f, -90.0f);
			} else if(wantsToWalk && physics.velocity.x > 0.05f){
				graphics.getTransform().updateRotation(0.0f, 1.0f, 0.0f, 90.0f);
			}
			
			for(int i = 0; i < physics.intersected.size(); i++) {
				Physics2D el = physics.intersected.get(i);
				if(el.owner instanceof Coin) {
					pickUp((Coin)el.owner);
				} else if(el.owner instanceof HeavenBeam) {
					// Active -> LevelFinished
					floatTimeLeft = floatTime;
					roll = 0.0f;
					state = State.LevelFinished;
					Physics2D p = getPhysics2d();
					p.velocity.set(0.0f, 2.0f);
					p.acceleration.set(0.0f, 0.0f);
					p.hasWeight = false;
					p.solid = false;
				}
			}
			break;
			
		case LevelFinished:
			Physics2D p = getPhysics2d();
			floatTimeLeft -= delta;
			roll += delta * floatSpeed;
			Quaternion q = new Quaternion(new Vector3(0.0f, 1.0f, 0.0f), 90.0f);
			q.mul(new Quaternion(new Vector3(0.1f, 0.6f, 1.0f).normalize(), roll));
			graphics.getTransform().updateRotation(q);
			graphics.getTransform().refresh();
			if(floatTimeLeft <= 0.0f) {
				// LevelFinished -> Active
				p.hasWeight = true;
				p.solid = true;
				state = State.Active;
				graphics.getTransform().updateRotation(0.0f, 1.0f, 0.0f, 90.0f);
				finishLevel();
			}
			break;
			
		case Cutscene:
		case Hurt:
			state = State.Active;
			break;

		default:
			break;
		}
		
		
		super.update(delta);
	}
}
