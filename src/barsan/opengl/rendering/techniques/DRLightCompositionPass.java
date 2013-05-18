package barsan.opengl.rendering.techniques;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import barsan.opengl.Yeti;
import barsan.opengl.math.Vector2;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Settings;

import com.jogamp.opengl.util.texture.Texture;

public class DRLightCompositionPass extends Technique {

	/** TODO: fancier way of setting these */
	public float aoScale = 1.0f;
	public float aoBias = 0.25f;
	public float asSampleRad = 2.0f;	
	public float aoIntensity = 5.33f;
	

	public DRLightCompositionPass() {
		super(ResourceLoader.shader("DRLightCompose"));
		
		AOInit();
	}
	
	private void AOInit() {
		ResourceLoader.loadTexture("randomNormal", "randomNormal.jpg");
		Texture t = ResourceLoader.texture("randomNormal");
		GL2 gl = Yeti.get().gl;
		t.setTexParameterf(gl, parameterName, value)
	}
	
	@Override
	public void setup(RendererState rs) {
		super.setup(rs);
		
		/* No need for instanceRenderSetup, we are only rendering 1 quad/frame */
		program.setU1i("diffuseMap", 0);
		program.setU1i("lightMap", 1);
		
		aoScale = 0.5f;
		aoBias = 0.33f;
		asSampleRad = 0.5f;	
		aoIntensity = 2.0f;
		
		AOSetup(rs);
	}
	
	private void AOSetup(RendererState rs) {
		program.setU1i("normalMap", 2);
		program.setU1i("positionMap", 3);
		
		Texture randomTexture = ResourceLoader.texture("randomNormal");
		
		rs.gl.glActiveTexture(GL.GL_TEXTURE0 + 4);
		rs.gl.glBindTexture(GL.GL_TEXTURE_2D, randomTexture.getTextureObject(rs.gl));
		program.setU1i("randomNormalMap", 4);
		
		Settings s = Yeti.get().settings;
		program.setUVector2f("screenSize", new Vector2(s.width, s.height));
		program.setU1f("randomSize", randomTexture.getWidth());
		
		program.setU1f("aoScale", aoScale);
		program.setU1f("aoBias", aoBias);
		program.setU1f("aoSampleRad", asSampleRad);
		program.setU1f("aoIntensity", aoIntensity);
	}
}
