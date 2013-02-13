package barsan.opengl.flat;

import java.util.ArrayList;
import java.util.List;

import barsan.opengl.Yeti;
import barsan.opengl.math.Rectangle;
import barsan.opengl.math.Vector2;
import barsan.opengl.rendering.Scene;

public class World2D {

	/* The 3D host scene handling the rendering */
	private Scene scene;
	private List<Entity2D> entities = new ArrayList<>();
	private float gravity = 1.50f;
	
	
	public World2D(Scene scene) {
		Yeti.debug("Initialized 2D world");
		this.scene = scene;
	}
	
	public void update(float delta) {
		for(Entity2D e : entities) {
			e.update(delta);
			// Maybe e could return a boolean showing whether it changed its 
			// position? Could save a lot of unnecessary updates of the qt.
			// quadTree.rePosition(e);
		}
	}
	
	public void addEntity(Entity2D entity) {
		entities.add(entity);
		entity.init(this);
		scene.addModelInstance(entity.graphics);
	}
	
	/// Returns true if the two entities intersect
	public boolean intersect(Physics2D e1, Physics2D e2) {
		Rectangle r1 = e1.bounds;
		Rectangle r2 = e2.bounds;
		
		return r1.intersects(r2);
	}
	
	/// Tries to see if e collides with anything; if it does, return that entity
	public Physics2D collideWithLevel(Physics2D e) {
		Rectangle r = e.bounds;
		for(Entity2D target : entities) {
			if(!target.physics.solid) continue;
			
			if(target.physics.bounds.intersects(r))
				return target.physics;
		}
		
		return null;			
	}
	
	public Physics2D pollPosition(Vector2 position) {
		// TODO: when using quadtrees, just see where position should be in the
		// tree and only check that branch
		for(Entity2D target : entities) {
			if(!(target.physics.solid)) continue;
						
			Rectangle nr = target.physics.getBounds();
			if(nr.contains(position)) {
				return target.physics;
			}
		}
		
		return null;
	}
	
	// Moves the mover as close as possible to obstacle, without causing
	// a collision
	public void moveToContact(Physics2D mover, Physics2D obstacle) {
		// since we're working with rectangles, we need to classify the mover's
		// velocity vector into four possible states (TOP, RIGHT, BOTTOM, LEFT)
		// of the obstacle
		Rectangle or = obstacle.bounds;
		Rectangle mr = mover.bounds;
		float angle = mover.velocity.angle();
		
		if(angle < 45 && angle >= 315) {
			// RIGHT of mover
			
		} else if (angle < 135) {
			// TOP of mover	
			
			
		} else if (angle < 225) {
			// LEFT of mover
		} else { // angle < 315
			// BOTTOM of mover
			// e.g. landed on something
			mover.setPosition(mr.x, or.y + or.height);
		}
	}

	public float getGravity() {
		return gravity;
	}

	public void setGravity(float gravity) {
		this.gravity = gravity;
	}
	
	public Scene getScene() {
		return scene;
	}
}
