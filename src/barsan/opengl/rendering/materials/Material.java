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
import barsan.opengl.util.Log;

import com.jogamp.opengl.util.texture.Texture;

/**
 * Contains material data that's to be rendered with a certain technique.
 *  
 * @author Andrei Barsan
 */
public class Material {
	
	private String name = "";
	
	@Deprecated
	protected int positionIndex, normalIndex, texcoordIndex, tangentIndex, binormalIndex;
	@Deprecated
	protected Shader shader;
	protected Color ambient, diffuse, specular;
	
	protected boolean writesDepthBuffer = true;
	protected boolean checksDepthBuffer = true;
	protected boolean ignoreLights = false;
	
	protected float specularIntensity = 1;
	protected int specularPower = 1;
	
	protected Texture diffuseMap = null;
	protected Texture normalMap = null;
	
	@Deprecated
	protected List<MaterialComponent> components = new ArrayList<MaterialComponent>();
	
	public Material() {
		this.ambient = Color.WHITE;
		this.diffuse = Color.WHITE;
		this.specular = Color.WHITE;
	}
	
	@Deprecated
	public Material(Shader shader) {
		this("unnamed material", shader);
	}
	
	@Deprecated
	public Material(String name, Shader shader) {
		this(shader, name, Color.WHITE, Color.WHITE, Color.WHITE);
	}
	
	@Deprecated
	public Material(Shader shader, String name, Color ambient, Color diffuse, Color specular) {
		this.shader = shader;
		this.name = name;
		
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
	
	@Deprecated
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

	@Deprecated
	protected void enableShader(RendererState rendererState) {
		if(null == shader) {
			Yeti.screwed("Tried to enable null shader in material: " + getName());
		}
		rendererState.gl.glUseProgram(shader.getHandle());
	}
	
	@Deprecated
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

	@Deprecated
	public void addComponent(MaterialComponent component) {
		components.add(component);
	}

	@Deprecated
	public void removeComponent(MaterialComponent component) {
		if(!components.remove(component)) {
			Yeti.screwed("Tried to remove non-existing material component!");
		}
	}
	
	@Deprecated
	public boolean containsComponent(MaterialComponent component) {
		return components.contains(component);
	}
	
	@Deprecated
	public Shader getShader() {
		return shader;
	}

	public float getSpecularIntensity() {
		return specularIntensity;
	}

	public void setSpecularIntensity(float specularIntensity) {
		this.specularIntensity = specularIntensity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
