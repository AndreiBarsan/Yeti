package barsan.opengl.math;

public class Segment2D {
	public Vector2 start;
	public Vector2 end;
		
	public Segment2D(Vector2 start, Vector2 end) {
		super();
		this.start = start;
		this.end = end;
	}

	public boolean intersects(Segment2D other) {
		// TODO:
		return false;
	}
	
	public boolean contains(Vector2 point) {
		// Use epsilon and distance
		return false;
	}
}
