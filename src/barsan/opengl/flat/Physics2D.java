package barsan.opengl.flat;

import barsan.opengl.Yeti;
import barsan.opengl.math.Rectangle;
import barsan.opengl.math.Vector2;

public class Physics2D {
	public Rectangle bounds;
	public Vector2 velocity;
	public Vector2 acceleration;
	public Entity2D owner;
	public float friction;
	
	Physics2D lastContact;
	
	boolean onGround = false;
	
	/** Do I collide with other stuff? */
	boolean solid;
	
	/** Am I affected by gravity? */
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
		this.velocity = new Vector2();
		this.acceleration = new Vector2();
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
	
	private boolean checkOnGround() {
		World2D w = owner.world;
		lastContact = w.pollPosition(new Vector2(bounds.x + 
				bounds.width / 2, bounds.y - 0.05f));
		if(lastContact == this) {
			Yeti.screwed("You shouldn't touch yourself!");
		}
		return null != lastContact;
	}
	
	void jump() {
		if(onGround) {
			velocity.y = 35.0f;
		}
	}
	
	void update(float delta) {
		World2D w = owner.world;
		Vector2 deltaMove = new Vector2(velocity);
		deltaMove.add(acceleration);
		deltaMove.applyFriction(friction);
		
		if(hasWeight) {
			if(solid) {
				boolean wasOnGround = onGround;
				boolean nowOnGround = checkOnGround();
				
				if(nowOnGround) {
					if( ! wasOnGround) {
						// We just landed
						if(velocity.y <= 0.0f) {
							System.out.println("LANDED");
							w.moveToContact(this, lastContact);
							velocity.y = 0.0f;
						} else {
							//System.out.println("Don't mind me, jumping through platform!");
							nowOnGround = false;
						}
					}					
				} else {
					if(wasOnGround) {
						System.out.println("Just jumped!");
					}
					velocity.y -= w.getGravity();
					if(velocity.y < -25.0f) {
						velocity.y = -25.0f;
					}
				}
				
				onGround = nowOnGround;
			}
			else {
				
			}
		}
		
		if(solid) {
			// Now to perform sideways checks - am I hitting a wall left, right
			// or on top?
			// TODO
		}
		
		// Apply movement
		bounds.x += delta * deltaMove.x;
		bounds.y += delta * deltaMove.y;
		
	}
}
