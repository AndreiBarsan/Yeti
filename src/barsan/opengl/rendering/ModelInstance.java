package barsan.opengl.rendering;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.media.opengl.GL2;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.math.Transform;
import barsan.opengl.rendering.materials.BasicMaterial;
import barsan.opengl.rendering.materials.Material;

public class ModelInstance implements Renderable {

	protected Model model;
	private Material material;
	private Transform localTransform;
	private boolean castsShadows;

	protected ModelInstance parent = null;
	protected ArrayList<ModelInstance> children;

	public ModelInstance(Model model) {
		this(model, new BasicMaterial(), new Transform());
	}
	
	public ModelInstance(Model model, Material material) {
		this(model, material, new Transform());
	}
	
	public ModelInstance(Model model, Transform transform) {
		this(model, new BasicMaterial(), transform);
	}
	
	public ModelInstance(Model model, Material material, Transform localTransform) {
		this.model = model;
		this.material = material;
		this.localTransform = localTransform;

		castsShadows = true;
		children = new ArrayList<>();
	}

	@Override
	public void render(RendererState rendererState, Matrix4Stack transformStack) {
		transformStack.push(localTransform.get());

		Material activeMaterial;
		if(rendererState.hasForcedMaterial()) {
			activeMaterial = rendererState.getForcedMaterial();
		} else if (material != null) {
			activeMaterial = material;
		} else {
			activeMaterial = rendererState.getDefaultMaterial();
		}
		
		activeMaterial.setup(rendererState, transformStack.result());

		int pindex = activeMaterial.getPositionIndex();
		model.getVertices().use(pindex);

		if (!activeMaterial.ignoresLights()) {
			int nindex = activeMaterial.getNormalIndex();
			model.getNormals().use(nindex);
		}

		activeMaterial.bindTextureCoodrinates(model);
		
		// This should actually be something like:
		// Batcher.getBatch(activeMaterial).add(this, new ImmutableRS(rendererState));
		// - if we do this, the number of uniform sets will drop dramatically!
		// - challenge - optimizing the rendererstate so that it really only
		// holds the information that influence the corresponding object
		activeMaterial.render(rendererState, model);
		
		// Ya need to disable glDisableVertexAttribArray cuz otherwise the
		// fixed pipeline rendering gets messed up, yo!
		// Mild bug ~1.5h 28.11.2012

		activeMaterial.unsetBuffers(model);
		activeMaterial.cleanUp(rendererState);
		
		for (ModelInstance mi : children) {
			mi.render(rendererState, transformStack);
		}

		transformStack.pop();
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public Transform getTransform() {
		return localTransform;
	}
	
	public void setTransform(Matrix4 matrix) {
		this.localTransform.setMatrix(matrix);
	}

	public void setTransform(Transform transform) {
		this.localTransform = transform;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}
	
	public void setCastsShadows(boolean value) {
		castsShadows = value;
	}
	
	public boolean castsShadows() {
		return castsShadows;
	}

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
}
