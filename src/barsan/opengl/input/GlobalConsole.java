package barsan.opengl.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import barsan.opengl.Yeti;

public class GlobalConsole implements KeyListener, InputProvider {

	Yeti y;
	
	private boolean consoleEnabled = false;
	String consoleInput = "";
	ArrayList<String> history = new ArrayList<>();
	
	public GlobalConsole() {
		y = Yeti.get();
	}
	
	@Override
	public void keyTyped(KeyEvent e) { }
	
	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_F10:
				Yeti.warn("Fullscreen toggle disabled.");
				break;
				
			case KeyEvent.VK_ENTER:
				if(consoleEnabled) {
					if( ! consoleInput.equals("")) {
						String result = Yeti.get().executeCommand(consoleInput);
						history.add(consoleInput);
						consoleInput = "";
						for(String line : result.split("\n")) {
							history.add(line);
						}
					}
				}
				break;
			
			case KeyEvent.VK_BACK_QUOTE:
				consoleEnabled = ! consoleEnabled;
				break;
				
			case KeyEvent.VK_ESCAPE:
				if(consoleEnabled) {
					if(consoleInput.length() == 0) {
						consoleEnabled = false;
					}
					else {
						consoleInput = "";
					}
				}
				else {
					if(y.settings.playing) {
						y.pause();
					}
				}
				break;
				
				default:
				if(consoleEnabled) {
					char ch = e.getKeyChar();
					if(Character.isJavaIdentifierPart(ch) ||
							".,- +/*".contains(String.valueOf(ch))) {
						consoleInput += ch;
					}
					
					if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
						if(consoleInput.length() > 1) {
							consoleInput = consoleInput.substring(0, consoleInput.length() - 2);
						}
					}
					
					e.consume();
				}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) { }
	
	public boolean isEnabled() {
		return consoleEnabled;
	}
	
	public String getCurrentCommand() {
		return consoleInput.toString();
	}
	
	public ArrayList<String> getHistory() {
		return history;
	}
	
	public String getTitle() {
		return "Yeti console (press ~ to close)";
	}
}
