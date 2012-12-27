package barsan.opengl.math;


/**
 * Matrix helper. Inspired by libgdx implementation by badlogicgames@gmail.com
 * @author Andrei Barsan
 *
 */
public class Matrix4 {
	
	float data[] = new float[16];
	
	// Used in many computations
	private static float tmp[] = new float[16];
	private static Quaternion aux_quaternion = new Quaternion();
	private static Vector3 aux_vector = new Vector3();
	// private static Matrix4 auxM = new Matrix4();
	
	public static final int M00 = 0;	// 0;
	public static final int M01 = 4;	// 1;
	public static final int M02 = 8;	// 2;
	public static final int M03 = 12;	// 3;
	public static final int M10 = 1;	// 4;
	public static final int M11 = 5;	// 5;
	public static final int M12 = 9;	// 6;
	public static final int M13 = 13;	// 7;
	public static final int M20 = 2;	// 8;
	public static final int M21 = 6;	// 9;
	public static final int M22 = 10;	// 10;
	public static final int M23 = 14;	// 11;
	public static final int M30 = 3;	// 12;
	public static final int M31 = 7;	// 13;
	public static final int M32 = 11;	// 14;
	public static final int M33 = 15;	// 15;
	
	private static final float[] id = new float[] {
		1, 0, 0, 0,
		0, 1, 0, 0,
		0, 0, 1, 0,
		0, 0, 0, 1
	};
	
	public Matrix4() {
		setIdentity();
	}
	
	public Matrix4(float[] data) {
		set(data);
	}
	
	public Matrix4(Matrix4 transform) {
		this.set(transform);
	}

	/**
	 * Build a view matrix based on three normalized coordinates.
	 * @param vx
	 * @param vy
	 * @param vz
	 */
	public Matrix4(Vector3 vx, Vector3 vy, Vector3 vz) {
		set(vx, vy, vz);
	}
	
	public Matrix4(Quaternion quaternion) {
		set(quaternion);
	}

	public Matrix4 set(Vector3 vx, Vector3 vy, Vector3 vz) {
		setIdentity();
		
		data[M00] = vx.x;
		data[M01] = vx.y;
		data[M02] = vx.z;
		
		data[M10] = vy.x;
		data[M11] = vy.y;
		data[M12] = vy.z;
		
		data[M20] = -vz.x;
		data[M21] = -vz.y;
		data[M22] = -vz.z;
		
		return this;
	}

	public Matrix4 set(float[] data) {
		System.arraycopy(data, 0, this.data, 0, this.data.length);
		return this;
	}
	
	public Matrix4 set(Matrix4 other) {
		System.arraycopy(other.data, 0, data, 0, data.length);
		return this;
	}
	
	/**
	 * Creates a rotation matrix from the Quaternion q.
	 * @param q
	 * @return
	 */
	public Matrix4 set(Quaternion quaternion) {
		float l_xx = quaternion.x * quaternion.x;
		float l_xy = quaternion.x * quaternion.y;
		float l_xz = quaternion.x * quaternion.z;
		float l_xw = quaternion.x * quaternion.w;
		float l_yy = quaternion.y * quaternion.y;
		float l_yz = quaternion.y * quaternion.z;
		float l_yw = quaternion.y * quaternion.w;
		float l_zz = quaternion.z * quaternion.z;
		float l_zw = quaternion.z * quaternion.w;
		// Set matrix from quaternion
		data[M00] = 1 - 2 * (l_yy + l_zz);
		data[M01] = 2 * (l_xy - l_zw);
		data[M02] = 2 * (l_xz + l_yw);
		data[M03] = 0;
		
		data[M10] = 2 * (l_xy + l_zw);
		data[M11] = 1 - 2 * (l_xx + l_zz);
		data[M12] = 2 * (l_yz - l_xw);
		data[M13] = 0;
		
		data[M20] = 2 * (l_xz - l_yw);
		data[M21] = 2 * (l_yz + l_xw);
		data[M22] = 1 - 2 * (l_xx + l_yy);
		data[M23] = 0;
		
		data[M30] = 0;
		data[M31] = 0;
		data[M32] = 0;
		data[M33] = 1;
		return this;
	}
	
