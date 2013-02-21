package barsan.opengl.input;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.SwingUtilities;

import barsan.opengl.Yeti;
import barsan.opengl.math.MathUtil;
import barsan.opengl.rendering.Camera;

public class CameraInput implements KeyListener, MouseListener, MouseMotionListener {
	
	private Camera camera;
	
	/** Used to keep the mouse in place when looking around. */ 
	private Robot robot;
	
	private boolean mouseControlled = true;
	
	void showHelp() {
		Yeti.debug("============================================");
		Yeti.debug("==        Initialized camera input.       ==");
		Yeti.debug("== Use WASD and the mouse to move around. ==");
		Yeti.debug("============================================");
	}
	
	public CameraInput(Camera camera) {
		this.camera = camera;
		showHelp();
		
		if(mouseControlled) {
			try {
				robot = new Robot();
			} catch (AWTException e) {
				Yeti.screwed("Could not create mouse controller.", e);
			}
		}
	}
	
	public CameraInput() {
		showHelp();
	}
	
	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	@Override
	public void keyTyped(KeyEvent e) {	}
	
	@Override
	public void keyReleased(KeyEvent e) { }
	
	@Override
	public void keyPressed(KeyEvent e) {
		
		switch (e.getKeyCode()) {
			case KeyEvent.VK_Q:
				camera.strafeLeft();
				break;
				
			case KeyEvent.VK_E:
				camera.strafeRight();
				break;
		
			case KeyEvent.VK_LEFT:
				//camera.turnLeft();
				break;
				
			case KeyEvent.VK_A:
				if(mouseControlled) {
					camera.strafeLeft();
				} else {
					camera.turnLeft();
				}
				break;
			
			case KeyEvent.VK_RIGHT:
				//camera.turnRight();
				break;
				
			case KeyEvent.VK_D:
				if(mouseControlled) {
					camera.strafeRight();
				} else {
					camera.turnRight();
				}
				break;
				
			case KeyEvent.VK_UP:
				//camera.turnUp();
				break;
				
			case KeyEvent.VK_W:
				if( (e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
					camera.strafeUp();
				} else {
					camera.forward();
				}
				break;
				
			case KeyEvent.VK_DOWN:
				//camera.turnDown();
				break;
				
			case KeyEvent.VK_S:		
				if( (e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
					camera.strafeDown();
				} else {
					camera.backward();
				}
				break;
				
			default:
				break;
		}
	}
		
	int lastDragX, lastDragY;
	int lastMoveX, lastMoveY;
	
	@Override
	public void mouseDragged(MouseEvent e) {
		//int dx = lastDragX - e.getX();
		//int dy = lastDragY - e.getY();
		
		lastDragX = e.getX();
		lastDragY = e.getY();
	}

	private static Point auxPoint = new Point();
	@Override
	public void mouseMoved(MouseEvent e) {
		if(!mouseControlled) return;
		
		int xcenter = e.getComponent().getWidth() / 2;
		int ycenter = e.getComponent().getHeight() / 2;
		auxPoint.setLocation(xcenter, ycenter);
		SwingUtilities.convertPointToScreen(auxPoint, e.getComponent());
		int dx =  xcenter - e.getX();
		int dy =  ycenter - e.getY(); 
		
		dx = MathUtil.clamp(dx, -20, 20);
		dy = MathUtil.clamp(dy, -20, 20);
		
		camera.move3D(dx, dy);
		
		robot.mouseMove(auxPoint.x, auxPoint.y);
	}

	@Override
	public void mouseClicked(MouseEvent e) { }
	@Override
	public void mousePressed(MouseEvent e) { }
	@Override
	public void mouseReleased(MouseEvent e) { }
	@Override
	public void mouseEntered(MouseEvent e) { }
	@Override
	public void mouseExited(MouseEvent e) { }

	public boolean isMouseControlled() {
		return mouseControlled;
	}

	public void setMouseControlled(boolean mouseControlled) {
		this.mouseControlled = mouseControlled;
		if(mouseControlled) {
			if(robot == null) { 
					try {
					robot = new Robot();
				} catch (AWTException e) {
					e.printStackTrace();
				}
			}
		}
	}
}