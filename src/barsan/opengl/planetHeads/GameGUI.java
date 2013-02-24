package barsan.opengl.planetHeads;

import java.awt.Font;

import barsan.opengl.Yeti;
import barsan.opengl.flat.Player;
import barsan.opengl.util.GUI;
import barsan.opengl.util.TextHelper;

public class GameGUI extends GUI {

	private Font guiFont = new Font("sans-serif", Font.PLAIN, 30);
	private int width, height;
	private Player player;
	
	public GameGUI(Player player) {
		width = Yeti.get().settings.width;
		height = Yeti.get().settings.height;
		this.player = player;
	}
	
	@Override
	public void render() {
		TextHelper.setFont(guiFont);
		Yeti.get().gl.glUseProgram(0);
		TextHelper.beginRendering(width, height);
		{
			String hud = String.format("Score: %d\n", player.score);
			TextHelper.drawTextMultiLine((int)position.x, (int)position.y, hud);
		}
		TextHelper.endRendering();
	}

}
