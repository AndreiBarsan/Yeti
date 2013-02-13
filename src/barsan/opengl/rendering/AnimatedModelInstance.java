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
		
		for(Renderable mi : children) {
			mi.render(rendererState, transformStack);
		}
		
		transformStack.pop();
	}
}
