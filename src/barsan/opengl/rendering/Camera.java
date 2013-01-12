package barsan.opengl.rendering;

import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Quaternion;
import barsan.opengl.math.Vector3;

/**
 * @author Andrei Barsan
 */
public class Camera {
	
	static final float DEFAULT_NEAR =   0.25f;
	static final float DEFAULT_FAR 	= 1000.00f;
	static final float DEFAULT_FOV = 	45.0f;
	
	private Vector3 eyePosition, up;
	private Vector3 direction;
	private int width, height;	
	private int viewportX, viewportY;
	private float FOV; /* Degrees */

	private float speed = 0.9f;
	private float rotSpeed = 6.0f; /* Degrees */
		
	private Matrix4 projection;

	private float frustumNear;
	private float frustumFar;	
	
	// Holds the view matrix of the camera, auto-updates when requested
	// TODO: update only on demand!
	private Matrix4 vm = new Matrix4();
	static Matrix4 aux_matrix = new Matrix4();
	
	Quaternion currentRotation = new Quaternion();
	
	public Camera(int width, int height) {
		this(new Vector3(-4, 2, 0), new Vector3(1, -0.5f, 0).normalize(), width, height);
	}
	
	public Camera(Vector3 position, Vector3 direction, int width, int height) {
		this.direction = direction;
		this.eyePosition = position;
		this.width = width;
		this.height = height;
		
		frustumNear = DEFAULT_NEAR;
		frustumFar = DEFAULT_FAR;
		
		up = new Vector3(0, 1, 0);
		FOV = DEFAULT_FOV;
		
		refreshProjection();
		currentRotation = new Quaternion(direction, 0);
	}
	
	public void forward() {
		Vector3 d = new Vector3(direction).mul(-speed);
		eyePosition.add(d);
	}
	
	public void backward() {
		Vector3 d = new Vector3(direction).mul(speed);
		eyePosition.add(d);
	}
	
	public void strafeLeft() {
		Vector3 d = new Vector3(up).cross((new Vector3(direction).mul(speed)));
		eyePosition.add(d);
	}
	
	public void strafeRight() {
		Vector3 d = new Vector3(direction).cross(new Vector3(up)).mul(speed);
		eyePosition.add(d);
	}
	
	public void strafeDown() {
		eyePosition.add(new Vector3(up).mul(-speed));
	}
	
	public void strafeUp() {
		eyePosition.add(new Vector3(up).mul(speed));
	}
	
	public void turnLeft() {
		direction.mul(aux_matrix.setRotate(-rotSpeed, up.x, up.y, up.z));
		
	}
	
	public void turnRight() {
		direction.mul(aux_matrix.setRotate(rotSpeed, up.x, up.y, up.z));
	}
	
	public void turnUp() {
		Vector3 right = new Vector3(direction).cross(up).normalize();
		direction.mul(aux_matrix.setRotate(rotSpeed, right.x, right.y, right.z));
	}
	
	public void turnDown() {
		Vector3 left = new Vector3(up).cross(direction).normalize();
		direction.mul(aux_matrix.setRotate(rotSpeed, left.x, left.y, left.z));
	}
	
	public Matrix4 getView() {
		return vm.setLookAt(eyePosition, new Vector3(eyePosition).add(direction), up);
	}

	public void reshape(int x, int y, int width, int height) {
		this.width = width;
		this.height = height;
		this.viewportX = x;
		this.viewportY = y;
		refreshProjection();
	}
	
	public Matrix4 getProjection() {
		return projection;
	}
	
	private void refreshProjection() {
		assert(height != 0);
		assert(FOV > 5);
		assert(frustumNear < frustumFar);
		
		float aspect = (float)width / height;
		
		projection = new Matrix4().setFrustum(FOV, aspect, frustumNear, frustumFar);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public float getFrustumNear() {
		return frustumNear;
	}

	public void setFrustumNear(float frustumNear) {
		this.frustumNear = frustumNear;
		refreshProjection();
	}

	public float getFrustumFar() {
		return frustumFar;
	}

	public void setFrustumFar(float frustumFar) {
		this.frustumFar = frustumFar;
		refreshProjection();
	}
	
	public void setFOV(float FOV) {
		this.FOV = FOV;
	}
	
	public float getFOV(float FOV) {
		return FOV;
	}
	
	static Quaternion pq = new Quaternion(), yq = new Quaternion();
	static Vector3 aux_v3 = new Vector3();
	
	float yawAccum = 0.0f;
	float pitchAccum = 0.0f;
	
	public void move3D(int dx, int dy) {		
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

		//currentRotation.mul(yq);
	}
	
	static final private Matrix4 invProjectionView = new Matrix4(); 
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
		this.eyePosition = eyePosition;		
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
		this.direction = direction;
	}


}
