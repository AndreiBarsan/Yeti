package barsan.opengl.rendering;

import java.util.List;

import javax.media.opengl.GL2;

import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.resources.ResourceLoader;

import com.jogamp.opengl.util.texture.Texture;

public class DRGeometryPass extends Technique {

	// Where we start mount the items' texture (diffuse/bump/ etc.)
	private int textureSlotStart;
	private int diffuseSlot;
	private int bumpSlot;
	
	public DRGeometryPass(int textureSlotStart) {
		super(ResourceLoader.shader("DRGeometry"));
		
		this.textureSlotStart = textureSlotStart;
		diffuseSlot = textureSlotStart;
		bumpSlot = textureSlotStart + 1;
	}

	@Override
	public void setup(RendererState rs) {
		super.setup(rs);
		
		view.set(rs.getCamera().getView());
		projection.set(rs.getCamera().getProjection());
	}
	
	private void bindTexture(RendererState rs, Texture t, int slot) {
		t.setTexParameterf(rs.gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, rs.getAnisotropySamples());
		rs.gl.glActiveTexture(GL2.GL_TEXTURE0 + diffuseSlot);
		t.bind(rs.gl);
		program.setU1i("colorMap", diffuseSlot);
	}
	
	@Override
	protected void instanceRenderSetup(ModelInstance mi, RendererState rs, Matrix4Stack matrixStack) {
		Texture t = mi.getMaterial().getTexture();
		if(t != null) {
			program.setU1i("useTexture", true);
			bindTexture(rs, t, diffuseSlot);
		} else {
			program.setU1i("useTexture", false);
		}
		
		program.setUVector4f("matDiffuse", mi.getMaterial().getDiffuse().getData());
	}
}
