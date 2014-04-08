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
import barsan.opengl.rendering.cameras.Camera;

public class CameraInput implements InputProvider, KeyListener, MouseListener, MouseMotionListener {
	
	private Camera camera;
	
	/** Used to keep the mouse in place when looking around. */ 
	private Robot robot;
	
	private boolean mouseControlled = true;
	
	static private final int KSIZE = 255;
	static private final int LIMX = 10;
	static private final int LIMY = 10;
	
	private boolean[] keyboardState = new boolean[KSIZE];
	
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
	
	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	@Override
	public void keyTyped(KeyEvent e) {	}
	
	@Override
	public void keyReleased(KeyEvent e) { 
		if(e.isConsumed()) {
			return;
		}
		
		if(e.getKeyCode() < KSIZE) {
			keyboardState[e.getKeyCode()] = false;
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		
		if(e.isConsumed()) {
			return;
		}
		
		if(e.getKeyCode() < KSIZE) {
			keyboardState[e.getKeyCode()] = true;
		}
	}
		
	public boolean check(int code) {
		assert code < KSIZE;
		return keyboardState[code];
	}
	
	int lastDragX, lastDragY;
	int lastMoveX, lastMoveY;

	private boolean pauseOnCtrl = true;
	
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
		if(pauseOnCtrl && (e.getModifiers() & MouseEvent.CTRL_MASK) != 0) {
			return;
		}
		
		int xcenter = e.getComponent().getWidth() / 2;
		int ycenter = e.getComponent().getHeight() / 2;
		
		auxPoint.setLocation(xcenter, ycenter);
		SwingUtilities.convertPointToScreen(auxPoint, e.getComponent());
		
		int dx =  xcenter - e.getX();
		int dy =  ycenter - e.getY(); 
		
		dx = MathUtil.clamp(dx, -LIMX, LIMX);
		dy = MathUtil.clamp(dy, -LIMY, LIMY);
		
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
	
	/** Whether the camera ignores the mouse when CTRL is pressed */
	public boolean isPauseOnCtrl() {
		return pauseOnCtrl;
	}

	public void setPauseOnCtrl(boolean pauseOnCtrl) {
		this.pauseOnCtrl = pauseOnCtrl;
	}
}