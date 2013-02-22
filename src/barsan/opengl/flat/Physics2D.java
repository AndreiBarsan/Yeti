package barsan.opengl.flat;

import javax.naming.OperationNotSupportedException;

import barsan.opengl.Yeti;
import barsan.opengl.math.Rectangle;
import barsan.opengl.math.Segment2D;
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
	
	float maxXSpeed = 20.0f;
	float maxYSpeed = 40.0f;
	
	float jumpTimeTotal = 0.1f;
	float jumpTimeLeft = 0.0f;
	
	public boolean jumpInput = false;
	public float jumpStrength = 300.0f;
	
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
		this.friction = 0.0f;
	}
	
	public boolean collidesWith(Physics2D other) {
		return bounds.overlaps(other.bounds);
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
				bounds.width / 2, bounds.y - 0.00005f));
		
		if(lastContact == this) {
			Yeti.screwed("You shouldn't touch yourself!");
		}
		
		return null != lastContact;
	}
	
	void update(float delta) {
		World2D w = owner.world;
		velocity.add(new Vector2(acceleration).mul(delta));
		
		if( friction > 0.0f ) {
			velocity.applyFrictionX(friction * delta);
		}
		
		if(velocity.x < -maxXSpeed) {
			velocity.x = -maxXSpeed;
		} else if(velocity.x > maxXSpeed) {
			velocity.x = maxXSpeed;
		}
		
		if(hasWeight) {
			if(solid) {
				boolean wasOnGround = onGround;
				boolean nowOnGround = checkOnGround();
				
				if(jumpTimeLeft > 0.0f) {
					if(jumpInput) {
						jumpTimeLeft = Math.max(0.0f, jumpTimeLeft - delta);
						velocity.y += jumpStrength * delta;
					}
				}
				
				if(nowOnGround) {
					if( ! wasOnGround) {
						jumpTimeLeft = jumpTimeTotal;
						// We just landed
						if(velocity.y <= 0.0f) {
							System.out.println("LANDED");
							w.moveToContact(this, lastContact);
							velocity.y = 0.0f;
						} else {
							System.out.println("Don't mind me, jumping through platform!");
							nowOnGround = false;
						}
					}					
				} else {					
					if(wasOnGround) {
						System.out.println("Just jumped!");
					}
					
					if(jumpTimeLeft > 0.0f && !jumpInput) {
						jumpTimeLeft = 0.0f;
					}
					
					velocity.y -= w.getGravity() * delta;
					
					if(velocity.y < -maxYSpeed) {
						velocity.y = -maxYSpeed;
					}
				}
				
				onGround = nowOnGround;
			}
			else {
				
			}
		}
		
		Vector2 deltaMove = new Vector2(velocity);
		deltaMove.mul(delta);
		
		if(solid && hasWeight) {
			w.potentialStep(bounds, deltaMove);
		}
		
		// Apply movement
		bounds.x += deltaMove.x;
		bounds.y += deltaMove.y;
	}
}
