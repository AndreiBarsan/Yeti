package barsan.opengl.flat;

import barsan.opengl.math.Vector2;
import barsan.opengl.resources.ResourceLoader;

public class Player extends Entity2D {

	public Player(Vector2 position) {
		super(position, ResourceLoader.model("planetHead"));
	}

}
