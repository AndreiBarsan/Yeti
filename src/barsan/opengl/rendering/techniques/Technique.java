package barsan.opengl.rendering.techniques;

import java.util.List;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.rendering.Shader;
import barsan.opengl.rendering.StaticModelInstance;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.resources.ResourceLoader;

/**
 * TODO: the light pass technique violates the current Technique contract, in that
 * it doesn't supply support for rendering model instances.
 * 
 * Possible solution:
 * 		- [ABC] Technique -> (DirectTechnique OR IndirectTechnique)
 * 		- DirectTechnique prevents functionality for rendering dudes directly
 * 		- IndirectTechnique doesn't - it should be the base for more advanced stuff
 * 			like the light pass
 * 
 * @author Andrei B�rsan
 */
public abstract class Technique {
	
	protected Shader program;
	int vertexIndex;
	int normalIndex;
	int texCoordIndex;
	int tangentIndex;
	int binormalIndex;
	
	public static Technique current = null;
	
	protected Matrix4 view = new Matrix4();
	protected Matrix4 projection = new Matrix4();
	protected Matrix4 viewModel = new Matrix4();
	protected Matrix4 MVP = new Matrix4();
	
	public Technique(Shader program) {
		this.program = program;
		vertexIndex = program.getAttribLocation(Shader.A_POSITION);
		normalIndex = program.getAttribLocation(Shader.A_NORMAL);
		texCoordIndex = program.getAttribLocation(Shader.A_TEXCOORD);
		tangentIndex = program.getAttribLocation("vTang");
		binormalIndex = program.getAttribLocation("vBinorm");
		
		if(vertexIndex == -1) {
			Yeti.warn("No vertex input! Vertices are pretty important. Are you " +
					"sure this is the behavior you want? Culprit: " + program);
		}
	}
	
	public void setup(RendererState rs) {
		Technique.current = this; 
		rs.gl.glUseProgram(program.getHandle());
	}
	
	public int getVertexIndex() {
		return vertexIndex;
	}
	
	public int getNormalIndex() {
		return normalIndex;
	}
	
	public int getTexCoordIndex() {
		return texCoordIndex;
	}
	
	public int getTangentIndex() {
		return tangentIndex;
	}
	
	public int getBinormalIndex() {
		return binormalIndex;
	}
	
	public void renderModelInstances(RendererState rs, List<ModelInstance> modelInstances) {
		Matrix4Stack matrixStack = new Matrix4Stack();
		for(ModelInstance modelInstance : modelInstances) {
			renderDude(modelInstance, rs, matrixStack);
		}
	}
	
	protected void instanceRenderSetup(ModelInstance mi, RendererState rs, Matrix4Stack matrixStack) 
	{
	}
	
	/** Loads a material to be used by the technique's shader. */
	public abstract void loadMaterial(RendererState rs, Material material);
	
	/**
	 * Standard method that allows hierarchies of model instances to be rendered.
	 * Specific uniform binding should occur in the instanceRenderSetup method.
	 */
	public void renderDude(ModelInstance mi, RendererState rs, Matrix4Stack matrixStack) {
		Matrix4 modelMatrix = mi.getTransform().get().cpy();
		
		matrixStack.push(modelMatrix);
		viewModel.set(view).mul(modelMatrix);
		MVP.set(projection).mul(view).mul(modelMatrix);
		
		// TODO: maybe only bind these in techniques where they're actually needed?
		program.setUMatrix4("mvpMatrix", MVP);
		program.setUMatrix4("mMatrix", modelMatrix);
		
		instanceRenderSetup(mi, rs, matrixStack);
		mi.techniqueRender(rs);
		
		for(ModelInstance child : mi.getChildren()) {
			renderDude(child, rs, matrixStack);
		}
		matrixStack.pop();
	}
	
	//public abstract void render(GL3 gl, List<ModelInstance> toDraw);
}
