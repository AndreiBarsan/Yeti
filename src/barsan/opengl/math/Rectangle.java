package barsan.opengl.math;

public class Rectangle {
	private static final float EPSILON = 0.0001f;
	
	private float x, y;
	private float width, height;
	public float getX() {
		return x;
	}
	
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
	
	public boolean intersects(Rectangle other) {
		return ! ( x > other.x + other.width || x + width < other.x || y > other.y + other.height || y + height < other.y);
//		return( (x > other.x && x < other.x + other.width || x < other.x && x + width > other.x )
//			&& (y > other.y && y < other.y + other.height || y < other.y && y + height > other.y) );
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
	public float getWidth() {
		return width;
	}
	public void setWidth(float width) {
		this.width = width;
	}
	public float getHeight() {
		return height;
	}
	public void setHeight(float height) {
		this.height = height;
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
	
}
