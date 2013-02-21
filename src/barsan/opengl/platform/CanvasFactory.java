package barsan.opengl.platform;

import java.awt.Component;

import javax.media.opengl.GLCapabilities;

public interface CanvasFactory {
	public Component createCanvas(GLCapabilities capabilities);
}
