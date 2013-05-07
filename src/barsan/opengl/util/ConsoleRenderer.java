package barsan.opengl.util;

import java.awt.Font;
import java.util.ArrayList;

import barsan.opengl.Yeti;
import barsan.opengl.input.GlobalConsole;

public class ConsoleRenderer {

	private GlobalConsole gci;
	private Font consoleFont;
	private int consoleRows = 15;
	private int rh = 20;
	private int topBorder = 15;
	
	public ConsoleRenderer(GlobalConsole gci) {
		this.gci = gci;
		
		consoleFont = new Font(Font.MONOSPACED, Font.PLAIN, 16);
	}

	public void render() {
		if(gci.isEnabled()) {
			String prompt = "> ";
			TextHelper.setFont(consoleFont);
			Settings ys = Yeti.get().settings;
			TextHelper.beginRendering(ys.width, ys.height);
			{
				TextHelper.drawTextMultiLine(0, ys.height - rh - 10, "Yeti console (press ESC to close)");
				ArrayList<String> history = gci.getHistory();
				
				int begin = Math.max(0, history.size() - consoleRows);
				for(int i = begin; i < history.size(); i++) {
					TextHelper.drawText(0, ys.height - rh - topBorder
							- rh * consoleRows +
							
							(consoleRows - (i - begin)) * rh
							, history.get(i));
				}
				
				String cmd = gci.getCurrentCommand(); 
				TextHelper.drawTextMultiLine(0, ys.height - rh * consoleRows - rh - topBorder, prompt + cmd);
			}
			TextHelper.endRendering();
		}
	}
}