	/**
	 * PRE Multiplies this matrix with another one, updates the current object
	 * with the result, and returns a pointer to itsfel to allow operation
	 * chaining.
	 */
	public Matrix4 mul(Matrix4 matrix) {
		tmp[M00] = data[M00] * matrix.data[M00] + data[M01] * matrix.data[M10] + data[M02] * matrix.data[M20] + data[M03]
			* matrix.data[M30];
		tmp[M01] = data[M00] * matrix.data[M01] + data[M01] * matrix.data[M11] + data[M02] * matrix.data[M21] + data[M03]
			* matrix.data[M31];
		tmp[M02] = data[M00] * matrix.data[M02] + data[M01] * matrix.data[M12] + data[M02] * matrix.data[M22] + data[M03]
			* matrix.data[M32];
		tmp[M03] = data[M00] * matrix.data[M03] + data[M01] * matrix.data[M13] + data[M02] * matrix.data[M23] + data[M03]
			* matrix.data[M33];
		tmp[M10] = data[M10] * matrix.data[M00] + data[M11] * matrix.data[M10] + data[M12] * matrix.data[M20] + data[M13]
			* matrix.data[M30];
		tmp[M11] = data[M10] * matrix.data[M01] + data[M11] * matrix.data[M11] + data[M12] * matrix.data[M21] + data[M13]
			* matrix.data[M31];
		tmp[M12] = data[M10] * matrix.data[M02] + data[M11] * matrix.data[M12] + data[M12] * matrix.data[M22] + data[M13]
			* matrix.data[M32];
		tmp[M13] = data[M10] * matrix.data[M03] + data[M11] * matrix.data[M13] + data[M12] * matrix.data[M23] + data[M13]
			* matrix.data[M33];
		tmp[M20] = data[M20] * matrix.data[M00] + data[M21] * matrix.data[M10] + data[M22] * matrix.data[M20] + data[M23]
			* matrix.data[M30];
		tmp[M21] = data[M20] * matrix.data[M01] + data[M21] * matrix.data[M11] + data[M22] * matrix.data[M21] + data[M23]
			* matrix.data[M31];
		tmp[M22] = data[M20] * matrix.data[M02] + data[M21] * matrix.data[M12] + data[M22] * matrix.data[M22] + data[M23]
			* matrix.data[M32];
		tmp[M23] = data[M20] * matrix.data[M03] + data[M21] * matrix.data[M13] + data[M22] * matrix.data[M23] + data[M23]
			* matrix.data[M33];
		tmp[M30] = data[M30] * matrix.data[M00] + data[M31] * matrix.data[M10] + data[M32] * matrix.data[M20] + data[M33]
			* matrix.data[M30];
		tmp[M31] = data[M30] * matrix.data[M01] + data[M31] * matrix.data[M11] + data[M32] * matrix.data[M21] + data[M33]
			* matrix.data[M31];
		tmp[M32] = data[M30] * matrix.data[M02] + data[M31] * matrix.data[M12] + data[M32] * matrix.data[M22] + data[M33]
			* matrix.data[M32];
		tmp[M33] = data[M30] * matrix.data[M03] + data[M31] * matrix.data[M13] + data[M32] * matrix.data[M23] + data[M33]
			* matrix.data[M33];
		return this.set(tmp);
	}
	
	public Matrix4 mul(float scalar) {
		data[M00] *= scalar;
		data[M01] *= scalar;
		data[M02] *= scalar;
		data[M03] *= scalar;
		data[M10] *= scalar;
		data[M11] *= scalar;
		data[M12] *= scalar;
		data[M13] *= scalar;
		data[M20] *= scalar;
		data[M21] *= scalar;
		data[M22] *= scalar;
		data[M23] *= scalar;
		data[M30] *= scalar;
		data[M31] *= scalar;
		data[M32] *= scalar;
		data[M33] *= scalar;
		return this;
	}
	
