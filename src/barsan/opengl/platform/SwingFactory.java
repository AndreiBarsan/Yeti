package barsan.opengl.platform;

import java.awt.Component;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLJPanel;

public class SwingFactory implements CanvasFactory {

	@Override
	public Component createCanvas(GLCapabilities capabilities) {
		return new GLJPanel(capabilities);
	}
	
}
