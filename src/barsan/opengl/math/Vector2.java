package barsan.opengl.math;

public class Vector2 {

	public float x, y;

	public Vector2() {
		set(0.0f, 0.0f);
	}
	
	public Vector2(float x, float y) {
		set(x, y);
	}
	
	public Vector2(Vector2 other) {
		set(other);
	}
	
	public Vector2 set(Vector2 other) {
		return set(other.x, other.y);
	}
	
	public Vector2 set(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public float len() {
		return (float)Math.sqrt(x * x + y * y);
	}
	
	public float len2() {
		return x * x + y * y;
	}
	
	public Vector2 add(Vector2 other) {
		x += other.x;
		y += other.y;
		return this;
	}
	
	public Vector2 mul(float scalar) {
		x *= scalar;
		y *= scalar;
		return this;
	}
	
	public Vector2 normalize() {
		float l = len();
		x /= l;
		y /= l;
		return this;
	}
	
	public Vector2 applyFriction(float amount) {
		float l = len();
		if(l > 0.0f) {
			x /= l;
			y /= l;
			
			float reducedLength = Math.max(0.0f, l - amount);
			x *= reducedLength;
			y *= reducedLength;
		}
		return this;
	}
	
	public Vector2 applyFrictionX(float amount) {
		if(x > 0) {
			x = Math.max(0.0f, x - amount);
		} else if(x < 0) {
			x = Math.min(x + amount, 0.0f);
		}
		
		return this;
	}
	
	public float angle() {
		float a = (float)Math.atan2(y, x) * MathUtil.RAD_TO_DEG;
		if(a < 360.0f) a += 360.0f;
		return a;
	}
	
	public Vector2 copy() {
		return new Vector2(this);
	}
	
}
