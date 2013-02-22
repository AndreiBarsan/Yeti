package barsan.opengl.planetHeads;

import java.awt.Frame;

import barsan.opengl.Yeti;
import barsan.opengl.platform.AWTFactory;
import barsan.opengl.scenes.MenuScene;

public class Game {
	public static void main(String[] args) {
		Yeti yeti = Yeti.get();
		Frame frame = new Frame("PlanetHeads");
		frame.setSize(yeti.settings.width, yeti.settings.height);
		
		yeti.startApplicationLoop(null, frame, frame, new AWTFactory());
		yeti.settings.lastSceneIndex = 2;
	}
}
