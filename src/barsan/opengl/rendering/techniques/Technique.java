package barsan.opengl.rendering.techniques;

import java.util.List;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.rendering.Shader;

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
 * @author Andrei Bârsan
 */
public abstract class Technique {
	
	protected Shader program;
	int vi, ni, tci;
	int tangentIndex;
	int binormalIndex;
	
	public static Technique current = null;
	
	protected Matrix4 view = new Matrix4();
	protected Matrix4 projection = new Matrix4();
	protected Matrix4 viewModel = new Matrix4();
	protected Matrix4 MVP = new Matrix4();
	
	public Technique(Shader program) {
		this.program = program;
		vi = program.getAttribLocation(Shader.A_POSITION);
		ni = program.getAttribLocation(Shader.A_NORMAL);
		tci = program.getAttribLocation(Shader.A_TEXCOORD);
		
		tangentIndex = program.getAttribLocation("vTang");
		binormalIndex = program.getAttribLocation("vBinorm");
		
		if(vi == -1) {
			Yeti.warn("No vertex input! Vertices are pretty important. Are you " +
					"sure this is the behavior you want? Culprit: " + program);
		}
	}
	
	public void setup(RendererState rs) {
		Technique.current = this; 
		rs.gl.glUseProgram(program.getHandle());
	}
	
	public int getVertexIndex() {
		return vi;
	}
	
	public int getNormalIndex() {
		return ni;
	}
	
	public int getTexCoordIndex() {
		return tci;
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
	
	/**
	 * Standard method that allows hierarchies of model instances to be rendered.
	 * Specific uniform binding should occurr in the instanceRenderSetup method.
	 */
	public void renderDude(ModelInstance mi, RendererState rs, Matrix4Stack matrixStack) {
		matrixStack.push(mi.getTransform().get());
		Matrix4 modelMatrix = matrixStack.peek().cpy();
		viewModel.set(view).mul(modelMatrix);
		MVP.set(projection).mul(view).mul(modelMatrix);
		
		program.setUMatrix4("mvpMatrix", MVP);
		program.setUMatrix4("mvMatrix", viewModel);
		program.setUMatrix4("mMatrix", modelMatrix);
		
		instanceRenderSetup(mi, rs, matrixStack);
		
		mi.techniqueRender();
		
		for(ModelInstance child : mi.getChildren()) {
			renderDude(child, rs, matrixStack);
		}
		matrixStack.pop();
	}
	
	//public abstract void render(GL3 gl, List<ModelInstance> toDraw);
}
