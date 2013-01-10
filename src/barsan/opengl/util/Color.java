package barsan.opengl.util;

public class Color {
	public static final Color WHITE 	= new Color(1.0f, 1.0f, 1.0f, 1.0f);
	public static final Color BLACK 	= new Color(0.0f, 0.0f, 0.0f, 1.0f);
	public static final Color RED 		= new Color(1.0f, 0.0f, 0.0f, 1.0f);
	public static final Color GREEN 	= new Color(0.0f, 1.0f, 0.0f, 1.0f);
	public static final Color BLUE 		= new Color(0.0f, 0.0f, 1.0f, 1.0f);
	
	public static final Color TRANSPARENTBLACK 	= new Color(0.0f, 0.0f, 0.0f, 0.0f);
	
	public float r, g, b, a;
	private float data[] = new float[4];

	public Color(float r, float g, float b, float a) {
		this.set(r, g, b, a);
	}
	
	public Color(float r, float g, float b) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = 1.0f;
	}
	
	public Color(Color other) {
		r = other.r;
		g = other.g;
		b = other.b;
		a = other.a;
	}
	
	public Color copy() {
		return new Color(this);
	}
	
	public Color set(float r, float g, float b) {
		return this.set(r, g, b, 1.0f);
	}
	
	public Color set(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		
		return this;
	}
	
	public float[] getData() {
		data[0] = r; 
		data[1] = g;
		data[2] = b;
		data[3] = a;
		return data;
	}
	
	// TODO: maybe HSV conversion and other shizznit

}
