package barsan.opengl.rendering;

import java.nio.ByteBuffer;

import javax.media.opengl.GL2;

import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Model.Face;
import barsan.opengl.resources.ResourceLoader;

import com.jogamp.opengl.util.texture.Texture;

/**
 * @author Andrei Bârsan
 *
 */
public class Billboard extends ModelInstance {
	
	public enum AxisClamp {
		None,
		ClampX
	}
	
	static class BillboardMaterial extends Material {
	
		static final String SHADER_NAME = "billboard";
		private AxisClamp axisClamp;
		
		public BillboardMaterial(AxisClamp axisClamp) {
			super(ResourceLoader.shader(SHADER_NAME));
			ignoreLights = true;
			writesDepthBuffer = false;
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
			}
			
			Matrix4 mvp = new Matrix4(projection).mul(viewModel);
	
			enableShader(rendererState);
			shader.setUMatrix4("mvpMatrix", mvp);
			shader.setU1i("colorMap", 0);
			texture.bind(rendererState.getGl());
		}
	}
	
	private BillboardMaterial b_ref;
	
	// TODO: warning! don't forget to make sure this works with nesting as well!
	// The user should be able to have (actual) models as kids of billboards
	// and vice-versa.
	public Billboard(GL2 gl, Texture texture) {
		this(gl, texture, new Matrix4());
	}
	
	public Billboard(GL2 gl, Texture texture, Matrix4 transform) {
		super(new Model(gl, "billboard_tex{" + texture + "}"), new BillboardMaterial(AxisClamp.None), transform);
		
		getMaterial().setTexture(texture);
		
		b_ref = (BillboardMaterial)getMaterial();
		
		Face mainFace = new Face();
		mainFace.points = new Vector3[] {
				new Vector3( 1.0f, -1.0f, 0.0f),
				new Vector3( 1.0f, 1.0f,  0.0f),
				new Vector3(-1.0f, 1.0f,  0.0f),
				new Vector3(-1.0f, -1.0f, 0.0f)
		};
		
		//*
		mainFace.texCoords = new Vector3[] {
				new Vector3(0.0f, 0.0f, 0.0f),
				new Vector3(0.0f, 1.0f, 0.0f),
				new Vector3(1.0f, 1.0f, 0.0f),
				new Vector3(1.0f, 0.0f, 0.0f)
		};
		//*/
		model.master.faces.add(mainFace);
		model.setPointsPerFace(4);
		model.buildVBOs();
	}
	
	@Override
	public void render(RendererState rendererState, Matrix4Stack transformStack) {
		GL2 gl = rendererState.getGl();

		ByteBuffer out = ByteBuffer.allocate(1);
		gl.glGetBooleani_v(GL2.GL_BLEND, 0, out);
		
		gl.glEnable(GL2.GL_BLEND);
		// TODO: blending manager that has a stack of states
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		super.render(rendererState, transformStack);
		
		if(out.get(0) != 1) {
			// Only disable if blending had been disabled before
			gl.glDisable(GL2.GL_BLEND);
		}
	}
	
	public AxisClamp getAxisClamp() {
		return b_ref.axisClamp;
	}
	
	public void setAxisClamp(AxisClamp value) {
		b_ref.axisClamp = value;
	}

}
