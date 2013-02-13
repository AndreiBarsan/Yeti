package barsan.opengl.rendering;

import barsan.opengl.math.Matrix4Stack;

public class AnimatedModelInstance extends ModelInstance {

	protected AnimatedModel model;
	protected AnimatedMaterial material;
	
	public AnimatedModelInstance(AnimatedModel model, AnimatedMaterial material) {
		this.model = model;
		this.material = material;
	}
	
	@Override
	public void render(RendererState rendererState, Matrix4Stack transformStack) {
		transformStack.push(localTransform.get());
		
		AnimatedMaterial activeMaterial;
		if(rendererState.hasForcedMaterial()) {
			activeMaterial = rendererState.getForcedAnimatedMaterial();
		} else {
			activeMaterial = material;
		}
		
		activeMaterial.setup(rendererState, transformStack.result());
		
		int f1 = 0;
		int f2 = 1;
		float tweenIndex = 0.5f;
		
		
		int pIndexStart = activeMaterial.getPositionStartIndex();
		int pIndexEnd = activeMaterial.getPositionEndIndex();
		model.getFrames().get(f1).model.getVertices().use(pIndexStart);
		model.getFrames().get(f2).model.getVertices().use(pIndexEnd);
		
		int nIndexStart = activeMaterial.getNormalStartIndex();
		int nIndexEnd = activeMaterial.getNormalEndIndex();
		model.getFrames().get(f1).model.getNormals().use(nIndexStart);
		model.getFrames().get(f2).model.getNormals().use(nIndexEnd);
		
		
		activeMaterial.bindTextureCoodrinates(model);
		
		// Buffers are bound by this point
		activeMaterial.render(rendererState, model);	
		activeMaterial.unsetBuffers(model);
		activeMaterial.cleanUp(rendererState);
		
		for(Renderable mi : children) {
			mi.render(rendererState, transformStack);
		}
		
		transformStack.pop();
	}
}
