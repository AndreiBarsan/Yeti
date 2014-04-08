package barsan.opengl.util;

import java.awt.Font;
import java.util.ArrayList;

import barsan.opengl.Yeti;
import barsan.opengl.input.GlobalConsole;

public class ConsoleRenderer {

	private GlobalConsole console;
	private Font consoleFont;
	private int consoleRows = 15;
	private int rh = 20;
	private int topBorder = 15;
	
	public ConsoleRenderer(GlobalConsole gci) {
		this.console = gci;
		consoleFont = new Font(Font.MONOSPACED, Font.PLAIN, 16);
	}

	public void render() {
		if(console.isEnabled()) {
			String prompt = "> ";
			TextHelper.setFont(consoleFont);
			Settings ys = Yeti.get().settings;
			TextHelper.beginRendering(ys.width, ys.height);
			{
				TextHelper.drawText(0, ys.height - rh - 10, console.getTitle());
				ArrayList<String> history = console.getHistory();
				
				int begin = Math.max(0, history.size() - consoleRows);
				for(int i = begin; i < history.size(); i++) {
					TextHelper.drawText(0,
							ys.height - rh * 2 - topBorder - ( (i - begin) * rh),
							history.get(i));
				}
				
				String cmd = console.getCurrentCommand(); 
				TextHelper.drawText(0, ys.height - rh * (consoleRows + 2) - topBorder, prompt + cmd);
			}
			TextHelper.endRendering();
		}
	}
}
