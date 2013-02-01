package barsan.opengl.math;

public class Rectangle {
	private float x, y;
	private float width, height;
	public float getX() {
		return x;
	}
	
	public Rectangle(float x, float y, float width, float height) {
		super();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public boolean contains(Vector2 point) {
		return point.x > x && point.x < x + width
				&& point.y > y && point.y < y + height;
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
	
	
}
