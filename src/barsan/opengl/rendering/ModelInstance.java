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

	protected ModelInstance parent = null;
	protected ArrayList<ModelInstance> children;

	public ModelInstance(Model model) {
		this(model, new BasicMaterial(), new Transform());
	}
	
	public ModelInstance(Model model, Material material) {
		this(model, material, new Transform());
	}
	
	/*
	public ModelInstance(Model model, Matrix4 transform) {
		this(model, null, transform);
	}
	*/
	public ModelInstance(Model model, Material material, Transform localTransform) {
		this.model = model;
		this.material = material;
		this.localTransform = localTransform;

		children = new ArrayList<>();
	}

	@Override
	public void render(RendererState rendererState, Matrix4Stack transformStack) {
		GL2 gl = rendererState.gl;

		transformStack.push(localTransform.get());

		if (material != null) {
			material.setup(rendererState, transformStack.result());
		} else {
			rendererState.getDefaultMaterial().setup(rendererState, transformStack.result());
		}

		int pindex = material.getPositionIndex();
		model.getVertices().use(pindex);

		int nindex = material.getNormalIndex();

		if (!material.ignoresLights()) {
			model.getNormals().use(nindex);
		}
		
		/*
		if(model.getTangents() != null) {
			int tangentsIndex = material.getTangentIndex();
			model.getTangents().use(tangentsIndex);
		}
		if(model.getBitangents() != null) {
			int bitangentsIndex = material.getBitangentIndex();
			model.getBitangents().use(bitangentsIndex);
		}
		//*/

		material.bindTextureCoodrinates(model);
		material.render(rendererState, model);
		// Ya need to disable glDisableVertexAttribArray cuz otherwise the
		// fixed pipeline rendering gets messed up, yo!
		// Mild bug ~1.5h 28.11.2012

		material.unsetBuffers(model);
		
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
