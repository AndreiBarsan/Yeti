package barsan.opengl.flat;

import java.util.ArrayList;
import java.util.List;

import barsan.opengl.Yeti;
import barsan.opengl.math.MathUtil;
import barsan.opengl.math.Rectangle;
import barsan.opengl.math.Segment2D;
import barsan.opengl.math.Vector2;
import barsan.opengl.rendering.Scene;

public class World2D {

	/* The 3D host scene handling the rendering */
	private Scene scene;
	private List<Entity2D> entities = new ArrayList<>();
	private List<Entity2D> toDispose = new ArrayList<>();
	private float gravity = 200.0f;
	private boolean updating = false;
	
	public World2D(Scene scene) {
		Yeti.debug("Initialized 2D world");
		this.scene = scene;
	}
	
	public void update(float delta) {
		updating = true;
		for(int i = 0; i < entities.size() - 1; i++) {
			Rectangle eBounds = entities.get(i).getPhysics2d().getBounds();
			entities.get(i).getPhysics2d().intersected.clear();
			for(int j = i + 1; j < entities.size(); j++) {
				
				if(entities.get(j).getPhysics2d().getBounds().overlaps(eBounds)) {
					entities.get(i).getPhysics2d().addContact(entities.get(j).getPhysics2d());
					entities.get(j).getPhysics2d().addContact(entities.get(i).getPhysics2d());
				}
			}
		}
		
		for(int i = 0; i < entities.size(); i++) {
			entities.get(i).update(delta);
			// Maybe e could return a boolean showing whether it changed its 
			// position? Could save a lot of unnecessary updates of the qt.
			// quadTree.rePosition(e);
		}
		updating = false;
		
		for(int i = 0; i < toDispose.size(); i++) {
			scene.removeModelInstance(toDispose.get(i).getGraphics());
			entities.remove(toDispose.get(i));
		}
		toDispose.clear();
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
		
		return r1.overlaps(r2);
	}
	
	/// Tries to see if e collides with anything; if it does, return that entity
	public Physics2D collideWithLevel(Physics2D e) {
		Rectangle r = e.bounds;
		for(Entity2D target : entities) {
			if(!target.physics.solid) continue;
			
			if(target.physics.bounds.overlaps(r))
				return target.physics;
		}
		
		return null;			
	}
	
	public Rectangle testRectangleFree(Rectangle original, Rectangle r) {
		for(Entity2D t : entities) {
			if(!t.physics.solid) continue;
			if(original.equals(t.physics.bounds)) continue;
			
			if(t.physics.bounds.overlaps(r)) return t.physics.bounds;
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
	
	public Physics2D pollSegment(Segment2D segment, Physics2D toExclude) {
		return null;
	}
	
	// Moves the mover as close as possible to obstacle, without causing
	// a collision
	public void moveToContactFloor(Physics2D mover, Physics2D obstacle) {
		// since we're working with rectangles, we need to classify the mover's
		// velocity vector into four possible states (TOP, RIGHT, BOTTOM, LEFT)
		// of the obstacle
		Rectangle or = obstacle.bounds;
		Rectangle mr = mover.bounds;
		
		mover.setPosition(mr.x, or.y + or.height);
			
		assert( ! mr.overlaps(or)) : "The contact should no longer be true";
	}

	public void remove(Entity2D e) {
		if(updating) {
			toDispose.add(e);
		} else {
			entities.remove(e);
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
