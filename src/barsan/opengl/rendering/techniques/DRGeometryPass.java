package barsan.opengl.rendering.techniques;

import com.jogamp.opengl.GL2;

import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.rendering.StaticModelInstance;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.resources.ResourceLoader;

import com.jogamp.opengl.util.texture.Texture;

public class DRGeometryPass extends Technique {

	// Where we start mount the items' texture (diffuse/bump/ etc.)
	private int diffuseMapSlot;
	private int normalMapSlot;
	
	public DRGeometryPass(int textureSlotStart) {
		super(ResourceLoader.shader("DRGeometry"));
		
		diffuseMapSlot = textureSlotStart;
		normalMapSlot = textureSlotStart + 1;
	}

	@Override
	public void setup(RendererState rs) {
		super.setup(rs);
		
		view.set(rs.getCamera().getView());
		projection.set(rs.getCamera().getProjection());
	}
	
	private void bindTexture(RendererState rs, Texture t, String name, int slot) {
		rs.gl.glActiveTexture(GL2.GL_TEXTURE0 + slot);
		t.bind(rs.gl);
		int aiso = rs.getAnisotropySamples();
		t.setTexParameterf(rs.gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, aiso);
		program.setU1i(name, slot);
	}
	
	@Override
	public void loadMaterial(RendererState rs, Material material) {
		Texture normalMap = material.getNormalMap();
		if(normalMap != null) {
			program.setU1i("useBump", true);
			// Note: having this disabled means no non-uniform scaling is allowed
			// program.setUMatrix3("normalMatrix", MathUtil.getNormalTransform(viewModel));
			bindTexture(rs, normalMap, "normalMap", normalMapSlot);
		} else {
			program.setU1i("useBump", false);
		}
		
		Texture diffuseMap = material.getDiffuseMap();
		if(diffuseMap != null) {
			program.setU1i("useTexture", true);
			bindTexture(rs, diffuseMap, "diffuseMap", diffuseMapSlot);
		} else {
			program.setU1i("useTexture", false);
		}
		
		program.setUVector4f("matDiffuse", material.getDiffuse().getData());
		program.setU1f("matSpecularIntensity", (float) material.getSpecularIntensity());
		program.setU1f("matSpecularPower", (float) material.getSpecularPower());
	}
	
	@Override
	protected void instanceRenderSetup(ModelInstance mi, RendererState rs, Matrix4Stack matrixStack) {
		
	}
	
}
