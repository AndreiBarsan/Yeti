package barsan.opengl.util;

import java.awt.Color;
import java.awt.Font;

import barsan.opengl.Yeti;

import com.jogamp.opengl.util.awt.TextRenderer;

public class TextHelper {

	static TextRenderer ren = new TextRenderer(new Font("sans-serif", Font.PLAIN, 20));
	
	public static void drawText(int x, int y, String text, Color color) {
		ren.setColor(Color.BLACK);
		ren.draw(text, x + 1, y - 1);		
		ren.setColor(color);
		ren.draw(text, x, y);
	}
		
	public static void drawText(int x, int y, String text) {
		drawText(x, y, text, Color.WHITE);
	}
	
	public static void setFont(Font font) {
		ren = new TextRenderer(font);
	}
	
	public static void drawTextMultiLine(int x, int y, String text, Color color) {
		String[] lines = text.split("\n");
		int height = (int) ren.getBounds(text).getHeight();
		for(int i = 0; i < lines.length; i++) {
			drawText(x, (lines.length - i) * height, lines[i], color);
		}
	}
	
	public static void drawTextMultiLine(int x, int y, String text) {
		drawTextMultiLine(x, y, text, Color.WHITE);
	}
	
	public static void beginRendering(int width, int height) {
		Yeti.get().gl.glUseProgram(0);
		ren.beginRendering(width, height);
	}
	
	public static void endRendering() {
		ren.endRendering();
	}
}
