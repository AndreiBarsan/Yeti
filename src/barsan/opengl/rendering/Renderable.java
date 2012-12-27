package barsan.opengl.rendering;

import barsan.opengl.math.Matrix4Stack;

/**
 * Contract: whoever implements this interface should be able to draw itself
 * using the provided renderer state. The renderer state includes a list of
 * all lights in the scene, the camera information, the GL context, etc.
 * 
 * @author SiegeDog
 *
 */
public interface Renderable {
	void render(RendererState rendererState, Matrix4Stack transformStack);
}
