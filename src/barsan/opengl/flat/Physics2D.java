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
	
	float maxXSpeed = 40.0f;
	float maxYSpeed = 80.0f;
	
	float jumpTimeTotal = 0.05f;
	float jumpTimeLeft = 0.0f;
	
	public boolean jumpInput = false;
	public float burstJumpStrength = 1400.0f;
	
	/**
	 * Creates a weightless, non-solid entity.
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
						velocity.y += burstJumpStrength * delta;
					}
				}
				
				if(nowOnGround) {
					if( ! wasOnGround) {
						jumpTimeLeft = jumpTimeTotal;
						// We just landed
						if(velocity.y <= 0.0f) {
							owner.landed();
							w.moveToContactFloor(this, lastContact);
							velocity.y = 0.0f;
						} else {
							nowOnGround = false;
						}
					}					
				} else {					
					if(wasOnGround) {
						owner.jumped();
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
		}
		
		Vector2 deltaMove = new Vector2(velocity);
		deltaMove.mul(delta);
		
		if(solid && hasWeight) {
			// Sideways checks
			Rectangle moved = new Rectangle(bounds);
			moved.x += deltaMove.x;

			Rectangle result = w.testRectangleFree(bounds, moved);
			if(result == null) {
				bounds.x += deltaMove.x;
			} else {
				
				if(deltaMove.y == 0) {
					owner.hitWallSide();
					if(deltaMove.x > 0.01f) {
						if(bounds.x < result.x) {
							bounds.x = result.x - bounds.width;
							velocity.x = 0.0f;
						}
					} else if(deltaMove.x < -0.01f) {
						if(bounds.x > result.x) {
							bounds.x = result.x + result.width;
							velocity.x = 0.0f;
						}
					}
				} else {
					if(deltaMove.y > 0.01f && moved.y < result.y) {
						// Hit a ceiling
						bounds.x += deltaMove.x;
						jumpTimeLeft = 0.0f;
						owner.hitCeiling();
						velocity.y = 0.0f;		// here, some bounce could be introduced
					} else if(deltaMove.y < -0.01f && moved.y < result.y) {
						
						// This is just hacky
						if(moved.x > result.x && moved.x + moved.width < result.x + result.width) {
							bounds.x += deltaMove.x;
						}
					}
				}
				
			}
		} else {
			// Apply movement
			bounds.x += deltaMove.x;
		}
		
		bounds.y += deltaMove.y;
	}
}
