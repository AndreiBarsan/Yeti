package barsan.opengl.scenes;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.flat.Sprite;
import barsan.opengl.input.CameraInput;
import barsan.opengl.input.InputAdapter;
import barsan.opengl.math.Transform;
import barsan.opengl.rendering.Renderer;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.StaticModelInstance;
import barsan.opengl.rendering.materials.BasicMaterial;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.SceneHelper;
import barsan.opengl.util.TextHelper;

public class MenuScene extends Scene {

	protected CameraInput cameraInput;

	public interface MenuAction {
		public void performAction();
	}
	
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
	
	public static class DummyAction implements MenuAction {
		public void performAction() { }
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
		private FontRenderContext context = new FontRenderContext(new AffineTransform(), true, false);
		private int index = 0;
		
		public class MenuEntry {
			private String text;
			private MenuAction action;
			private boolean selected;
			private boolean centered = true;
			private float widthCache = -1.0f;
			
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
				widthCache = (float) font.getStringBounds(text, context).getWidth();
			}
			
			public void draw(int x, int y) {
				if(centered) {
					x -= widthCache / 2;
				}
				TextHelper.drawText(x, y, text, selected ? Color.YELLOW : Color.WHITE, 2);
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
			int step = 42;
			
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
	
	private Menu menu = new Menu();
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		
		SceneHelper.quickSetup2D(this);
		ResourceLoader.loadTexture("background", "menuBackground.png");
		ResourceLoader.loadObj("sphere", "sphere.obj");
		//*
		addModelInstance(new StaticModelInstance(ResourceLoader.model("sphere"), 
				new BasicMaterial(barsan.opengl.util.Color.RED),
				new Transform().updateScale(1.0f)
		));
		//*/
		
		
		Sprite s;
		addBillboard(s = new Sprite(Yeti.get().gl, ResourceLoader.texture("background")));
		
		
		menu.addEntry(menu.new MenuEntry("Begin!", new TransitionAction(new GameScene())));
		menu.addEntry(menu.new MenuEntry("Light test", new TransitionAction(new LightTest())));
		menu.addEntry(menu.new MenuEntry("About", new DummyAction()));
		menu.addEntry(menu.new MenuEntry("Exit", new ExitAction()));
		
		Renderer.renderDebug = false;
		
		addInput(new InputAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
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
		
		GL2 gl = Yeti.get().gl;
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		super.display(drawable);
		
		TextHelper.beginRendering(camera.getWidth(), camera.getHeight());
		{
			menu.draw();
		}
		TextHelper.endRendering();
	}
}
