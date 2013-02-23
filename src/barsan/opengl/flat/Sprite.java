package barsan.opengl.flat;

import javax.media.opengl.GL2;

import barsan.opengl.Yeti;
import barsan.opengl.math.Vector2;
import barsan.opengl.rendering.Billboard;

import com.jogamp.opengl.util.texture.Texture;

/**
 * Incredibly primitive (OOGA-BOOGA-Tier) sprite class. Doesn't support batching
 * or any fancy stuff.
 *  
 * @author Andrei Bârsan
 */
public class Sprite extends Billboard {
	
	public Sprite(GL2 gl, Texture texture) {
		super(gl, texture);
		getTransform().updateScale(texture.getHeight());
		setAxisClamp(AxisClamp.ClampAll);
	}
	
	public void setPosition(Vector2 position) {
		//float w = Yeti.get().settings.width;
		//float h = Yeti.get().settings.height;
		//localTransform.updateTranslate(position.x / w, position.y / h, 0.0f);
		
		//System.out.println(localTransform.getTranslate());
		localTransform.updateTranslate(position.x, position.y, 0.0f);
	}

}
