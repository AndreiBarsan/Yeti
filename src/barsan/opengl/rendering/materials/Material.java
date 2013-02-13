package barsan.opengl.rendering.materials;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.Model;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.rendering.Shader;
import barsan.opengl.rendering.StaticModel;
import barsan.opengl.util.Color;

import com.jogamp.opengl.util.texture.Texture;

/**
 * This should wrap around a shader. Given a certain ModelInstance and RenderState,
 * it should know how to set up all the shader's uniforms and attributes.
 * 
 * @author Andrei Barsan
 *
 */
public abstract class Material {
	
	private int positionIndex, normalIndex, texcoordIndex, tangentIndex, bitangentIndex;
	protected Shader shader;
	protected Color ambient, diffuse, specular;
	
	protected boolean writesDepthBuffer = true;
	protected boolean checksDepthBuffer = true;
	protected boolean ignoreLights = false;
	
	/**
	 *	This is the exponent of the specular highlight. 
	 */
	protected int shininess = 128;
	protected Texture texture = null;
	protected List<MaterialComponent> components = new ArrayList<MaterialComponent>();
	
	public Material(Shader shader) {
		this(shader, Color.WHITE, Color.WHITE, Color.WHITE);
	}
	
	public Material(Shader shader, Color ambient, Color diffuse, Color specular) {
		assert shader != null;
		
		this.shader = shader;
		
		this.positionIndex = shader.getAttribLocation(Shader.A_POSITION);
		this.normalIndex = shader.getAttribLocation(Shader.A_NORMAL);
		this.texcoordIndex = shader.getAttribLocation(Shader.A_TEXCOORD);
	//	this.tangentIndex = shader.getAttribLocation("vTangent");
	//	this.bitangentIndex = shader.getAttribLocation("vBitangent");
		
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
	}

	/**
	 * Called by the renderer before the modelinstance to which this material is
	 * assigned to gets rendered. Enables the shader and sets up its required
	 * state. 
	 * 
	 * Note: this doesn't actually render anything yet!
	 * 
	 * @param rendererState	Information about the shader.
	 * @param modelMatrix	Model transform of the current model instance.
	 */
	public void setup(RendererState rendererState, Matrix4 modelMatrix) {
		enableShader(rendererState);
		/*
		 * All unbound textures now default to this. As long as no CUBE MAPS
		 * are left unbound all should be ok. Bug status: will not fix as it is.
		 * It will automagically be resolved by dynamic shader generation.
		 */
		int textureIndex = 1;
		
		for (MaterialComponent c : components) {
			//System.out.println("Setting up " + c + "...");
			c.setup(this, rendererState, modelMatrix);
			//System.out.println("Binding at TI=" + textureIndex);
			textureIndex += c.setupTexture(this, rendererState, textureIndex);
			//System.out.println("TI now equals: " + textureIndex);
			//System.out.println();
		}
	}
	
	/**
	 * Called by the renderer after it is finished with the batch of items with
	 * this material. Cleans up the shader state so that (possibly) other 
	 * materials using the same shader program don't break. This is usually
	 * associated with shader components, as they can cause bugs if they don't
	 * clean after themselves.
	 * 
	 * @param rendererState
	 */
	public void cleanUp(RendererState rendererState) {
		for (MaterialComponent c : components) {
			c.cleanUp(this, rendererState);
		}
	}
	
	public void render(RendererState rendererState, Model model) {
		GL gl = rendererState.gl;
		enableShader(rendererState);
		
		gl.glDepthMask(writesDepthBuffer);
		if(checksDepthBuffer) {
			gl.glEnable(GL2.GL_DEPTH_TEST);
		} else {
			gl.glDisable(GL2.GL_DEPTH_TEST);
		}
		
		model.render(model.getArrayLength());
	}

	protected void enableShader(RendererState rendererState) {
		rendererState.gl.glUseProgram(shader.getHandle());
	}
	
	public void bindTextureCoodrinates(Model model) {
		if(texture != null) {
			model.getTexCoords().use(texcoordIndex);
		}
	}

	public void unsetBuffers(Model model) {
		model.cleanUp(positionIndex, normalIndex, texcoordIndex);
		/*
		model.getVertices().cleanUp(positionIndex);
		if(!ignoreLights) {
			model.getNormals().cleanUp(normalIndex);
		}
		if(texture != null) {
			// FIXME: sort of a hack
			model.getTexcoords().cleanUp(texcoordIndex);
		}*/
	}
	
	public int getPositionIndex() {
		return positionIndex;
	}
	
	public int getNormalIndex() {
		return normalIndex;
	}
	
	public int getTexcoordIndex() {
		return texcoordIndex;
	}
	
	public int getTangentIndex() {
		return tangentIndex;
	}
	
	public int getBitangentIndex() {
		return bitangentIndex;
	}

	public Color getDiffuse() {
		return diffuse;
	}

	public void setDiffuse(Color diffuse) {
		this.diffuse = diffuse;
	}

	public Color getSpecular() {
		return specular;
	}

	public void setSpecular(Color specular) {
		this.specular = specular;
	}

	public float getShininess() {
		return shininess;
	}

	public void setShininess(int shininess) {
		this.shininess = shininess;
	}

	public Texture getTexture() {
		return texture;
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	public boolean getUseDepthBuffer() {
		return writesDepthBuffer;
	}
	
	public void setIgnoresLights(boolean val) {
		ignoreLights = val;
	}
	
	public boolean ignoresLights() {
		return ignoreLights;
	}
	
	public boolean getCheckDepthBuffer() {
		return checksDepthBuffer;
	}
	
	public boolean writesToDepthBuffer() {
		return writesDepthBuffer;
	}

	public void setWriteDepthBuffer(boolean writeDepthBuffer) {
		this.writesDepthBuffer = writeDepthBuffer;
	}

	public void setCheckDepthBuffer(boolean checkDepthBuffer) {
		this.checksDepthBuffer = checkDepthBuffer;
	}
	
	public Color getAmbient() {
		return ambient;
	}

	public void setAmbient(Color ambient) {
		this.ambient = ambient;
	}

	public void addComponent(MaterialComponent component) {
		components.add(component);
	}

	public void removeComponent(MaterialComponent component) {
		if(!components.remove(component)) {
			Yeti.screwed("Tried to remove non-existing material component!");
		}
	}

	public boolean containsComponent(MaterialComponent component) {
		return components.contains(component);
	}
	
	public Shader getShader() {
		return shader;
	}
}