	public Matrix4 tra () {
		tmp[M00] = data[M00];
		tmp[M01] = data[M10];
		tmp[M02] = data[M20];
		tmp[M03] = data[M30];
		tmp[M10] = data[M01];
		tmp[M11] = data[M11];
		tmp[M12] = data[M21];
		tmp[M13] = data[M31];
		tmp[M20] = data[M02];
		tmp[M21] = data[M12];
		tmp[M22] = data[M22];
		tmp[M23] = data[M32];
		tmp[M30] = data[M03];
		tmp[M31] = data[M13];
		tmp[M32] = data[M23];
		tmp[M33] = data[M33];
		return set(tmp);
	}
	
	public float[] getData() {
		return data;
	}
	
	public Matrix4 setIdentity() {
		set(id);
		return this;
	}
	
	
	/*
	 * 01 00 00 tx
	 * 00 01 00 ty
	 * 00 00 01 tz
	 * 00 00 00 01 
	 */
	public Matrix4 setTranslate(float x, float y, float z) {
		setIdentity();
		data[M03] = x;
		data[M13] = y;
		data[M23] = z;
		return this;
	}
	
	/*
	 * sx 00 00 00
	 * 00 sy 00 00
	 * 00 00 sz 00
	 * 00 00 00 01
	 */
	public Matrix4 setScale(float x, float y, float z) {
		setIdentity();
		data[M00] = x;
		data[M11] = y;
		data[M22] = z;
		return this;
	}
	
	public Matrix4 setScale(float s) {
		setIdentity();
		data[M00] = s;
		data[M11] = s;
		data[M22] = s;
		return this;
	}

	
	// Angle + axis rotation
	public Matrix4 setRotate(float angle, float x, float y, float z) {
		setIdentity();
		if(angle == 0) return this;
		return this.set(aux_quaternion.set(aux_vector.set(x, y, z), angle));
	}
	
	/*
	public Matrix4 setRotateNoQ(float angle, float x, float y, float z) {
		setIdentity();
		if(angle == 0) return this;
		
		float ca = (float) Math.cos(angle);
		float sa = (float) Math.sin(angle);
		
		// TODO: use quaternions		
		data[M00] = ca + x * x * (1 - ca);
		data[M01] = x * y * (1 - ca) - z * sa;
		data[M02] = x * z * (1 - ca) + y * sa;
		data[M10] = y * x * (1 - ca) + z * sa;
		data[M11] = ca + y * y * (1 - ca);
		data[M12] = y * z * (1 - ca) - x * sa;
		data[M20] = z * x * (1 - ca) - y * sa;
		data[M21] = z * y * (1 - ca) + x * sa;
		data[M22] = ca + z * z * (1 - ca);
		return this;
	}
	//*/
	
	public Matrix4 setEuler(float yaw, float pitch, float roll) {
		setIdentity();
		return this.set(aux_quaternion.setEuler(yaw, pitch, roll));
	}
	
