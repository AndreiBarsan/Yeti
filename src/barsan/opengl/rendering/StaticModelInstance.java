package barsan.opengl.rendering;

import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.math.Transform;
import barsan.opengl.rendering.materials.BasicMaterial;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.rendering.materials.TextureComponent;

import com.jogamp.opengl.util.texture.Texture;

public class StaticModelInstance extends ModelInstance {

	/* pp */ StaticModel model;
	/* pp */ Material material;
	
	public StaticModelInstance(StaticModel model) {
		this(model, new BasicMaterial(), new Transform());
	}
	
	public StaticModelInstance(StaticModel model, Material material) {
		this(model, material, new Transform());
	}
	
	public StaticModelInstance(StaticModel model, Transform transform) {
		this(model, new BasicMaterial(), transform);
	}
	
	public StaticModelInstance(StaticModel model, Material material, Transform localTransform) {
		this.model = model;
		this.material = material;
		this.localTransform = localTransform;

		castsShadows = true;
	}
	
	@Override
	public void render(RendererState rendererState, Matrix4Stack transformStack) {
		transformStack.push(localTransform.get());
	
		Material activeMaterial;
		if(rendererState.hasForcedMaterial()) {
			activeMaterial = rendererState.getForcedMaterial();
		} else {
			activeMaterial = material;
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
		
		// Ya need to call glDisableVertexAttribArray cuz otherwise the
		// fixed pipeline rendering gets messed up, yo!
		// Mild bug ~1.5h 28.11.2012
	
		activeMaterial.unsetBuffers(model);
		activeMaterial.cleanUp(rendererState);
		
		for (Renderable mi : children) {
			mi.render(rendererState, transformStack);
		}
	
		transformStack.pop();
	}


	@Override
	public Material getMaterial() {
		return material;
	}

	@Override
	public void setMaterial(Material material) {
		this.material = material;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(StaticModel model) {
		this.model = model;
	}
	
	@Override
	public void setTexture(Texture texture) {
		material.setTexture(texture);
		material.addComponent(new TextureComponent());
	}
}
