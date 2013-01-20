package barsan.opengl.math;

/**
 * Contains easy to control transform handles for scale, translation and rotation.
 * Allows easier management of combined transformations (rotate, scale, translate)
 * helping avoid the required (verbose) matrix multiplications.
 * 
 * @author Andrei Bârsan
 *
 */
public class Transform {
	private Matrix4 transformMatrix;
	
	private Vector3 scale;
	private Vector3 translate;
	private Quaternion rotation;
	
	public Transform() {
		scale = new Vector3(1.0f, 1.0f, 1.0f);
		translate = new Vector3(0.0f, 0.0f, 0.0f);
		rotation = new Quaternion(new Vector3(0.0f, 1.0f, 0.0f), 0.0f);
		
		transformMatrix = new Matrix4();
	}
	
	public Vector3 getScale() {
		return scale;
	}

	public Vector3 getTranslate() {
		return translate;
	}

	public Quaternion getRotation() {
		return rotation;
	}
	
	/**
	 * Sets the scale component. Does NOT refresh the internal matrix.
	 * Follow with a call to refresh() to apply.
	 */
	public Transform setScale(float scale) {
		this.scale.set(scale, scale, scale);
		return this;
	}
	
	/**
	 * Sets the scale component. Does NOT refresh the internal matrix.
	 * Follow with a call to refresh() to apply.
	 */
	public Transform setScale(float sx, float sy, float sz) {
		this.scale.set(sx, sy, sz);
		return this;
	}
	
	/**
	 * Sets the scale component. Does NOT refresh the internal matrix.
	 * Follow with a call to refresh() to apply.
	 */
	public Transform setScale(Vector3 scale) {
		this.scale.set(scale);
		return this;
	}
	
	/**
	 * Sets the translate component. Does NOT refresh the internal matrix.
	 * Follow with a call to refresh() to apply.
	 */
	public Transform setTranslate(float tx, float ty, float tz) {
		this.translate.set(tx, ty, tz);
		return this;
	}
	
	/**
	 * Sets the translate component. Does NOT refresh the internal matrix.
	 * Follow with a call to refresh() to apply.
	 */
	public Transform setTranslate(Vector3 translate) {
		this.translate.set(translate);
		return this;
	}
	
	/**
	 * Sets the translate component and the scale (uniformly, with the value 
	 * scale on all 3 dimensions). Does NOT refresh the internal matrix.
	 * Follow with a call to refresh() to apply.
	 */
	public Transform setTranslateScale(float tx, float ty, float tz, float scale) {
		this.translate.set(tx, ty, tz);
		this.scale.set(scale, scale, scale);
		return this;
	}
	
	/**
	 * Sets the translate and the scale components. Does NOT refresh the internal 
	 * matrix. Follow with a call to refresh() to apply.
	 */
	public Transform setTranslateScale(float tx, float ty, float tz, 
			float sx, float sy, float sz) {
		this.translate.set(tx, ty, tz);
		this.scale.set(sx, sy, sz);
		return this;
	}
	
	/**
	 * Sets the rotation component. Does NOT refresh the internal matrix.
	 * Follow with a call to refresh() to apply.
	 */
	public Transform setRotation(Quaternion rotation) {
		this.rotation.set(rotation);
		return this;
	}
	
	/**
	 * Sets the rotation component. Does NOT refresh the internal matrix.
	 * Follow with a call to refresh() to apply.
	 */
	public Transform setRotation(Vector3 axis, float angle) {
		this.rotation.set(axis, angle);
		return this;
	}
	
	/**
	 * Sets the rotation component. Does NOT refresh the internal matrix.
	 * Follow with a call to refresh() to apply.
	 */
	public Transform setRotation(float ax, float ay, float az, float angle) {
		this.rotation.set(av.set(ax, ay, az), angle);
		return this;
	}
	private static Vector3 av = new Vector3();
	private static Quaternion aq = new Quaternion();
		
	/**
	 * Overwrites this transform with a new matrix.
	 * @param matrix
	 */
	public Transform setMatrix(Matrix4 matrix) {
		this.transformMatrix.set(matrix);
		return this;
	}
	
	/**
	 * Sets the scale component and recomputes the matrix.
	 */
	public Transform updateScale(float scale) {
		this.scale.set(scale, scale, scale);
		computeMatrix();
		return this;
	}
	
	/**
	 * Sets the scale component and recomputes the matrix.
	 */
	public Transform updateScale(float sx, float sy, float sz) {
		this.scale.set(sx, sy, sz);
		computeMatrix();
		return this;
	}
	
	/**
	 * Updates the scale and recomputes the matrix.
	 * @param scale
	 */
	public Transform updateScale(Vector3 scale) {
		setScale(scale); 
		computeMatrix();
		return this;
	}
	
	/**
	 * Updates the translation component and recomputes the matrix.
	 */
	public Transform updateTranslate(float tx, float ty, float tz) {
		setTranslate(tx, ty, tz);
		computeMatrix();
		return this;
	}
	
	/**
	 * Updates the translate component and recomputes the matrix.
	 */
	public Transform updateTranslate(Vector3 translate) {
		setTranslate(translate);
		computeMatrix();
		return this;
	}
	
	/**
	 * Sets the translate component and the scale (uniformly, with the value 
	 * scale on all 3 dimensions) and recomputes the matrix.
	 */
	public Transform updateTranslateScale(float tx, float ty, float tz, float scale) {
		setTranslate(tx, ty, tz);
		setScale(scale, scale, scale);
		computeMatrix();
		return this;
	}
	
	/**
	 * Sets the translate and the scale components and recomputes the matrix.
	 */
	public Transform updateTranslateScale(float tx, float ty, float tz, 
			float sx, float sy, float sz) {
		setTranslate(tx, ty, tz);
		setScale(sx, sy, sz);
		computeMatrix();
		return this;
	}

	
	public Transform updateRotation(float ax, float ay, float az, float angle) {
		return updateRotation(aq.set(av.set(ax, ay, az).normalize(), angle));
	}
	
	/**
	 * Updates the rotation component and recomputes the matrix.
	 */
	public Transform updateRotation(Quaternion rotation) {
		setRotation(rotation);
		computeMatrix();
		return this;
	}
	
	
	private static Matrix4 	auxS = new Matrix4(),
							auxT = new Matrix4(),
							auxR = new Matrix4();
	
	private void computeMatrix() {
		auxS.setScale(scale.x, scale.y, scale.z);
		auxT.setTranslate(translate.x, translate.y, translate.z);
		auxR.setRotate(rotation);
		
		transformMatrix.set(auxT).mul(auxR).mul(auxS);
	}
	
	/**
	 * Shorthand public alias of computeMatrix().
	 * Explicitly refreshes the internal matrix;
	 */
	public void refresh() {
		computeMatrix();
	}
	
	/**
	 * Outputs the internal matrix representation for use in GL computations.
	 */
	public Matrix4 get() {
		return transformMatrix;
	}
	
	/**
	 * Outputs the internal matrix data for use in GL computations.
	 */
	public float[] getData() {
		return transformMatrix.data;
	}
}
