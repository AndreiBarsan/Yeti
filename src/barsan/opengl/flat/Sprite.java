package barsan.opengl.flat;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import barsan.opengl.math.Vector2;
import barsan.opengl.rendering.Billboard;

import com.jogamp.opengl.util.texture.Texture;

/**
 * Incredibly primitive (OOGA-BOOGA-Tier) sprite class. Doesn't support batching
 * or any fancy stuff.
 *  
 * @author Andrei B�rsan
 */
public class Sprite extends Billboard {
	
	public Sprite(GL gl, Texture texture, String name) {
		this(gl, texture, name, true);
	}
	
	/**
	 * Initializes the 2D sprite living in a 3D space.
	 * @param gl		The OpenGL context.
	 * @param texture	The sprite's actual texture.
	 * @param name		The sprite's name - useful for debugging.
	 * @param flipAroundY	Whether to flip the sprite 180 around the Y axis. Defaults
	 * to true because of SceneHelper sets up 2D cameras in such a way that larger
	 * Z values are on top. 
	 */
	public Sprite(GL gl, Texture texture, String name, boolean flipAroundY) {
		super(gl, texture, name);
		getTransform().updateScale(texture.getHeight());
		setAxisClamp(AxisClamp.ClampAll);
		
		if(flipAroundY) {
			localTransform.updateRotation(0.0f, 1.0f, 0.0f, 180.0f);
		}
	}
	
	public void setPosition(Vector2 position) {
		localTransform.updateTranslate(position.x, position.y, localTransform.getTranslate().z);
	}

}
