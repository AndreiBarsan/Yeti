package barsan.opengl.rendering;

import java.util.ArrayList;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.math.Transform;
import barsan.opengl.rendering.materials.Material;

import com.jogamp.opengl.util.texture.Texture;

public abstract class ModelInstance implements Renderable {

	protected Transform localTransform;
	protected boolean castsShadows = true;
	protected ModelInstance parent = null;
	protected ArrayList<ModelInstance> children = new ArrayList<>();

	@Override
	public abstract void render(RendererState rendererState, Matrix4Stack transformStack);

	public Transform getTransform() {
		return localTransform;
	}

	public void setTransform(Matrix4 matrix) {
		this.localTransform.setMatrix(matrix);
	}

	public void setTransform(Transform transform) {
		this.localTransform = transform;
	}

	public void setCastsShadows(boolean value) {
		castsShadows = value;
	}

	public boolean castsShadows() {
		return castsShadows;
	}
	
	public abstract void setTexture(Texture texture);
	
	public void addChild(ModelInstance child) {
		children.add(child);
		child.setParent(this);
	}

	public void removeChild(ModelInstance child) {
		if (!children.remove(child)) {
			Yeti.screwed("Tried to remove inexisting ModelInstance child.");
		}
		child.setParent(null);
	}

	private void setParent(ModelInstance parent) {
		this.parent = parent;
	}
	
	public abstract Material getMaterial();
	public abstract void setMaterial(Material material);

}