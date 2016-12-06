package barsan.opengl.platform;

import java.awt.Component;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLCanvas;

public class AWTFactory implements CanvasFactory {

	@Override
	public Component createCanvas(GLCapabilities capabilities) {
		return new GLCanvas(capabilities);
	}

}
