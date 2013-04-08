package barsan.opengl.tools;

import java.awt.Frame;

import barsan.opengl.Yeti;
import barsan.opengl.platform.AWTFactory;
import barsan.opengl.scenes.NessieTestScene;

public class DeferredTest {
	public static void main(String[] args) {
		Yeti yeti = Yeti.get();
		Frame frame = new Frame("Nessie test");
		yeti.settings.width = 1280;
		yeti.settings.height = 720;
		frame.setResizable(false);
		yeti.setDefaultScene(new NessieTestScene());
		yeti.startApplicationLoop(null, frame, frame, new AWTFactory());
	}
}