	public Matrix4 inv() {
		float l_det = data[M30] * data[M21] * data[M12] * data[M03] - data[M20] * data[M31] * data[M12] * data[M03] - data[M30] * data[M11]
			* data[M22] * data[M03] + data[M10] * data[M31] * data[M22] * data[M03] + data[M20] * data[M11] * data[M32] * data[M03] - data[M10]
			* data[M21] * data[M32] * data[M03] - data[M30] * data[M21] * data[M02] * data[M13] + data[M20] * data[M31] * data[M02] * data[M13]
			+ data[M30] * data[M01] * data[M22] * data[M13] - data[M00] * data[M31] * data[M22] * data[M13] - data[M20] * data[M01] * data[M32]
			* data[M13] + data[M00] * data[M21] * data[M32] * data[M13] + data[M30] * data[M11] * data[M02] * data[M23] - data[M10] * data[M31]
			* data[M02] * data[M23] - data[M30] * data[M01] * data[M12] * data[M23] + data[M00] * data[M31] * data[M12] * data[M23] + data[M10]
			* data[M01] * data[M32] * data[M23] - data[M00] * data[M11] * data[M32] * data[M23] - data[M20] * data[M11] * data[M02] * data[M33]
			+ data[M10] * data[M21] * data[M02] * data[M33] + data[M20] * data[M01] * data[M12] * data[M33] - data[M00] * data[M21] * data[M12]
			* data[M33] - data[M10] * data[M01] * data[M22] * data[M33] + data[M00] * data[M11] * data[M22] * data[M33];
		if (l_det == 0f) throw new RuntimeException("non-invertible matrix");
		float inv_det = 1.0f / l_det;
		tmp[M00] = data[M12] * data[M23] * data[M31] - data[M13] * data[M22] * data[M31] + data[M13] * data[M21] * data[M32] - data[M11]
			* data[M23] * data[M32] - data[M12] * data[M21] * data[M33] + data[M11] * data[M22] * data[M33];
		tmp[M01] = data[M03] * data[M22] * data[M31] - data[M02] * data[M23] * data[M31] - data[M03] * data[M21] * data[M32] + data[M01]
			* data[M23] * data[M32] + data[M02] * data[M21] * data[M33] - data[M01] * data[M22] * data[M33];
		tmp[M02] = data[M02] * data[M13] * data[M31] - data[M03] * data[M12] * data[M31] + data[M03] * data[M11] * data[M32] - data[M01]
			* data[M13] * data[M32] - data[M02] * data[M11] * data[M33] + data[M01] * data[M12] * data[M33];
		tmp[M03] = data[M03] * data[M12] * data[M21] - data[M02] * data[M13] * data[M21] - data[M03] * data[M11] * data[M22] + data[M01]
			* data[M13] * data[M22] + data[M02] * data[M11] * data[M23] - data[M01] * data[M12] * data[M23];
		tmp[M10] = data[M13] * data[M22] * data[M30] - data[M12] * data[M23] * data[M30] - data[M13] * data[M20] * data[M32] + data[M10]
			* data[M23] * data[M32] + data[M12] * data[M20] * data[M33] - data[M10] * data[M22] * data[M33];
		tmp[M11] = data[M02] * data[M23] * data[M30] - data[M03] * data[M22] * data[M30] + data[M03] * data[M20] * data[M32] - data[M00]
			* data[M23] * data[M32] - data[M02] * data[M20] * data[M33] + data[M00] * data[M22] * data[M33];
		tmp[M12] = data[M03] * data[M12] * data[M30] - data[M02] * data[M13] * data[M30] - data[M03] * data[M10] * data[M32] + data[M00]
			* data[M13] * data[M32] + data[M02] * data[M10] * data[M33] - data[M00] * data[M12] * data[M33];
		tmp[M13] = data[M02] * data[M13] * data[M20] - data[M03] * data[M12] * data[M20] + data[M03] * data[M10] * data[M22] - data[M00]
			* data[M13] * data[M22] - data[M02] * data[M10] * data[M23] + data[M00] * data[M12] * data[M23];
		tmp[M20] = data[M11] * data[M23] * data[M30] - data[M13] * data[M21] * data[M30] + data[M13] * data[M20] * data[M31] - data[M10]
			* data[M23] * data[M31] - data[M11] * data[M20] * data[M33] + data[M10] * data[M21] * data[M33];
		tmp[M21] = data[M03] * data[M21] * data[M30] - data[M01] * data[M23] * data[M30] - data[M03] * data[M20] * data[M31] + data[M00]
			* data[M23] * data[M31] + data[M01] * data[M20] * data[M33] - data[M00] * data[M21] * data[M33];
		tmp[M22] = data[M01] * data[M13] * data[M30] - data[M03] * data[M11] * data[M30] + data[M03] * data[M10] * data[M31] - data[M00]
			* data[M13] * data[M31] - data[M01] * data[M10] * data[M33] + data[M00] * data[M11] * data[M33];
		tmp[M23] = data[M03] * data[M11] * data[M20] - data[M01] * data[M13] * data[M20] - data[M03] * data[M10] * data[M21] + data[M00]
			* data[M13] * data[M21] + data[M01] * data[M10] * data[M23] - data[M00] * data[M11] * data[M23];
		tmp[M30] = data[M12] * data[M21] * data[M30] - data[M11] * data[M22] * data[M30] - data[M12] * data[M20] * data[M31] + data[M10]
			* data[M22] * data[M31] + data[M11] * data[M20] * data[M32] - data[M10] * data[M21] * data[M32];
		tmp[M31] = data[M01] * data[M22] * data[M30] - data[M02] * data[M21] * data[M30] + data[M02] * data[M20] * data[M31] - data[M00]
			* data[M22] * data[M31] - data[M01] * data[M20] * data[M32] + data[M00] * data[M21] * data[M32];
		tmp[M32] = data[M02] * data[M11] * data[M30] - data[M01] * data[M12] * data[M30] - data[M02] * data[M10] * data[M31] + data[M00]
			* data[M12] * data[M31] + data[M01] * data[M10] * data[M32] - data[M00] * data[M11] * data[M32];
		tmp[M33] = data[M01] * data[M12] * data[M20] - data[M02] * data[M11] * data[M20] + data[M02] * data[M10] * data[M21] - data[M00]
			* data[M12] * data[M21] - data[M01] * data[M10] * data[M22] + data[M00] * data[M11] * data[M22];
		data[M00] = tmp[M00] * inv_det;
		data[M01] = tmp[M01] * inv_det;
		data[M02] = tmp[M02] * inv_det;
		data[M03] = tmp[M03] * inv_det;
		data[M10] = tmp[M10] * inv_det;
		data[M11] = tmp[M11] * inv_det;
		data[M12] = tmp[M12] * inv_det;
		data[M13] = tmp[M13] * inv_det;
		data[M20] = tmp[M20] * inv_det;
		data[M21] = tmp[M21] * inv_det;
		data[M22] = tmp[M22] * inv_det;
		data[M23] = tmp[M23] * inv_det;
		data[M30] = tmp[M30] * inv_det;
		data[M31] = tmp[M31] * inv_det;
		data[M32] = tmp[M32] * inv_det;
		data[M33] = tmp[M33] * inv_det;
		return this;
	}

