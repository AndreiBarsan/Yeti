package barsan.opengl.rendering.techniques;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL4;

import barsan.opengl.Yeti;
import barsan.opengl.math.Vector2;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Settings;

import com.jogamp.opengl.util.texture.Texture;

public class DRLightCompositionPass extends Technique {

	public class AOSettings {
		public float scale = 1.0f;
		public float bias = 0.25f;
		public float sampleRad = 0.075f;	
		public float intensity = 0.25f;
	}
	
	public AOSettings ao = new AOSettings();

	public DRLightCompositionPass() {
		super(ResourceLoader.shader("DRLightCompose"));
		AOInit();
	}
	
	private void AOInit() {
		ResourceLoader.loadTexture("randomNormal", "randomNormal.jpg");
	}
	
	@Override
	public void setup(RendererState rs) {
		super.setup(rs);
		
		/* No need for instanceRenderSetup, we are only rendering 1 quad/frame */
		program.setU1i("diffuseMap", 0);
		program.setU1i("lightMap", 1);
		
		AOSetup(rs);
	}
	
	@Override
	public void loadMaterial(RendererState rs, Material material) {
		// Nop, this is a pass that doesn't involve materials
		// TODO: normal techniques and abstract techniques or something?
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
		
		program.setU1f("aoScale", ao.scale);
		program.setU1f("aoBias", ao.bias);
		program.setU1f("aoSampleRad", ao.sampleRad);
		program.setU1f("aoIntensity", ao.intensity);
		
		int ph = program.getHandle();
		
		GL4 gl4 = (GL4) rs.gl;
		if(null == gl4) {
			Yeti.screwed("GLSL subroutine fetching and manipulation requires GL4.");
		}
		
		int sr = gl4.glGetSubroutineIndex(ph, GL2.GL_FRAGMENT_SHADER, "normalAO");
		gl4.glUniformSubroutinesuiv(GL2.GL_FRAGMENT_SHADER,
				1,
				new int[] { sr },
				0
			);
	}
}
