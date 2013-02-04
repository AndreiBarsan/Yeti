package barsan.opengl.flat;

import java.util.LinkedList;
import java.util.List;

import barsan.opengl.math.Rectangle;
import barsan.opengl.math.Vector2;

public class World2D {

	private List<Entity2D> entities = new LinkedList<>();
	
	public Entity2D pollPosition(Vector2 position) {
		// TODO: when using quadtrees, just see where position should be in the
		// tree and only check that branch
		for(Entity2D target : entities) {
			if(!(target.physics.solid)) continue;
						
			Rectangle nr = target.physics.getBounds();
			if(nr.contains(position)) {
				return target;
			}
		}
		
		return null;
	}
	
	// Moves the mover as close as possible to obstacle, without causing
	// a collision
	public void moveToContact(Entity2D mover, Entity2D obstacle) {
		// since we're working with rectangles, we need to classify the mover's
		// velocity vector into four possible states (TOP, RIGHT, BOTTOM, LEFT)
		// of the obstacle
		Rectangle or = obstacle.physics.bounds;
		Rectangle mr = mover.physics.bounds;
		float angle = mover.physics.velocity.angle();
		
		if(angle < 45 && angle >= 315) {
			// RIGHT of mover
			
		} else if (angle < 135) {
			// TOP of mover	
			
			
		} else if (angle < 225) {
			// LEFT of mover
		} else { // angle < 315
			// BOTTOM of mover
			// e.g. landed on something
			mover.physics.setPosition(mr.getX(), or.getY() + or.getHeight());
		}
	}
}
