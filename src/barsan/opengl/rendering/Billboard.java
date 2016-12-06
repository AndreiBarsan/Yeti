package barsan.opengl.rendering;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.math.Transform;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.resources.ModelLoader;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;

import com.jogamp.opengl.util.texture.Texture;

/**
 * @author Andrei B�rsan
 *
 */
public class Billboard extends StaticModelInstance {
	
	public enum AxisClamp {
		None,
		ClampX,
		ClampAll
	}
		
	static class BillboardMaterial extends Material {
	
		static final String SHADER_NAME = "billboard";
		private AxisClamp axisClamp;
		
		public BillboardMaterial(AxisClamp axisClamp, String name) {
			super(ResourceLoader.shader(SHADER_NAME), name, Color.WHITE, Color.WHITE, Color.WHITE);
			ignoreLights = true;
			this.axisClamp = axisClamp;
		}
	
		@Override
		public void setup(RendererState rendererState, Matrix4 modelMatrix) {
			Matrix4 view = rendererState.getCamera().getView();
			Matrix4 projection = rendererState.getCamera().getProjection();
			Matrix4 viewModel = new Matrix4(view).mul(modelMatrix);
	
			/*
			 * Much, much faster than mucking around with the quad coords. That produces
			 * several rather complex computations per vertex, while this one just
			 * a bunch of variable sets per quad!
			 * 
			 * This would be very good for smoke, for instance. Not very good
			 * for trees, however, which need to be axis-aligned.
			 */
			switch(axisClamp) {
				case ClampX:
					viewModel.clearRotationX();
					break;
					
				case None:
					viewModel.clearRotation();
					break;
					
				case ClampAll:
					break;
			}
			
			Matrix4 mvp = new Matrix4(projection).mul(viewModel);
	
			enableShader(rendererState);
			shader.setUMatrix4("mvpMatrix", mvp);
			GL gl = Yeti.get().gl;
			gl.glActiveTexture(GL.GL_TEXTURE0);
			shader.setU1i("colorMap", 0);
			diffuseMap.bind(rendererState.gl);
		}
		
		@Override
		public void cleanUp(RendererState rendererState) {
			// nop
		}
	}
	
	protected BillboardMaterial b_ref;
	
	// TODO: warning! don't forget to make sure this works with nesting as well!
	// The user should be able to have (actual) models as kids of billboards
	// and vice-versa. Wait, do poly kids of 2d billboards make sense? Kind of,
	// their parent transform would be flattened - so they would look right.
	public Billboard(GL gl, Texture texture, String name) {
		this(gl, texture, new Transform(), name);
	}
	
	// TODO: use point sprites!
	public Billboard(GL gl, Texture texture, Transform transform, String name) {
		super(ModelLoader.buildQuadXY(texture.getAspectRatio(), 1.0f), new BillboardMaterial(AxisClamp.None, name + " billboard material"), transform);
		
		getMaterial().setDiffuseMap(texture);
		
		b_ref = (BillboardMaterial)getMaterial();
	}
	
	@Override
	public void render(RendererState rendererState, Matrix4Stack transformStack) {
		// Previous state changes are no longer needed; billboards are now
		// getting rendered completely separately and the renderer handles the
		// state changes itself.
		super.render(rendererState, transformStack);
	}
	
	public AxisClamp getAxisClamp() {
		return b_ref.axisClamp;
	}
	
	public void setAxisClamp(AxisClamp value) {
		b_ref.axisClamp = value;
	}

}
