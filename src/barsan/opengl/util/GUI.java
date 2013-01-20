package barsan.opengl.util;

import barsan.opengl.math.Vector3;

public abstract class GUI {
	protected Vector3 position;

	public GUI() {
		this(new Vector3());
	}
	
	public GUI(Vector3 position) {
		this.position = position;
	}
	
	public abstract void render();
	
	public Vector3 getPosition() {
		return position;
	}

	public void setPosition(Vector3 position) {
		this.position = position;
	}

}
