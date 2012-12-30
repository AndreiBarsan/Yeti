package barsan.opengl.rendering;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.media.opengl.GL2;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Matrix4Stack;

public class ModelInstance implements Renderable {

	protected Model model;
	private Material material;
	private Matrix4 localTransform;

	protected ModelInstance parent = null;
	protected ArrayList<ModelInstance> children;

	//*
	public ModelInstance(Model model) {
		this(model, new BasicMaterial(), new Matrix4());
	}
	
	/*
	public ModelInstance(Model model, Matrix4 transform) {
		this(model, null, transform);
	}
	
	public ModelInstance(Model model, Shader shader) {
		this(model, shader, new Matrix4());
	}*/

	public ModelInstance(Model model, Material material, Matrix4 localTransform) {
		this.model = model;
		this.material = material;
		this.localTransform = localTransform;

		children = new ArrayList<>();
	}

	@Override
	public void render(RendererState rendererState, Matrix4Stack transformStack) {
		GL2 gl = rendererState.getGl();

		transformStack.push(localTransform);

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

		material.bindTextureCoodrinates(model);
		material.render(rendererState, model);
		if(material.getTexture() != null) {
			//gl.glBindTexture(GL2.GL_TEXTURE_2D_MULTISAMPLE, 0);
		}
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

	public Matrix4 getTransform() {
		return localTransform;
	}

	public void setTransform(Matrix4 transform) {
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
