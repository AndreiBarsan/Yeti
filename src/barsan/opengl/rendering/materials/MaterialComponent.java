package barsan.opengl.rendering.materials;

import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.RendererState;

/* pp */ interface MaterialComponent {
	/**
	 * Sets up this material component. Multiple components make up a whole 
	 * material. This method doesn't handle textures - use setupTexture for that.
	 * The reason is because texture binding works with a limited number of
	 * slots, and we need to keep track of it.
	 *  
	 * @param m		The material being set up. 
	 * @param rs	The rendering context.
	 * @param modelMatrix	The current modelmatrix.
	 * 
	 * TODO: consider refactoring this; maybe just pass a modelinstance (this
	 * way access to the object's buffers might also get cleaner)
	 */
	/* pp */ void setup(Material m, RendererState rs, Matrix4 modelMatrix);
	/**
	 * Fills up 0 or more texture slots and binds other related variables.
	 * @return The number of texture slots occupied.
	 */
	/* pp */ int setupTexture(Material m, RendererState rs, int slot);
	/**
	 * Frees up whatever resources were bound on setup! 
	 * Do not destroy texutures and such here! Use dispose() for that!
	 */
	/* pp */ void cleanUp(Material m, RendererState rs);
	
	/* pp */ void dispose();
}