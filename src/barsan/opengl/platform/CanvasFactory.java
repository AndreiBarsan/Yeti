package barsan.opengl.platform;

import java.awt.Component;

import com.jogamp.opengl.GLCapabilities;

public interface CanvasFactory {
	public Component createCanvas(GLCapabilities capabilities);
}