	public float det() {
		return data[M30] * data[M21] * data[M12] * data[M03] - data[M20] * data[M31] * data[M12] * data[M03] - data[M30] * data[M11]
			* data[M22] * data[M03] + data[M10] * data[M31] * data[M22] * data[M03] + data[M20] * data[M11] * data[M32] * data[M03] - data[M10]
			* data[M21] * data[M32] * data[M03] - data[M30] * data[M21] * data[M02] * data[M13] + data[M20] * data[M31] * data[M02] * data[M13]
			+ data[M30] * data[M01] * data[M22] * data[M13] - data[M00] * data[M31] * data[M22] * data[M13] - data[M20] * data[M01] * data[M32]
			* data[M13] + data[M00] * data[M21] * data[M32] * data[M13] + data[M30] * data[M11] * data[M02] * data[M23] - data[M10] * data[M31]
			* data[M02] * data[M23] - data[M30] * data[M01] * data[M12] * data[M23] + data[M00] * data[M31] * data[M12] * data[M23] + data[M10]
			* data[M01] * data[M32] * data[M23] - data[M00] * data[M11] * data[M32] * data[M23] - data[M20] * data[M11] * data[M02] * data[M33]
			+ data[M10] * data[M21] * data[M02] * data[M33] + data[M20] * data[M01] * data[M12] * data[M33] - data[M00] * data[M21] * data[M12]
			* data[M33] - data[M10] * data[M01] * data[M22] * data[M33] + data[M00] * data[M11] * data[M22] * data[M33];
	}

