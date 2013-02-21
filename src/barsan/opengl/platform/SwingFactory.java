package barsan.opengl.platform;

import java.awt.Component;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.awt.GLJPanel;

public class SwingFactory implements CanvasFactory {

	@Override
	public Component createCanvas(GLCapabilities capabilities) {
		return new GLJPanel(capabilities);
	}
	
}
