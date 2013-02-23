package barsan.opengl.math;

public class Rectangle {
	private static final float EPSILON = 0.0001f;
	
	public float x, y;
	public float width, height;
	
	public Rectangle(float x, float y, float width, float height) {
		set(x, y, width, height);
	}
	
	public Rectangle(Rectangle other) {
		set(other);
	}
	
	public Rectangle set(Rectangle other) {
		return set(other.x, other.y, other.width, other.height);
	}
	
	public Rectangle set(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		return this;
	}
	
	public boolean contains(float xx, float yy) {
		return xx > x && xx < x + width
				&& yy > y && yy < y + height;
	}
	
	public boolean contains(Vector2 point) {
		return point.x > x && point.x < x + width
				&& point.y > y && point.y < y + height;
	}
	
	public boolean overlaps(Rectangle other) {
		return ! ( x >= other.x + other.width || x + width <= other.x || y >= other.y + other.height || y + height <= other.y);
	}
	
	public Rectangle intersect(Rectangle other) {
		assert this.overlaps(other);
		// TODO: maybe refactor using leftmost and topmost rect. variables (as aux)
		float outX, outY;
		outX = Math.max(x, other.x);
		outY = Math.min(y, other.y);
		
		float outW, outH;
		if(other.x < x + width && other.x + other.width < x + width) {
			// Other is really narrow
			outW = other.width;
		} else if(other.x < x && other.x + other.width > x + width) {
			// Other is reeeaaally wide
			outW = width;
		} else if(other.x > x && other.x + other.width > x + width) {
			outW = x + width - other.x;
		} else if(other.x < x && other.x + other.width < x + width) {
			outW = other.x + other.width - x;
		} else {
			outW = 0.0f;
			assert false : "Something went wrong";
		}
		
		if(other.y < y + height && other.y + other.height < y + height) {
			// Other is really short
			outH = other.height;
		} else if(other.y < y && other.y + other.height > y + height) {
			// Other is reeeaaally tall
			outH = height;
		} else if(other.y > y && other.y + other.height > y + height) {
			outH = y + height - other.y;
		} else if(other.y < y && other.y + other.height < y + height){
			outH = other.y + other.height - y;
		} else {
			outH = 0.0f;
			assert false : "Something went wrong";
		}
		
		return new Rectangle(outX, outY, outW, outH);
	}
	
	public Rectangle copy() {
		return new Rectangle(this);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(! (obj instanceof Rectangle)) return false;
		Rectangle other = (Rectangle)obj;
		return 		Math.abs(x - other.x) < EPSILON
				&& 	Math.abs(y - other.y) < EPSILON
				&& 	Math.abs(height - other.height) < EPSILON
				&& 	Math.abs(width - other.width) < EPSILON;
	}
	
	@Override
	public String toString() {
		return String.format("[x=%d, y=%d, w=%d, h=%d]", x, y, width, height);
	}
	
}
