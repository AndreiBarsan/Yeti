package barsan.opengl.math;

/**
 * Quaternion class used in 3D rotation computations. A highly well-written
 * useful article which helped me build this class: 
 * {@link "http://www.jacemiller.net/downloads/An%20Introduction%20to%20Quaternions%20and%20their%20Applications%20to%20Rotations%20in%20Computer%20Graphics.pdf"}
 * 
 * @author Andrei Bârsan
 *
 */
public class Quaternion {
	
	private static Quaternion 	aux_q1 = new Quaternion(),
								aux_q2 = new Quaternion();
	static final float COMPARE_EPSILON = 1e-5f;
	
	public float x, y, z, w;
	
	public Quaternion() {
		setIdentity();
	}
	
	public Quaternion(float x, float y, float z, float w) {
		set(x, y, z, w);
	}
	
	public Quaternion(Vector3 axis, float angle) {
		set(axis, angle);
	}
	
	public Quaternion(Quaternion other) {
		set(other);
	}
	
	public Quaternion set(Quaternion other) {
		x = other.x;
		y = other.y;
		z = other.z;
		w = other.w;
		
		return this;
	}
	
	public Quaternion set(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		
		return this;
	}
	
	/**
	 * Simple duplication method using a copy constructor, rather than Java
	 * cloning. 
	 * @return A deep copy of the Quaternion.
	 */
	public Quaternion copy() {
		return new Quaternion(this);
	}
	
	/**
	 * Sets the current quaternion based on the classical yaw, pitch and roll
	 * Euler angles.
	 * @param yaw	Yaw in degrees.
	 * @param pitch	Pitch in degrees.
	 * @param roll	Roll in degrees.
	 * @return		This object for chaining.
	 */
	public Quaternion setEuler(float yaw, float pitch, float roll) {
		yaw 	*= MathUtil.DEG_TO_RAD;
		pitch 	*= MathUtil.DEG_TO_RAD;
		roll 	*= MathUtil.DEG_TO_RAD;
		
		float c_yaw 	= (float)Math.cos(yaw / 2);
		float c_pitch 	= (float)Math.cos(pitch / 2);
		float c_roll 	= (float)Math.cos(roll / 2);

		float s_yaw 	= (float)Math.sin(yaw / 2);
		float s_pitch 	= (float)Math.sin(pitch / 2);
		float s_roll 	= (float)Math.sin(roll / 2);
		
		x = c_yaw * s_pitch * c_roll + s_yaw * c_pitch * s_roll;
		y = s_yaw * c_pitch * c_roll - c_yaw * s_pitch * s_roll;
		z = c_yaw * c_pitch * s_roll - s_yaw * s_pitch * c_roll;
		w = c_yaw * c_pitch * c_roll + s_yaw * s_pitch * s_roll;

		return this;
	}
	
	/**
	 * Classic construction case of a quaternion, from a rotation axis and an 
	 * angle.
	 * @param axis	The 3D rotation axis.
	 * @param angle	The size of the rotation angle, in degrees.
	 * @return	This object for chaining.
	 */
	public Quaternion set(Vector3 axis, float angle) {
		angle *= MathUtil.DEG_TO_RAD;
		float h_angle = angle / 2.0f;
		float as = (float)Math.sin(h_angle);
		float ac = (float)Math.cos(h_angle);
		
		// WARNING: after setting from an axis, the quaternion needs
		// to be normalized
		return this.set(axis.x * as, axis.y * as, axis.z * as, ac).nor();
	}
	
	public Matrix4 asMatrix4() {
		return new Matrix4(this);
	}
	
	public Quaternion setIdentity() {
		x = y = z = 0;
		w = 1;
		return this;
	}
	
	// TODO: remove me
	public void transform (Vector3 v) {
		aux_q1.set(this);
		aux_q1.conjugate();
		aux_q1.mulLeft(aux_q2.set(v.x, v.y, v.z, 0)).mulLeft(this);

		v.x = aux_q1.x;
		v.y = aux_q1.y;
		v.z = aux_q1.z;
	}
	
	/**
	 * Piece-wise product between this quaternion and another one. Not to be
	 * confused with the cross product!
	 * @param other The other operand.
	 * @return This object for chaining. 
	 */
	public Quaternion dot(Quaternion other) {
		x *= other.x;
		y *= other.y;
		z *= other.z;
		w *= other.w;
		
		return this;
	}
	
	public Quaternion add(Quaternion other) {
		x += other.x;
		y += other.y;
		z += other.z;
		w += other.w;
		
		return this;
	}
	
	public Quaternion mul(Quaternion other) {
		float newX = w * other.x + x * other.w + y * other.z - z * other.y;
		float newY = w * other.y + y * other.w + z * other.x - x * other.z;
		float newZ = w * other.z + z * other.w + x * other.y - y * other.x;
		float newW = w * other.w - x * other.x - y * other.y - z * other.z;
		x = newX;
		y = newY;
		z = newZ;
		w = newW;
		return this;
	}
	
	public Quaternion mulLeft (Quaternion other) {
		float newX = other.w * x + other.x * w + other.y * z - other.z * y;
		float newY = other.w * y + other.y * w + other.z * x - other.x * z;
		float newZ = other.w * z + other.z * w + other.x * y - other.y * x;
		float newW = other.w * w - other.x * x - other.y * y - other.z * z;
		x = newX;
		y = newY;
		z = newZ;
		w = newW;
		return this;
	}
	
	public Quaternion conjugate() {
		x = -x;
		y = -y;
		z = -z;
		// NOT! w = -w;
		
		return this;
	}
	
	public Quaternion inverse() {
		return conjugate().div( len2() );
	}
	
	public Quaternion div(float scalar) {
		return set(x / scalar, y / scalar, z / scalar, w / scalar);
	}
	
	public float len2() {
		return x * x +  y * y + z * z + w * w;
	}
	
	public float len() {
		return (float)Math.sqrt(x * x + y * y + z * z + w * w);
	}
	
	/**
	 * Normalization.
	 * @return	This object for chaining.
	 */
	public Quaternion nor() {
		float len2 = len2();
		if(len2 != 0.0f && (Math.abs(len2 - 1.0f) > 0.0001f)) {
			float len = (float)Math.sqrt(len2);
			x /= len;
			y /= len;
			z /= len;
			w /= len;
		}
		return this;
	}
	/*
	public float getPitch() {
		return (float) Math.atan2(2*(y*z + w*x), 1 - 2 * (z * z + w * w));
	}
	
	public float getYaw() {
		return (float) Math.asin(2*(x*z - w*y));
	}
	
	public float getRoll() {
		return (float)Math.atan2(2*(x*y + w*z), 1 - 2 * (y * y + z * z));  
	}
	//*/
	
	public Matrix4 toMatrix4() {
		return new Matrix4(this);
	}
	
	@Override
	public String toString() {
		return String.format("qu {%.2f | %.2f | %.2f | %.2f}", x, y, z, w);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(! (obj instanceof Quaternion))
			return false;
		
		if(obj == this)
			return true;
		
		Quaternion oq = (Quaternion)obj;
		return		Math.abs(x - oq.x) < COMPARE_EPSILON
				&&	Math.abs(y - oq.y) < COMPARE_EPSILON
				&&	Math.abs(z - oq.z) < COMPARE_EPSILON
				&&	Math.abs(w - oq.w) < COMPARE_EPSILON;
	}
}
