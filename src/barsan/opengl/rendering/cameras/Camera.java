package barsan.opengl.rendering.cameras;

import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Quaternion;
import barsan.opengl.math.Vector3;

public abstract class Camera {

	protected static final float DEFAULT_NEAR = 0.25f;
	protected static final float DEFAULT_FAR = 1000.00f;
	static final Matrix4 invProjectionView = new Matrix4();
	static Matrix4 aux_matrix = new Matrix4();
	static Quaternion pq = new Quaternion();
	
	protected Vector3 eyePosition;
	protected Vector3 up;
	protected Vector3 direction;
	protected int width;
	protected int height;
	private int viewportX;
	private int viewportY;
	
	protected Quaternion currentRotation = new Quaternion();
	
	// Dirty flags
	protected boolean projectionDirty;
	protected boolean viewDirty;
	
	static Vector3 aux_v3 = new Vector3();
	float yawAccum = 0.0f;
	float pitchAccum = 0.0f;
	float speed = 0.9f;
	float rotSpeed = 6.0f;
	protected float frustumNear;
	protected float frustumFar;
	
	protected Matrix4 projection = new Matrix4();
	private Matrix4 vm = new Matrix4();
	
	public Camera(Vector3 position, Vector3 direction, int width, int height) {
		this(position, direction, new Vector3(0.0f, 1.0f, 0.0f), width, height, DEFAULT_NEAR, DEFAULT_FAR);
	}
	
	public Camera(Vector3 position, Vector3 direction, Vector3 up, int width, int height,
			float near, float far) {
		this.direction = direction;
		this.eyePosition = position;
		this.width = width;
		this.height = height;
		this.up = up;
		this.frustumNear = near;
		this.frustumFar = far;
		currentRotation = new Quaternion(direction, 0);
		
		projectionDirty = true;
		viewDirty = true;
	}

	public void forward() {
		Vector3 d = new Vector3(direction).mul(speed);
		eyePosition.add(d);
		viewDirty = true;
	}

	public void backward() {
		Vector3 d = new Vector3(direction).mul(-speed);
		eyePosition.add(d);
		viewDirty = true;
	}

	public void strafeLeft() {
		Vector3 d = new Vector3(direction).cross(new Vector3(up)).normalize().mul(speed);
		eyePosition.add(d);
		viewDirty = true;
	}

	public void strafeRight() {
		Vector3 d = new Vector3(up).cross(new Vector3(direction)).normalize().mul(speed);
		eyePosition.add(d);
		viewDirty = true;
	}

	public void strafeDown() {
		eyePosition.add(new Vector3(up).mul(-speed));
		viewDirty = true;
	}

	public void strafeUp() {
		eyePosition.add(new Vector3(up).mul(speed));
		viewDirty = true;
	}

	public void turnLeft() {
		direction.mul(aux_matrix.setRotate(-rotSpeed, up.x, up.y, up.z));
		viewDirty = true;
	}

	public void turnRight() {
		direction.mul(aux_matrix.setRotate(rotSpeed, up.x, up.y, up.z));
		viewDirty = true;
	}

	public void turnUp() {
		Vector3 right = new Vector3(direction).cross(up).normalize();
		direction.mul(aux_matrix.setRotate(rotSpeed, right.x, right.y, right.z));
		viewDirty = true;
	}

	public void turnDown() {
		Vector3 left = new Vector3(up).cross(direction).normalize();
		direction.mul(aux_matrix.setRotate(rotSpeed, left.x, left.y, left.z));
		viewDirty = true;
	}

	public Vector3 getUp() {
		return up;
	}

	public void setUp(Vector3 up) {
		this.up = up;
	}

	public Matrix4 getView() {
		// This 'dirty' flag idea won't work. Why? Well, when someone uses
		// setDirection(newVal) it's all a-ok. But when you use getDirection().setX(-13),
		// there's no way to tell that happened and to set the right flags.
		refreshView();
		return vm.cpy();
	}

	public Matrix4 getProjection() {
		// Do it anyway
		refreshProjection();
		return projection;
	}

	protected abstract void refreshProjection();
	
	public void update() {
		refreshProjection();
		refreshView();
		assert false : "not yet";
	}

	private void refreshView() {
		vm.setLookAt(eyePosition, new Vector3(eyePosition).add(direction), up);
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	public void move3D(int dx, int dy) {	
		if(dx == 0 && dy == 0) return;
		
		yawAccum -= dx;
		pitchAccum -= dy;
		
		if(yawAccum < -360.0) yawAccum += 360.0f;
		if(yawAccum >  360.0) yawAccum -= 360.0f;
		
		float la = 89.9f;
		if(pitchAccum >  la) pitchAccum =  la;
		if(pitchAccum < -la) pitchAccum = -la;
		
		currentRotation.setEuler(yawAccum, pitchAccum, 0.0f);
		
		direction.set(0.0f, 0.0f, 1.0f);
		currentRotation.transform(direction);
		viewDirty = true;
	}

	public int getViewportX() {
		return viewportX;
	}

	public void setViewportX(int viewportX) {
		this.viewportX = viewportX;
	}

	public int getViewportY() {
		return viewportY;
	}

	public void setViewportY(int viewportY) {
		this.viewportY = viewportY;
	}

	public void setPosition(Vector3 eyePosition) {
		this.eyePosition.set(eyePosition);
		viewDirty = true;
	}

	public Vector3 getPosition() {
		return eyePosition;
	}

	public Quaternion getRotation() {
		return currentRotation;
	}

	public Vector3 getDirection() {
		return direction;
	}

	public void setDirection(Vector3 direction) {
		this.direction.set(direction);
		viewDirty = true;
	}

	public float getFrustumNear() {
		return frustumNear;
	}

	public void setFrustumNear(float frustumNear) {
		this.frustumNear = frustumNear;
		projectionDirty = true;
	}

	public float getFrustumFar() {
		return frustumFar;
	}

	public void setFrustumFar(float frustumFar) {
		this.frustumFar = frustumFar;
		projectionDirty = true;
	}

	public void unproject(Vector3 vWinSpace) {
		float x = vWinSpace.x, y = vWinSpace.y;
		int viewportX = 0, viewportY = 0;
		x = x - viewportX;
		y = getHeight() - y - 1;
		y = y - viewportY;
		vWinSpace.x = (2 * x) / getWidth() - 1;
		vWinSpace.y = (2 * y) / getHeight() - 1;
		vWinSpace.z = 2 * vWinSpace.z - 1;
		
		invProjectionView.set(projection).mul(getView()).inv();
		vWinSpace.project(invProjectionView);
	}
	
	public void lookAt(Vector3 eye, Vector3 target, Vector3 up) {
		eyePosition.set(eye);
		direction.set(target).sub(eye).normalize();
		up.set(up);
		
		projectionDirty = true;
		viewDirty = true;
	}

	@Override
	public String toString() {
		return String.format("P: %s, D: %s, U: %s, x:%d y:%d, w:%d, h:%d, near:%.4f, far:%.4f",
				eyePosition, direction, up, viewportX, viewportY,
				width, height, frustumNear, frustumFar);
	}
}