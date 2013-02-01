package barsan.opengl.math;

public class Vector2 {

	/* pp */ float x, y;

	public Vector2(float x, float y) {
		super();
		this.x = x;
		this.y = y;
	}

	public float len() {
		return (float)Math.sqrt(x * x + y * y);
	}
	
	public float len2() {
		return x * x + y * y;
	}
	
	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}
	
	
}
