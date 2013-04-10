package barsan.opengl.util;

import barsan.opengl.math.Vector2;

public abstract class GUI {
	
	protected Vector2 position;

	public GUI() {
		this(new Vector2());
	}
	
	public GUI(Vector2 position) {
		this.position = position;
	}
	
	public abstract void render();
	
	public Vector2 getPosition() {
		return position;
	}

	public void setPosition(Vector2 position) {
		this.position = position;
	}

	public void setPosition(float x, float y) {
		this.position.set(x, y);
	}
}
