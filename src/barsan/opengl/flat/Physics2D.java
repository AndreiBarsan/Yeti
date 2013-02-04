package barsan.opengl.flat;

import barsan.opengl.math.Rectangle;
import barsan.opengl.math.Vector2;

public class Physics2D {
	Rectangle bounds;
	Vector2 velocity;
	Vector2 acceleration;
	float friction;
	
	/** Do I collide with other stuff? */
	boolean solid;
	
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
		bounds.setX(x);
		bounds.setY(y);
	}
	
	void update(float delta) {
		// TODO
	}
}
