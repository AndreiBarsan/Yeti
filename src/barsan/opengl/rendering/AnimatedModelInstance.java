package barsan.opengl.rendering;

import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.math.Transform;
import barsan.opengl.rendering.materials.TextureComponent;

import com.jogamp.opengl.util.texture.Texture;

public class AnimatedModelInstance extends ModelInstance {

	protected AnimatedModel model;
	protected AnimatedMaterial material;
	
	public AnimatedModelInstance(AnimatedModel model, AnimatedMaterial material) {
		this(model, material, new Transform());
	}
	
	public AnimatedModelInstance(AnimatedModel model, AnimatedMaterial material, Transform transform) {
		this.model = model;
		this.material = material;
		localTransform = transform;
	}
	
	float tweenIndex = 0.0f;
	int cf = 0;
	
	public boolean playing;
	
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
		
		float delta = rendererState.getScene().getDelta();
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
		
		int f1 = cf;
		int f2 = (cf + 1) % n;
		
		if(cf == 0) f1 = f2 = 0;
		
		Shader s = material.getShader();
		s.setU1f("animationIndex", tweenIndex);
		
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
	
	@Override
	public void setTexture(Texture texture) {
		material.setTexture(texture);
		material.addComponent(new TextureComponent());
	}
}
