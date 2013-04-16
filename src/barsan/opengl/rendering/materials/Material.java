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
import barsan.opengl.util.Color;

import com.jogamp.opengl.util.texture.Texture;

/**
 * Contains material data that's to be rendered with a certain technique.
 *  
 * @author Andrei Barsan
 *
 */
public class Material {
	
	protected int positionIndex, normalIndex, texcoordIndex, tangentIndex, binormalIndex;
	protected Shader shader;
	protected Color ambient, diffuse, specular;
	
	protected boolean writesDepthBuffer = true;
	protected boolean checksDepthBuffer = true;
	protected boolean ignoreLights = false;
	
	protected float specularIntensity = 1;
	protected int specularPower = 1;
	
	protected Texture diffuseMap = null;
	protected Texture normalMap = null;
	
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
		this.tangentIndex = shader.getAttribLocation("vTang");
		this.binormalIndex = shader.getAttribLocation("vBinorm");
		
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
		// Silly bug: 2 hours wasted 22.11.2012 because I forgot to actually
		// set a shader... :|
		enableShader(rendererState);
		
		/*
		 * All unbound textures now default to this (index 0). As long as no CUBE MAPS
		 * are left unbound all should be ok. Bug status: will not fix as it is.
		 * It will automagically be resolved by dynamic shader generation.
		 */
		int textureIndex = 1;
		
		for (MaterialComponent c : components) {
			c.setup(this, rendererState, modelMatrix);
			textureIndex += c.setupTexture(this, rendererState, textureIndex);
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
		if(diffuseMap != null) {
			model.getTexCoords().use(texcoordIndex);
		}
	}

	public void unsetBuffers(Model model) {
		model.cleanUp(positionIndex, normalIndex, texcoordIndex, tangentIndex, binormalIndex);
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
	
	public int getBiormalIndex() {
		return binormalIndex;
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

	public float getSpecularPower() {
		return specularPower;
	}

	public void setSpecularPower(int specularPower) {
		this.specularPower = specularPower;
	}

	public Texture getDiffuseMap() {
		return diffuseMap;
	}

	public void setDiffuseMap(Texture texture) {
		this.diffuseMap = texture;
	}

	public Texture getNormalMap() {
		return normalMap;
	}
	
	public void setNormalMap(Texture bump) {
		this.normalMap = bump;
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

	public float getSpecularIntensity() {
		return specularIntensity;
	}

	public void setSpecularIntensity(float specularIntensity) {
		this.specularIntensity = specularIntensity;
	}
}
