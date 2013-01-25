package barsan.opengl.rendering.materials;

import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.RendererState;

public class GammaCorrection implements MaterialComponent {

	public static final float DEFAULT_GAMMA = 2.2f;
	
	private float gamma;
	private float invGamma;
	
	public GammaCorrection() {
		this(DEFAULT_GAMMA);
	}
	
	public GammaCorrection(float gamma) {
		setGamma(gamma);
	}
	
	@Override
	public void setup(Material m, RendererState rs, Matrix4 modelMatrix) {
		m.shader.setU1i("useGammaCorrection", true);	
		m.shader.setU1f("invGamma", invGamma);
	}

	@Override
	public int setupTexture(Material m, RendererState rs, int slot) {
		return 0;
	}

	@Override
	public void cleanUp(Material m, RendererState rs) {
		m.shader.setU1i("useGammaCorrection", false);		
	}

	@Override
	public void dispose() { }
	

	public float getGamma() {
		return gamma;
	}

	public void setGamma(float gamma) {
		this.gamma = gamma;
		this.invGamma = 1.0f / gamma;
	}
}
