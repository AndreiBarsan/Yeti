package barsan.opengl.scenes;

import java.awt.Font;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3bc;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;

import barsan.opengl.Yeti;
import barsan.opengl.rendering.Scene;
import barsan.opengl.util.TextHelper;

public class TextScene extends Scene{

	@Override
	public void init(GLAutoDrawable drawable) {
		super.init(drawable);
	}
	
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		GL3bc gl = Yeti.get().gl;
		GLU glu = new GLU();

		if (height == 0) {
			height = 1;
		}
		
		float aspect = (float) width / (float) height;

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		
		glu.gluPerspective(45.0f, aspect, 0.1f, 100.0f);

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
		
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = Yeti.get().gl.getGL2();
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		gl.glPushMatrix();
		gl.glTranslatef(0.0f, 0.0f, 1.0f);
		
		gl.glColor3f(1.0f, 0.5f, 0.33f);
			gl.glBegin(GL2.GL_TRIANGLES);
			gl.glVertex3f(0.0f, 0.0f, -1.0f);
			gl.glVertex3f(0.33f, 0.0f, -1.0f);
			gl.glVertex3f(0.0f, 0.5f, -1.0f);
		gl.glEnd();
		
		gl.glPopMatrix();
		
		gl.glUseProgram(0);
		TextHelper.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
		TextHelper.beginRendering(drawable.getWidth(), drawable.getHeight());
			TextHelper.drawText(20, 20, "Testing hud text!");
		TextHelper.endRendering();
		
	}
	
}
