package barsan.opengl.platform;

import java.awt.Component;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.awt.GLCanvas;

public class AWTFactory implements CanvasFactory {

	@Override
	public Component createCanvas(GLCapabilities capabilities) {
		return new GLCanvas(capabilities);
	}

}
