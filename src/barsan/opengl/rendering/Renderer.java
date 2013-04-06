package barsan.opengl.rendering;

import javax.media.opengl.GL3;

public interface Renderer {

	public abstract RendererState getState();

	public abstract void render(Scene scene);

	public abstract void dispose(GL3 gl);

}