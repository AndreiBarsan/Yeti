package barsan.opengl.planetHeads;

import java.awt.Frame;

import barsan.opengl.Yeti;
import barsan.opengl.platform.AWTFactory;

public class Game {
	public static void main(String[] args) {
		Yeti yeti = Yeti.get();
		Frame frame = new Frame("PlanetHeads");
		frame.setResizable(false);
		frame.setSize(yeti.settings.width, yeti.settings.height);
		yeti.settings.lastSceneIndex = 4;
		yeti.settings.width = 1024;
		yeti.settings.height = 748;
		yeti.startApplicationLoop(null, frame, frame, new AWTFactory());
	}
}
