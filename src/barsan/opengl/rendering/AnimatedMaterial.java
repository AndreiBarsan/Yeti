package barsan.opengl.rendering;

import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.resources.ResourceLoader;

public class AnimatedMaterial extends Material {
	
	static final String A_POSITION_START = "inPositionStart";
	static final String A_POSITION_END = "inPositionEnd";
	
	static final String A_NORMAL_START = "inNormalStart";
	static final String A_NORMAL_END = "inNormalEnd";
	
	static final String A_TEXCOORD = "inTexCoord"; 
	
	private int positionStartIndex,
				positionEndIndex,
				normalStartIndex,
				normalEndIndex;
	
	public AnimatedMaterial() {
		super(ResourceLoader.shader("animatedPhong"));
	}
	
	@Override
	public void setup(RendererState rendererState, Matrix4 modelMatrix) {
		super.setup(rendererState, modelMatrix);
	}

	/*
	public void render(RendererState rendererState, AnimatedModel model, 
			int f1, int f2, float tweenIndex) {
		
	}*/
	
	
	@Override
	public void render(RendererState rendererState, Model model) {
		// OOP hack?
		// Yeti.screwed("Insufficient data to render animation. Use void render(RendererState, AnimatedModel, int, int, float).");
		super.render(rendererState, model);
		// All VBO lengths are equal, this should work fayynn.
	}

	public int getPositionStartIndex() {
		return positionStartIndex;
	}

	public int getPositionEndIndex() {
		return positionEndIndex;
	}

	public int getNormalStartIndex() {
		return normalStartIndex;
	}

	public int getNormalEndIndex() {
		return normalEndIndex;
	}
	
}
