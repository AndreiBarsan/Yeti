package barsan.opengl.rendering;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.math.Transform;
import barsan.opengl.rendering.materials.Material;

public class AnimatedModelInstance extends ModelInstance {

	/* pp */ AnimatedModel model;
	/* pp */ AnimatedMaterial material;
	
	float tweenIndex = 0.0f;
	int cf = 0;
	public boolean playing;
	
	public AnimatedModelInstance(AnimatedModel model)  {
		this(model, new AnimatedMaterial(), new Transform());
	}
	
	public AnimatedModelInstance(AnimatedModel model, AnimatedMaterial material) {
		this(model, material, new Transform());
	}
	
	public AnimatedModelInstance(AnimatedModel model, AnimatedMaterial material, Transform transform) {
		this.model = model;
		this.material = material;
		localTransform = transform;
	}
	
	public void updateAnimation(float delta) {
		int n = model.getFrames().size();
		
		tweenIndex += delta;
		float step = 1 / 45.0f;
		if(tweenIndex >= step) {
			while(tweenIndex > step) tweenIndex -= step;
			
			if( ! ( (cf <= 2 || cf >= 30 || Math.abs(cf - 16) <= 2 ) && !playing) ) {
				cf = (cf + 1) % n;
			} else {
				cf = 0;
			}
		}
	}
	
	@Override
	public void render(RendererState rendererState, Matrix4Stack transformStack) {
		transformStack.push(localTransform.get());
		
		AnimatedMaterial activeMaterial;
		if(rendererState.hasForcedAnimatedMaterial()) {
			activeMaterial = rendererState.getForcedAnimatedMaterial();
		} else {
			activeMaterial = material;
		}
		
		activeMaterial.setup(rendererState, transformStack.result());
		
		int n = model.getFrames().size();
		int f1 = cf;
		int f2 = (cf + 1) % n;
		
		if(cf == 0) f1 = f2 = 0;
		
		Shader s = material.getShader();
		s.setU1f("animationIndex", tweenIndex);
		
		StaticModel f1model = model.getFrames().get(f1).model;
		StaticModel f2model = model.getFrames().get(f2).model;
		
		int pIndexStart = activeMaterial.getPositionStartIndex();
		int pIndexEnd = activeMaterial.getPositionEndIndex();
		f1model.getVertices().use(pIndexStart);
		f2model.getVertices().use(pIndexEnd);
		
		int nIndexStart = activeMaterial.getNormalStartIndex();
		int nIndexEnd = activeMaterial.getNormalEndIndex();
		f1model.getNormals().use(nIndexStart);
		f2model.getNormals().use(nIndexEnd);		
		
		activeMaterial.bindTextureCoodrinates(model);
		
		// Buffers are bound by this point
		activeMaterial.render(rendererState, model);
		
		// Make sure that both bound frames get unbound
		f1model.cleanUp(pIndexStart, nIndexStart);
		f2model.cleanUp(nIndexStart, nIndexEnd);
		
		activeMaterial.cleanUp(rendererState);
		
		for(Renderable mi : children) {
			mi.render(rendererState, transformStack);
		}
		
		transformStack.pop();
	}
	
	@Override
	public AnimatedModel getModel() {
		return model;
	}
	
	@Override
	public void techniqueRender() {
		Yeti.screwed("Not yet implemented.");	
	}
	
	@Override
	public Material getMaterial() {
		return material;
	}
	
	@Override
	public void setMaterial(Material material) {
		this.material = (AnimatedMaterial) material;	
	}
}
