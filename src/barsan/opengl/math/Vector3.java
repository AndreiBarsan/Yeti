package barsan.opengl.math;

public class Vector3 {
	
	public static final Vector3 ZERO = new Vector3();
	public static final Vector3 X = new Vector3(1.0f, 0.0f, 0.0f);
	public static final Vector3 Y = new Vector3(0.0f, 1.0f, 0.0f);
	public static final Vector3 Z = new Vector3(0.0f, 0.0f, 1.0f);
	public static final Vector3 UP = Y;
	
	public static final float EPSILON = 0.000001f;
	
	// Computation helpers
	static Quaternion aux1 = new Quaternion();
	static Quaternion aux2 = new Quaternion();
	static Quaternion aux3 = new Quaternion();
	
	// Internal data
	public float x, y, z;

	public Vector3() { }
	
	public Vector3(float x, float y, float z) {
		set(x, y, z);
	}
	
	public Vector3(Vector3 v) {
		set(v);
	}
	
	public Vector3 set(Vector3 other) {
		x = other.x;
		y = other.y;
		z = other.z;
		return this;
	}
	
	public Vector3 set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public Vector3 copy() {
		return new Vector3(this);
	}

	public Vector3 add(Vector3 other) {
		x += other.x;
		y += other.y;
		z += other.z;
		return this;
	}
	
	public Vector3 sub(Vector3 other) {
		x -= other.x;
		y -= other.y;
		z -= other.z;
		return this;
	}
	
	public Vector3 mul(float factor) {
		x *= factor;
		y *= factor;
		z *= factor;
		
		return this;
	}
	
	public float dot(Vector3 other) {
		return x * other.x + y * other.y + z * other.z;
	}
	
	public Vector3 setX(float x) {
		this.x = x;
		return this;
	}
	
	public Vector3 setY(float y) {
		this.y = y;
		return this;
	}
	
	public Vector3 setZ(float z) {
		this.z = z;
		return this;
	}
	
	/** Multiplies the vector by the given matrix. */
	public Vector3 mul (Matrix4 matrix) {
		float l_mat[] = matrix.getData();
		return this.set(x * l_mat[Matrix4.M00] + y * l_mat[Matrix4.M01] + z * l_mat[Matrix4.M02] + l_mat[Matrix4.M03], x
			* l_mat[Matrix4.M10] + y * l_mat[Matrix4.M11] + z * l_mat[Matrix4.M12] + l_mat[Matrix4.M13], x * l_mat[Matrix4.M20] + y
			* l_mat[Matrix4.M21] + z * l_mat[Matrix4.M22] + l_mat[Matrix4.M23]);
	}	
	
	/**
	 * Transforms this vector by applying the rotations described by the 
	 * quaternion.
	 * 
	 * @param rotation
	 * @return This object for chaining.
	 */
	public Vector3 transform(Quaternion rotation) {
		aux2.set(rotation).conjugate();
		aux2.mulLeft(aux1.set(x, y, z, 0)).mulLeft(rotation);
		
		return set(aux3.x, aux3.y, aux3.z);
	}

	
	public Vector3 cross(Vector3 other) {
		Vector3 aux = new Vector3();
		aux.x = y * other.z - z * other.y;
		aux.y = z * other.x - x * other.z;
		aux.z = x * other.y - y * other.x;
		
		return this.set(aux);
	}

	/**
	 * Scalar product of the current vector and other. This basically signifies
	 * the cosine of the angle between the two vectors.
	 * 
	 * @param other The other vector.
	 * @return The cosine of the angle between this and other.
	 */
	public float scal(Vector3 other) {
		return x * other.x + y * other.y + z * other.z;
	}
	
	/**
	 * Returns the angle between this vector and some other vector.
	 * @param other The other vector.
	 * @return The angle between this and other, in radians.
	 */
	public float angle(Vector3 other) {
		return (float)Math.acos(scal(other));
	}
	
	public float len() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}
	
	public float dist(Vector3 other) {
		float 	dx = other.x - x,
				dy = other.y - y,
				dz = other.z - z;
		
		return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public Vector3 normalize() {
		float len = len();
		x /= len;
		y /= len;
		z /= len;
		return this;
	}

	public Vector3 inv() {
		return this.mul(-1f);
	}
	
	/**
	 * Transforms this vector by projecting it into another coord. system, as
	 * defined by the matrix.
	 * 
	 * @param matrix Transformation matrix of the new coord. system. 
	 * @return This vector for chaining.
	 */
	public Vector3 project(Matrix4 matrix) {
		float l_mat[] = matrix.data;
		float l_w = x * l_mat[Matrix4.M30] + y * l_mat[Matrix4.M31] + z * l_mat[Matrix4.M32] + l_mat[Matrix4.M33];
		return this.set(
				(x * l_mat[Matrix4.M00] + y * l_mat[Matrix4.M01] + z * l_mat[Matrix4.M02] + l_mat[Matrix4.M03]) / l_w,
				(x * l_mat[Matrix4.M10] + y * l_mat[Matrix4.M11] + z * l_mat[Matrix4.M12] + l_mat[Matrix4.M13]) / l_w,
				(x * l_mat[Matrix4.M20] + y * l_mat[Matrix4.M21] + z * l_mat[Matrix4.M22] + l_mat[Matrix4.M23]) / l_w);
		
		/* If we project the vector in its original space (assumed identity-matrix), the formula becomes:
		 * this.set(
		 * 	x * 1 +     0 + 0 +     0 / 1
		 *  0     + y * 1 + 0 +     0 / 1
		 *  0     +         0 + z * 1 / 1) == this
		 *  
		 *  !!!
		 * 
		 * 
		 * */
	}

	@Override
	public String toString() {
		return String.format("[%.2f, %.2f, %.2f]", x, y, z);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(super.equals(obj)) {
			return true;
		}
		
		if(! (obj instanceof Vector3)) {
			return false;
		}
		
		Vector3 ov = (Vector3)obj;
		return	Math.abs(ov.x - x) < EPSILON
			&&	Math.abs(ov.y - y) < EPSILON
			&&	Math.abs(ov.z - z) < EPSILON;
	}
	
	/***************************************************************************
	 * Static utilities
	 */
	
	public static Vector3[] copyOf(Vector3[] array) {
		Vector3 out[] = new Vector3[array.length];
		for(int i = 0; i < array.length; ++i) {
			out[i] = array[i].copy();
		}
		
		return out;
	}
	
	public static Vector3[] copyOfIndices(Vector3[] array, int... indices) {
		Vector3 out[] = new Vector3[indices.length];
		int koff = 0;
		for(int i : indices) {
			out[koff++] = array[i]; 
		}
		return out;
	}
}
