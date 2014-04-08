package barsan.opengl.scenes;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.flat.Sprite;
import barsan.opengl.input.CameraInput;
import barsan.opengl.input.InputAdapter;
import barsan.opengl.math.MathUtil;
import barsan.opengl.math.Vector2;
import barsan.opengl.rendering.Renderer;
import barsan.opengl.rendering.Scene;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.SceneHelper;
import barsan.opengl.util.TextHelper;

public class MenuScene extends Scene {

	protected CameraInput cameraInput;

	private Menu menu = new Menu();
	private Sprite logo;
	Font authorFont = new Font("serif", Font.PLAIN, 24);
	
	float start = 500.0f;
	float end = 150.0f;
	float initialDelay = 1.0f;
	float time = 1.2f;
	float a = 0.0f;
	Sprite background;	

	/** Something that happens when you select a menu item. */
	public interface MenuAction {
		public void performAction();
	}
	
	/** A specific action that simply takes you to another scene when invoked */
	public static class TransitionAction implements MenuAction {
		private Scene target;
		
		public TransitionAction(Scene target) {
			this.target = target;
		}
		
		@Override
		public void performAction() {
			Yeti.get().loadScene(target);
		}
	}
	
	public static class ExitAction implements MenuAction {
		@Override
		public void performAction() {
			Yeti.quit();
		}
	}
	
	public static class Menu {
		private List<MenuEntry> entries = new ArrayList<>();
		private Font font = new Font("serif", Font.BOLD, 48);
		private int index = 0;

		public class MenuEntry {
			private String text;
			private MenuAction action;
			private boolean selected;
			private boolean centered = true;
			
			public MenuEntry(String text, MenuAction action) {
				this.setText(text);
				this.action = action;
			}
			
			public void activate() {
				action.performAction();
			}
			
			public String getText() {
				return text;
			}
			
			public void setText(String text) {
				this.text = text;
			}
			
			public void draw(int x, int y) {
				if(centered) {
					TextHelper.drawTextCentered(x, y, text, selected ? Color.YELLOW : Color.WHITE, 2);
				}
				else {
					TextHelper.drawText(x, y, text, selected ? Color.YELLOW : Color.WHITE, 2);
				}
			}
		}
		
		public void addEntry(MenuEntry entry) {
			entries.add(entry);
			entries.get(0).selected = true;
		}
		
		public void draw() {
			TextHelper.setFont(font);
			
			int x = Yeti.get().settings.width / 2;
			int y = 320;
			int step = 50;
			
			for(int i = 0; i < entries.size(); i++) {
				entries.get(i).draw(x, y - i * step);
			}
		}
		
		public void goUp() {
			entries.get(index).selected = false;
			
			index--;
			if(index < 0) index = entries.size() - 1;
			
			entries.get(index).selected = true;
		}
		
		public void goDown() {
			entries.get(index).selected = false;
			
			index++;
			if(index >= entries.size()) index = 0;
			
			entries.get(index).selected = true;
		}
		
		public void activate() {
			entries.get(index).activate();
		}
		
		public void mouseUpdated(int newX, int newY) {
			// TODO: when mouse moved, check every entry's bounding rectangle
		}
	}
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		
		SceneHelper.quickSetup2D(this);
		ResourceLoader.loadTexture("background", "menuBackground.png");
		ResourceLoader.loadTexture("logo", "logo.png");
	
		addBillboard(background = new Sprite(Yeti.get().gl, ResourceLoader.texture("background"), "background"), 0);
		addBillboard(logo = new Sprite(Yeti.get().gl, ResourceLoader.texture("logo"), "logo"), 200);	
		
		menu.addEntry(menu.new MenuEntry("Begin!", new TransitionAction(new GameScene())));
		menu.addEntry(menu.new MenuEntry("Light test", new TransitionAction(new LightTest())));
		//menu.addEntry(menu.new MenuEntry("About", new DummyAction()));
		menu.addEntry(menu.new MenuEntry("Exit", new ExitAction()));
		
		Renderer.renderDebug = false;
		
		addInput(new InputAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.isConsumed()) {
					return;
				}
				
				if(e.getKeyCode() == KeyEvent.VK_UP) {
					menu.goUp();
				} else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
					menu.goDown();
				} else if(e.getKeyCode() == KeyEvent.VK_SPACE
						|| e.getKeyCode() == KeyEvent.VK_ENTER) {
					menu.activate();
				}
			}
		});
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		// Ideally, using a designated 2D text & sprite renderer would be the best idea.
		if(exiting) {
			exit();
			return;
		}
				
		renderer.setSortBillboards(false);
		float delta = Yeti.get().getDelta();
		float logoY = start;
		
		if(initialDelay > 0.0f) {
			initialDelay -= delta;
		} else {
			a += delta;
			if(a <= time) {
				logoY = MathUtil.exp(start, end, a / time);
			} else {
				logoY = end;
			}
		}
		logo.setPosition(new Vector2(0.0f, logoY));
		
		GL2 gl = Yeti.get().gl;
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		super.display(drawable);
		
		TextHelper.beginRendering(camera.getWidth(), camera.getHeight());
		{
			menu.draw();
			TextHelper.setFont(authorFont);
			TextHelper.drawTextCentered(Yeti.get().settings.width / 2, 15, "Andrei Bârsan, Universitatea Transilvania din Brașov, 2012 - 2014");
		}
		TextHelper.endRendering();
	}
}
