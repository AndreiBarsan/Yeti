package barsan.opengl.rendering;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;

public abstract class Technique {
	
	protected Shader program;
	int vi, ni, tci;
	
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
		
		if(vi == -1) {
			Yeti.warn("No vertex input! " + program);
		}
		if(ni == -1) {
			//Yeti.warn("No normal input! " + program);
		}
		if(tci == -1) {
			//Yeti.warn("No texcoord input! " + program);
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
	
	//public abstract void render(GL3 gl, List<ModelInstance> toDraw);
}