	public Matrix4 setFrustum(float fov, float aspect, float near, float far) {
		setIdentity();
		float l_fd = (float)(1.0 / Math.tan((fov * (Math.PI / 180)) / 2.0));
		float l_a1 = (far + near) / (near - far);
		float l_a2 = (2 * far * near) / (near - far);
		data[M00] = l_fd / aspect;
		data[M10] = 0;
		data[M20] = 0;
		data[M30] = 0;
		data[M01] = 0;
		data[M11] = l_fd;
		data[M21] = 0;
		data[M31] = 0;
		data[M02] = 0;
		data[M12] = 0;
		data[M22] = l_a1;
		data[M32] = -1;
		data[M03] = 0;
		data[M13] = 0;
		data[M23] = l_a2;
		data[M33] = 0;
		
		return this;
	}

	public Matrix4 setLookAt(Vector3 eye, Vector3 center, Vector3 up) {
				
		// We're just computing the new axes based on our camera position
		Vector3 forward = new Vector3(center).sub(eye).normalize();
		Vector3 side = new Vector3(forward).cross(up).normalize();
		Vector3 newUp = new Vector3(side).cross(forward);
		
		return set(side, newUp, forward.mul(-1.0f)).mul(new Matrix4().setTranslate(-eye.x, -eye.y, -eye.z));
	}
	
	public Matrix4 setOrthogonalProjection(int x, int y, int width, int height, float near, float far) {
		data[M00] = 2.0f / (float)width;
		data[M10] = 0;
		data[M20] = 0;
		data[M30] = 0;
		data[M01] = 0;
		data[M11] = 2.0f / (float)height;
		data[M21] = 0;
		data[M31] = 0;
		data[M02] = 0;
		data[M12] = 0;
		data[M22] = 1.0f / ( far - near );
		data[M32] = 0;
		data[M03] = 0;
		data[M13] = 0;
		data[M23] = - near / (far - near);
		data[M33] = 1;
		
		return this;
	}

	/**
	 * Removes the translation information from this transform. Useful for e.g.
	 * skyboxes.
	 */
	public void clearTranslation() {
		data[M03] = 0.0f;
		data[M13] = 0.0f;
		data[M23] = 0.0f;
		System.out.println(data[M33]);
	}

	public void clearRotation() {
		/// FIXME: is kind of hacky
		data[M00] = 1;
		data[M01] = 0;
		data[M02] = 0;
		
		data[M10] = 0;
		data[M11] = 1;
		data[M12] = 0;
		
		data[M20] = 0;
		data[M21] = 0;
		data[M22] = 1;
	}
	
	public void clearRotationX() {
		/// FIXME: this is even worse
		data[M00] = 1;
		data[M10] = 0;
		data[M20] = 0;
	}

	public Vector3 getPosition() {
		return new Vector3(data[M03], data[M13], data[M23]);
	}
	
	@Override
	public String toString() {
		return String.format("{%.2f %.2f %.2f %.2f}\n{%.2f %.2f %.2f %.2f}\n{%.2f %.2f %.2f %.2f}\n{%.2f %.2f %.2f %.2f}", 
				data[M00], data[M01], data[M02], data[M03],
				data[M10], data[M11], data[M12], data[M13],
				data[M20], data[M21], data[M22], data[M23],
				data[M30], data[M31], data[M32], data[M33]);
	}
	
	// TODO: static utilities for generation perspective, orthographic (PROJECTION) or
	//	lookAt (VIEW) matrices

}
