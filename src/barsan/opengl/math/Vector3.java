package barsan.opengl.math;

public class Vector3 {
	
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
}
