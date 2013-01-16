package barsan.opengl.scenes;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Cylinder;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.materials.BasicMaterial;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;
import barsan.opengl.util.TextHelper;

public class ProceduralScene extends Scene {

	ModelInstance cylinder;
	PointLight mainLight;
	int precision = 6;
	boolean dirty = false;
	boolean smoothRendering = true;
	float diameter = 2.0f, height = 2f;
	final int MAX_PRECISION = 100;
	final int MIN_PRECISION = 3;
	long start = System.currentTimeMillis();
	
	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
		
		GL2 gl = Yeti.get().gl;
		
		cylinder = new ModelInstance(new Cylinder(gl, precision, diameter, height));
		modelInstances.add(cylinder);
		
		camera.setPosition(new Vector3(0.0f, 0.25f, -4.0f));
		camera.setDirection(new Vector3(0.0f, 0.0f, -1.0f));
		pointLights.add(mainLight = new PointLight(new Vector3(0f, 20f, 10f), new Color(0.9f, 0.9f, 0.9f, 1.0f)));
		
		Yeti.debug("J & K to increase/decrease cylinder precision.");
		Yeti.get().addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) { }
			public void keyPressed(KeyEvent e) { }
			
			public void keyReleased(KeyEvent e) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_J:
					if(precision > MIN_PRECISION) {
						precision--;
						dirty = true;
					}
				break;
				
				case KeyEvent.VK_K:
					if(precision < MAX_PRECISION) {
						precision++;
						dirty = true;
					}
				break;
				
				case KeyEvent.VK_M:
					smoothRendering = !smoothRendering;
					break;
				}
			}
		});
		
		cylinder.getMaterial().setDiffuse(new Color(1.0f, 1.0f, 0.1f));
		TextHelper.setFont(new Font("serif", Font.BOLD, 24));
	}
	
	void drawGUI(GLAutoDrawable drawable) {
		Yeti.get().gl.glUseProgram(0);
		float fps = drawable.getAnimator().getLastFPS();
		
		TextHelper.beginRendering(camera.getWidth(), camera.getHeight());
		{
			Vector3 cp = camera.getPosition();
			String hud = String.format("FPS: %.2f\nCamera: X:%.2f Y:%.2f Z:%.2f\nDir: %s", fps,
					cp.x, cp.y, cp.z, camera.getDirection());
			
			TextHelper.drawTextMultiLine(20, 20, hud);
		}
		TextHelper.endRendering();		
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = Yeti.get().gl;
		long time = System.currentTimeMillis() - start;
		float factor = (float)time / 400.0f;
		mainLight.setPosition(new Vector3(
				15.0f * (float)Math.cos(factor),
				8.0f, 
				15.0f * (-1) * (float)Math.sin(factor)));
		
		//((BasicMaterial)cylinder.getMaterial()).setSmooth(smoothRendering);
		
		if(dirty) {
			cylinder.getModel().dispose();
			cylinder.setModel(new Cylinder(gl, precision, diameter, height));
			dirty = false;
		}
		super.display(drawable);
		
		drawGUI(drawable);
	}
}
