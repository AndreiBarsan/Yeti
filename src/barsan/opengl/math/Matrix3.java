package barsan.opengl.math;

public class Matrix3 {
	public static final int M00 = 0;
	public static final int M01 = 3;
	public static final int M02 = 6;
	public static final int M10 = 1;
	public static final int M11 = 4;
	public static final int M12 = 7;
	public static final int M20 = 2;
	public static final int M21 = 5;
	public static final int M22 = 8;
	
	public float[] data = new float[9];
	private float[] aux = new float[9];
	
	public Matrix3 () {
		setIdentity();
	}
	
	public Matrix3 (Matrix3 matrix) {
		set(matrix);
	}

	public Matrix3(Matrix4 matrix) {
		set(matrix);
	}

	public Matrix3 setIdentity () {
		this.data[0] = 1;
		this.data[1] = 0;
		this.data[2] = 0;

		this.data[3] = 0;
		this.data[4] = 1;
		this.data[5] = 0;

		this.data[6] = 0;
		this.data[7] = 0;
		this.data[8] = 1;

		return this;
	}
	
	public Matrix3 set (Matrix3 mat) {
		data[0] = mat.data[0];
		data[1] = mat.data[1];
		data[2] = mat.data[2];
		data[3] = mat.data[3];
		data[4] = mat.data[4];
		data[5] = mat.data[5];
		data[6] = mat.data[6];
		data[7] = mat.data[7];
		data[8] = mat.data[8];
		return this;
	}

	public Matrix3 set (Matrix4 mat) {
		data[0] = mat.data[0];
		data[1] = mat.data[1];
		data[2] = mat.data[2];
		data[3] = mat.data[4];
		data[4] = mat.data[5];
		data[5] = mat.data[6];
		data[6] = mat.data[8];
		data[7] = mat.data[9];
		data[8] = mat.data[10];
		return this;
	}
	
	public float det () {
		return data[0] * data[4] * data[8] + data[3] * data[7] * data[2] + data[6] * data[1] * data[5] - data[0] * data[7] * data[5] - data[3]
			* data[1] * data[8] - data[6] * data[4] * data[2];
	}

	public Matrix3 inv () {
		float det = det();
		if (det == 0) throw new Error("Can't invert a singular matrix");

		float inv_det = 1.0f / det;

		aux[0] = data[4] * data[8] - data[5] * data[7];
		aux[1] = data[2] * data[7] - data[1] * data[8];
		aux[2] = data[1] * data[5] - data[2] * data[4];
		aux[3] = data[5] * data[6] - data[3] * data[8];
		aux[4] = data[0] * data[8] - data[2] * data[6];
		aux[5] = data[2] * data[3] - data[0] * data[5];
		aux[6] = data[3] * data[7] - data[4] * data[6];
		aux[7] = data[1] * data[6] - data[0] * data[7];
		aux[8] = data[0] * data[4] - data[1] * data[3];

		data[0] = inv_det * aux[0];
		data[1] = inv_det * aux[1];
		data[2] = inv_det * aux[2];
		data[3] = inv_det * aux[3];
		data[4] = inv_det * aux[4];
		data[5] = inv_det * aux[5];
		data[6] = inv_det * aux[6];
		data[7] = inv_det * aux[7];
		data[8] = inv_det * aux[8];

		return this;
	}
	

	public Matrix3 transpose () {
		float v00 = data[M00];
		float v01 = data[M10];
		float v02 = data[M20];
		float v10 = data[M01];
		float v11 = data[M11];
		float v12 = data[M21];
		float v20 = data[M02];
		float v21 = data[M12];
		float v22 = data[M22];
		data[M00] = v00;
		data[M01] = v01;
		data[M02] = v02;
		data[M10] = v10;
		data[M11] = v11;
		data[M12] = v12;
		data[M20] = v20;
		data[M21] = v21;
		data[M22] = v22;
		return this;
	}

	public float[] getData() {
		return data;
	}
}
