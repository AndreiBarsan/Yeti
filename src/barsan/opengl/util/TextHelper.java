package barsan.opengl.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import barsan.opengl.Yeti;

import com.jogamp.opengl.util.awt.TextRenderer;

public class TextHelper {

	private static TextRenderer ren = new TextRenderer(new Font("sans-serif", Font.PLAIN, 20));
	private static FontRenderContext context = new FontRenderContext(new AffineTransform(), true, false);
	private static Map<String, Rectangle2D> boundsCache = new HashMap<>();
	
	public static void drawText(int x, int y, String text, Color color) {
		drawText(x, y, text, color, 1);
	}
	public static void drawText(int x, int y, String text, Color color, int outline) {
		ren.setColor(Color.BLACK);
		ren.draw(text, x + outline, y - outline);
		ren.setColor(color);
		ren.draw(text, x, y);
		ren.flush();
	}
		
	public static void drawText(int x, int y, String text) {
		drawText(x, y, text, Color.WHITE);
	}
	
	public static void drawTextCentered(int x, int y, String text) {
		drawTextCentered(x, y, text, Color.WHITE, 1);
	}
	
	public static void drawTextCentered(int x, int y, String text, Color color) {
		drawTextCentered(x, y, text, color, 1);
	}
		
	public static void drawTextCentered(int x, int y, String text, Color color, int outline) {
		double w;
		if(boundsCache.containsKey(text)) {
			w = boundsCache.get(text).getWidth();
		} else {
			Rectangle2D bounds = ren.getFont().getStringBounds(text, context);
			boundsCache.put(text, bounds);
			w = bounds.getWidth();
		}
		drawText(x - (int)(w / 2), y, text, color, outline);
	}
	
	public static void setFont(Font font) {
		ren = new TextRenderer(font);
	}
	
	public static void drawTextMultiLine(int x, int y, String text, Color color) {
		String[] lines = text.split("\n");
		int height = (int) ren.getBounds(text).getHeight();
		for(int i = 0; i < lines.length; i++) {
			drawText(x, y + (lines.length - i) * height, lines[i], color);
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
