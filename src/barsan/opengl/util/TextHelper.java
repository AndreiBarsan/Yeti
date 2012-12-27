package barsan.opengl.util;

import java.awt.Font;

import com.jogamp.opengl.util.awt.TextRenderer;

public class TextHelper {

	static TextRenderer ren = new TextRenderer(new Font("serif", Font.PLAIN, 16));
	
	public static void drawText(int x, int y, String text) {
		ren.setColor(new java.awt.Color(0.0f, 0.0f, 0.0f));
		ren.draw(text, x + 1, y - 1);
		
		ren.setColor(new java.awt.Color(1.0f, 0.4f, 0.5f));
		ren.draw(text, x, y);
	}
	
	public static void setFont(Font font) {
		ren = new TextRenderer(font);
	}
	
	public static void drawTextMultiLine(int x, int y, String text) {
		String[] lines = text.split("\n");
		int height = (int) ren.getBounds(text).getHeight();
		for(int i = 0; i < lines.length; i++) {
			drawText(x, (lines.length - i) * height, lines[i]);
		}
	}
	
	public static void beginRendering(int width, int height) {
		ren.beginRendering(width, height);
	}
	
	public static void endRendering() {
		ren.endRendering();
	}
}
