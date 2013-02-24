package barsan.opengl.flat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import barsan.opengl.Yeti;
import barsan.opengl.math.Rectangle;
import barsan.opengl.math.Segment2D;
import barsan.opengl.math.Vector2;
import barsan.opengl.planetHeads.Coin;
import barsan.opengl.planetHeads.HeavenBeam;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;

/**
 * TODO: distinction between static entities (e.g. walls) that don't change over
 * time (and don't need to be reset) and dynamic entities (e.g. coins, enemies)
 * that DO need resetting.
 * 
 * @author Andrei Bârsan
 */
public class World2D {

	/* The 3D host scene handling the rendering */
	private Scene scene;
	private List<Entity2D> entities = new ArrayList<>();
	private List<Entity2D> toDispose = new ArrayList<>();
	private float gravity = 200.0f;
	private boolean updating = false;
	private Player player;
	
	public World2D(Scene scene) {
		Yeti.debug("Initialized 2D world");
		this.scene = scene;
		
		// The player should be persistent throughout the resets
		player = new Player(new Vector2(4.0f, 2.0f));
	}
	
	public void update(float delta) {
		updating = true;
		for(int i = 0; i < entities.size() - 1; i++) {
			Rectangle eBounds = entities.get(i).getPhysics2d().getBounds();
			for(int j = i + 1; j < entities.size(); j++) {
				
				if(entities.get(j).getPhysics2d().getBounds().overlaps(eBounds)) {
					entities.get(i).getPhysics2d().addContact(entities.get(j).getPhysics2d());
					entities.get(j).getPhysics2d().addContact(entities.get(i).getPhysics2d());
				}
			}
		}
		
		for(int i = 0; i < entities.size(); i++) {
			if( ! entities.get(i).isDead()) {
				// Maybe e could return a boolean showing whether it changed its 
				// position? Could save a lot of unnecessary updates of the qt.
				// quadTree.rePosition(e);
				entities.get(i).update(delta);
			}
			entities.get(i).getPhysics2d().intersected.clear();
		}
		updating = false;
		
		for(int i = 0; i < toDispose.size(); i++) {
			scene.removeModelInstance(toDispose.get(i).getGraphics());
			entities.remove(toDispose.get(i));
		}
		toDispose.clear();
	}
	
	public void reset() {
		clearAllEntities();
		// Left wall
		addBlock(-10, -20, 5, 40);
		// Walkway A
		addBlock(-5, -14, 40, 4);
		// Walkway B
		addBlock(25, -20, 20, 4);
		// Right square-ish wall/walkway
		addBlock(45, -15, 15, 12);
		
		Random rand = new Random();
		for(int i = 0; i < 20; i++) {
			addBlock(i * 10, -0.6f + i * 3.5f, 8, 2);
			if(i % 5 == 0) {
				addBlock(i * 10 - 2 + rand.nextFloat() * 4, 20f + i * 3.5f, 2 + rand.nextFloat() * 6, 4);
			}
		}
		
		for(int i = 0; i < 8; i++) {
			addEntity(new Coin(new Vector2(5 * i, -4.5f)));
		}
		
		addEntity(player);
		
		addEntity(new HeavenBeam(195f, 70f));		
	}
	
	
	public void addEntity(Entity2D entity) {
		entities.add(entity);
		entity.init(this);
		scene.addModelInstance(entity.graphics);
	}
	
	public void clearAllEntities() {
		// Warning: be careful not to tag things as dead when they're supposed
		// to be persistent!
		for(int i = 0; i < entities.size(); i++) {
			scene.removeModelInstance(entities.get(i).getGraphics());
		}
		entities.clear();
	}
	
	public void remove(Entity2D e) {
		if(updating) {
			toDispose.add(e);
		} else {
			entities.remove(e);
		}
	}
	
	/// Returns true if the two entities intersect
	public boolean intersect(Physics2D e1, Physics2D e2) {
		Rectangle r1 = e1.bounds;
		Rectangle r2 = e2.bounds;
		
		return r1.overlaps(r2);
	}
	
	/// Tries to see if e collides with anything; if it does, return that entity
	public Physics2D collideWithLevel(Physics2D e) {
		Rectangle r = e.bounds;
		for(Entity2D target : entities) {
			if(!target.physics.solid) continue;
			
			if(target.physics.bounds.overlaps(r))
				return target.physics;
		}
		
		return null;			
	}
	
	public Rectangle testRectangleFree(Rectangle original, Rectangle r) {
		for(Entity2D t : entities) {
			if(!t.physics.solid) continue;
			if(original.equals(t.physics.bounds)) continue;
			
			if(t.physics.bounds.overlaps(r)) return t.physics.bounds;
		}
		return null;
	}
	
	public Physics2D pollPosition(Vector2 position) {
		// TODO: when using quadtrees, just see where position should be in the
		// tree and only check that branch
		for(Entity2D target : entities) {
			if(!(target.physics.solid)) continue;
						
			Rectangle nr = target.physics.getBounds();
			if(nr.contains(position)) {
				return target.physics;
			}
		}
		
		return null;
	}
	
	public Physics2D pollSegment(Segment2D segment, Physics2D toExclude) {
		return null;
	}
	
	// Moves the mover as close as possible to obstacle, without causing
	// a collision
	public void moveToContactFloor(Physics2D mover, Physics2D obstacle) {
		// since we're working with rectangles, we need to classify the mover's
		// velocity vector into four possible states (TOP, RIGHT, BOTTOM, LEFT)
		// of the obstacle
		Rectangle or = obstacle.bounds;
		Rectangle mr = mover.bounds;
		
		mover.setPosition(mr.x, or.y + or.height);
			
		assert( ! mr.overlaps(or)) : "The contact should no longer be true";
	}
	
	private void addBlock(float x, float y, float w, float h) {
		float divW = 10.0f;
		float divH = 10.0f;
		
		int wSteps = (int)(w / divW);
		int hSteps = (int)(h / divH);
		
		float wReminder = w - wSteps * divW;
		float hReminder = h - hSteps * divH;
		
		for(int i = 0; i < wSteps; i++) {
			for(int j = 0; j < hSteps; j++) {
				addEntity(new Block(new Rectangle(x + i * divW, y + j * divH, divW, divH), ResourceLoader.texture("block01")));
			}
			if(hReminder > 0.01f) {
				addEntity(new Block(new Rectangle(x + i * divW, y + h - hReminder, divW, hReminder), ResourceLoader.texture("block01")));
			}
		} 
		if(wReminder > 0.01f) {
			for(int j = 0; j < hSteps; j++) {
				addEntity(new Block(new Rectangle(x + w - wReminder, y + j * divH, wReminder, divH), ResourceLoader.texture("block01")));
			}
			if(hReminder > 0.01f) {
				addEntity(new Block(new Rectangle(x + w - wReminder, y + h - hReminder, wReminder, hReminder), ResourceLoader.texture("block01")));
			}
		}
	}
	
	public float getGravity() {
		return gravity;
	}

	public void setGravity(float gravity) {
		this.gravity = gravity;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Scene getScene() {
		return scene;
	}
}
