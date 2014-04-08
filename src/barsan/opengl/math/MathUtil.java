package barsan.opengl.math;

/**
 * Various mathematical utilities.
 * 
 * @author Andrei Barsan
 *
 */
public class MathUtil {
	public static final float DEG_TO_RAD = 0.0174532925f;
	public static final float RAD_TO_DEG = 57.2957795f; 
	
	/**
	 * According to the proof at:
	 * http://www.lighthouse3d.com/tutorials/glsl-tutorial/the-normal-matrix/
	 * @param The original transform.
	 * @return The transpose of its inverse (3x3).
	 */
	public static Matrix3 getNormalTransform(Matrix4 transform) {
		return new Matrix3(transform).inv().transpose();
	}
	
	public static float lerp(float min, float max, float amount) {
		return min + (max - min) * amount;
	}
	
	public static float clamp(float value, float min, float max) {
		return (value < min) ? min : (value > max) ? max : value;
	}
	
	public static int clamp(int value, int min, int max) {
		return (value < min) ? min : (value > max) ? max : value;
	}
	
	public static float smoothStep(float start, float end, float amount) {
		float coef = 3 * amount * amount - 2 * amount * amount * amount;
		return start + (end - start) * coef;
	}
	
	public static float exp(float start, float end, float amount) {
		float val = computeExp(amount, 2, 5);
		return start + val * (end - start);
	}
	
	private static float computeExp(float a, float value, float power) {
		float min = (float)Math.pow(value, -power);
		float scale = 1 / (1 - min);
		
		if (a <= 0.5f) return ((float)Math.pow(value, power * (a * 2 - 1)) - min) * scale / 2;
		return (2 - ((float)Math.pow(value, -power * (a * 2 - 1)) - min) * scale) / 2;
	}
}
