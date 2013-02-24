package barsan.opengl.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import barsan.opengl.Yeti;

public class GlobalConsoleInput implements KeyListener, InputProvider {

	Yeti y;
	
	public GlobalConsoleInput() {
		y = Yeti.get();
	}
	
	@Override
	public void keyTyped(KeyEvent e) { }

	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_F10:
				y.toggleFullscreen();
				break;
			
			case KeyEvent.VK_ESCAPE:
				if(y.settings.playing) {
					y.pause();
				}
				break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) { }

}
