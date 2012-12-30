package barsan.opengl.math;

/**
 * Contains easy to control transform handles for scale, translation and rotation.
 * NB: Recomputes the resulting matrix on demand! This is to prevent unneeded 
 * calculations whenever setting multiple types of attributes at once.
 * 
 * @author Andrei Bârsan
 *
 */
public class Transform {
	private Matrix4 data;
	
	private Vector3 scale;
	private Vector3 translate;
	
	// TODO: QUATERNIONS QUATERNIONS QUATERNIONS
	private Vector3 rotateAxis;
	private float rotateAngle;
	
	public Transform() {
		scale = new Vector3(1.0f, 1.0f, 1.0f);
		translate = new Vector3(0.0f, 0.0f, 0.0f);
		rotateAxis = new Vector3(0.0f, 0.0f, 0.0f);
		rotateAngle = 0.0f;
		
		throw new Error("NYI");
	}
	
	public Vector3 getScale() {
		return scale;
	}

	public Vector3 getTranslate() {
		return translate;
	}

	public Vector3 getRotateAxis() {
		return rotateAxis;
	}

	public float getRotateAngle() {
		return rotateAngle;
	}
	
	public void setScale(Vector3 scale) {
		this.scale.set(scale);
	}
	
	public void setTranslate(Vector3 translate) {
		this.translate.set(translate);
	}
	
	public void setRotateAxis(Vector3 axis) {
		this.rotateAxis.set(axis);
	}
	
	public void setRotateAngle(float angle) {
		this.rotateAngle = angle;
	}
	
	/**
	 * Updates the scale and recomputes the matrix.
	 * @param scale
	 */
	public void updateScale(Vector3 scale) {
		setScale(scale); 
		computeMatrix();
	}
	
	/**
	 * Updates the translate component and recomputes the matrix.
	 * @param translate
	 */
	public void updateTranslate(Vector3 translate) {
		setTranslate(translate);
		computeMatrix();
	}
	
	/**
	 * Updates the rotation axis and recomputes the matrix.
	 * @param axis
	 */
	public void updateRotateAxis(Vector3 axis) {
		setRotateAxis(axis);
		computeMatrix();
	}
	
	public void updateRotate(Vector3 axis, float angle) {
		setRotateAxis(axis); 
		setRotateAngle(angle);
		computeMatrix();
	}
	
	private static Matrix4 	hackR = new Matrix4(), 
							hackS = new Matrix4(),
							hackT = new Matrix4(); 
	
	private void computeMatrix() {
		float ca = (float) Math.cos(rotateAngle);
		float sa = (float) Math.sin(rotateAngle);
		
		// TODO: use pre-multiplied formulas
		hackR.setRotate(rotateAngle, rotateAxis.x, rotateAxis.y, rotateAxis.z);
		hackS.setScale(scale.x, scale.y, scale.z);
		hackT.setTranslate(translate.x, translate.y, translate.z);
		
		data.set(hackR).mul(hackS).mul(hackT);
	}
	
	/**
	 * Shorthand public alias of computeMatrix().
	 * Explicitly refreshes the internal matrix;
	 */
	public void f5() {
		computeMatrix();
	}
	
	
}
