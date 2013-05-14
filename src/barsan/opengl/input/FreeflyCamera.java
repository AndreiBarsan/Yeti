package barsan.opengl.input;

import java.awt.event.KeyEvent;

import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.cameras.PerspectiveCamera;

// Note: might be better to make the actual camera a component, and not inherit
// from it
public class FreeflyCamera extends PerspectiveCamera {

	private CameraInput camInput;
	float speed = 0.0f;
	float rotSpeed = 0.0f;
	
	final float MAXSPEED = 100.0f;
	final float FRICTION = 20.0f;
	final float ACCELERATION = 15.0f;
	final float ACCMUL = 5f;
	
	Vector3 motionVector = new Vector3();
	
	public FreeflyCamera(Scene s, int width, int height) {
		super(width, height);
		
		camInput = new CameraInput(this);
		s.addInput(camInput);		
	}
	
	public void update(float delta) {
		Vector3 newMotion = new Vector3();
		float acc = (camInput.check(KeyEvent.VK_SHIFT)) ? ACCELERATION * ACCMUL : ACCELERATION;
		float msp = (camInput.check(KeyEvent.VK_SHIFT)) ? MAXSPEED * ACCMUL : MAXSPEED;
		float frc = (camInput.check(KeyEvent.VK_SHIFT)) ? FRICTION * ACCMUL : FRICTION;
		
		boolean in = false;
		if(camInput.check(KeyEvent.VK_UP) || camInput.check(KeyEvent.VK_W)) {
			in = true;
			if(camInput.check(KeyEvent.VK_CONTROL)) {
				newMotion.add(direction.copy().cross(up).cross(direction)
						.normalize().mul(acc));
			}
			else {
				newMotion.add(direction.copy().mul(acc));
			}
		} 
		else if(camInput.check(KeyEvent.VK_DOWN) || camInput.check(KeyEvent.VK_S)) {
			in = true;
			if(camInput.check(KeyEvent.VK_CONTROL)) {
				newMotion.sub(direction.copy().cross(up).cross(direction)
						.normalize().mul(acc));
			}
			else {
				newMotion.sub(direction.copy().mul(acc));
			}
		}
		
		if(camInput.check(KeyEvent.VK_LEFT) || camInput.check(KeyEvent.VK_A)) {
			in = true;
			newMotion.add(direction.copy().cross(up).mul(acc));
		} else if(camInput.check(KeyEvent.VK_RIGHT) || camInput.check(KeyEvent.VK_D)) {
			in = true;
			newMotion.sub(direction.copy().cross(up).mul(acc));
		}
				
		float deltaSpd = newMotion.len();
		if(deltaSpd > 0.0f) {
			newMotion.mul(delta);
			motionVector.add(newMotion);
		}
		
		float spd = motionVector.len();
		if(spd > 0.0f) {
			motionVector.normalize();
			
			if( ! in ) {
				spd -= frc * delta;
			}
			
			if(spd > msp * delta) {
				spd = msp * delta;
			} else if(spd < 0.0f) {
				spd = 0.0f;
			}
			motionVector.mul(spd);
		}
		
		eyePosition.add(motionVector);
	}
	
	public void setMouseControlled(boolean mouseControlled) {
		camInput.setMouseControlled(mouseControlled);
	}
	
	public boolean isMouseControlled() {
		return camInput.isMouseControlled();
	}

}
