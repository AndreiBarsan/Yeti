package barsan.opengl.flat;

import barsan.opengl.math.Rectangle;
import barsan.opengl.math.Vector2;

public class Physics2D {
	Rectangle bounds;
	Vector2 velocity;
	Vector2 acceleration;
	Entity2D owner;
	float friction;
	
	/** Do I collide with other stuff? */
	boolean solid;
	
	/** Do I get affected by gravity? */
	boolean hasWeight;
	
	/**
	 * Creates a weightless, non-solid entity. Useful for f
	 * @param world
	 * @param position
	 */
	public Physics2D(Entity2D owner, Vector2 position) {
		this(owner, new Rectangle(position.x, position.y, 1, 1), false, false);
	}
	
	public Physics2D(Entity2D owner, Rectangle bounds, boolean solid, boolean hasWeight) {
		this.owner = owner;
		this.bounds = bounds;
		this.solid = solid;
		this.hasWeight = hasWeight;
	}
	
	public boolean collidesWith(Physics2D other) {
		return bounds.intersects(other.bounds);
	}
	
	public Rectangle getBounds() {
		return bounds;
	}
	
	public void setBounds(Rectangle bounds) {
		this.bounds.set(bounds);
	}
	
	public void setPosition(Vector2 position) {
		setPosition(position.x, position.y);
	}
	
	public void setPosition(float x, float y) {
		bounds.x = x;
		bounds.y = y;
	}
	
	void update(float delta) {
		World2D w = owner.world;
		Vector2 deltaMove = new Vector2(velocity);
		if(hasWeight) {
			Vector2 gravity = new Vector2(0, -w.getGravity());
			deltaMove.add(gravity);
		}
		deltaMove.add(acceleration);
		deltaMove.applyFriction(friction);
		
		if(solid) {
			// Better - TODO: poll line segment (position, position + bounds]
			Entity2D obstacle = w.pollPosition(new Vector2(bounds.x, bounds.y).add(deltaMove));
			if(obstacle == null) {
				bounds.x += deltaMove.x;
				bounds.y += deltaMove.y;
			} else {
				w.moveToContact(this, obstacle.physics);
			}
		}
		
	}
}
