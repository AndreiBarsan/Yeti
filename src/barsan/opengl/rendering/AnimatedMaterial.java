package barsan.opengl.rendering;

import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.materials.LightComponent;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.rendering.materials.ShadowReceiver;
import barsan.opengl.rendering.materials.WorldTransform;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;

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
		this(ResourceLoader.shader("animatedPhong"));
	}
		
	public AnimatedMaterial(Shader shader) {
		super(shader);
		
		positionStartIndex = shader.getAttribLocation(A_POSITION_START);
		positionEndIndex = shader.getAttribLocation(A_POSITION_END);
		normalStartIndex = shader.getAttribLocation(A_NORMAL_START);
		normalEndIndex = shader.getAttribLocation(A_NORMAL_END);
		
		// Updated name
		texcoordIndex = shader.getAttribLocation(A_TEXCOORD);
		
		addComponent(new WorldTransform());
		addComponent(new LightComponent());
		addComponent(new ShadowReceiver());
		
		setAmbient(Color.WHITE.copy());
		setSpecular(Color.WHITE.copy());
		setDiffuse(Color.WHITE.copy());
	}
	
	@Override
	public void setup(RendererState rendererState, Matrix4 modelMatrix) {
		super.setup(rendererState, modelMatrix);
		
		shader.setUVector4f("matAmbient", ambient.getData());
		shader.setUVector4f("matDiffuse", diffuse.getData());
		shader.setUVector4f("matSpecular", specular.getData());
		shader.setU1i("shininess", specularPower);
	}
	
	@Override
	public void render(RendererState rendererState, Model model) {
		// All VBO lengths are equal, this should work ok.
		super.render(rendererState, model);
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
