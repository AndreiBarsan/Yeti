/**
 * 
 */
package barsan.opengl.rendering.materials;

import javax.media.opengl.GL2;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Model;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;

import com.jogamp.opengl.util.texture.Texture;

/**
 * @author Andrei Bârsan
 *
 */
public class ToonMaterial extends BasicMaterial {

	enum EdgeTechnique {
		/**
		 * This technique simply draws a solid black flat version of the 
		 * mesh with the GL_LINES polygon mode first, and the actual colors
		 * afterwards.
		 */
		SeparateLines,
		
		/**
		 * This method darkens fragments whose normals are perpendicular
		 * to the camera viewing direction.
		 * 
		 * TODO: fix, bugged at the moment
		 */
		EnhanceContour,
		
		/**
		 * This technique skips the drawing of the edges entierly. This should
		 * be activated when the whole scene is rendered with contours enabled,
		 * and when the contours are, thus, rendered as a postprocess effect
		 * using an edge detection algorithm on the z-buffer.
		 */
		PostProcessOnly
	}
	
	EdgeTechnique et = EdgeTechnique.SeparateLines;
	
	NormalDisplacementMaterial fm = new NormalDisplacementMaterial(Color.BLACK);
	
	public ToonMaterial(Texture tex) {
		setTexture(tex);
		shader = ResourceLoader.shader("cel");
	}
	
	public ToonMaterial(Color color) {
		super(Color.WHITE, color, Color.WHITE);
		shader = ResourceLoader.shader("cel");
	}
	
	@Override
	public void setup(RendererState rendererState, Matrix4 modelMatrix) {
		super.setup(rendererState, modelMatrix);
		
		Vector3 camDir = rendererState.getCamera().getDirection();
		fm.setup(rendererState, modelMatrix);
		
		//smoothShader.setU1i("useEdgeContour", 0);
		switch (et) {
				
			case SeparateLines:
				
				break;
				
			case EnhanceContour:
				shader.setUVector3f("cameraDirection", camDir);
				shader.setU1i("useEdgeContour", true);
				break;
				
			case PostProcessOnly:
				break;
					
		}
		
		
	}
	
	void doFlatAndLines(RendererState rs, Model model) {
		GL2 gl = Yeti.get().gl.getGL2();
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
		gl.glLineWidth(2.5f);
		//gl.glPolygonOffset(2.5f, 2.5f);
		gl.glFrontFace(GL2.GL_CW);
		
		// Apparently smooth lines are *really* expensive as they are almost
		// always done through software fallback, since only a handful of
		// CAD-specific cards actually natively support them. :(
		//gl.glEnable(GL2.GL_LINE_SMOOTH);
		//gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
		fm.render(rs, model);
		//gl.glDisable(GL2.GL_BLEND);
		
		// Reset state
		gl.glFrontFace(GL2.GL_CCW);
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
	}
	
	@Override
	public void render(RendererState rendererState, Model model) {
		
		super.render(rendererState, model);
		switch (et) {
		
		case SeparateLines:
			doFlatAndLines(rendererState, model);
			break;
			
		case EnhanceContour:
			break;
			
		case PostProcessOnly:
			break;
			
		}
		
		
		
	}
}
