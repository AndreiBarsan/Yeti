package barsan.opengl.rendering.materials;

import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.resources.ResourceLoader;

public class DepthWriterPoint extends Material {

	private static class DWPComponent implements MaterialComponent {
		
		private Vector3 lightPosition;
		
		/** Far plane used when rendering the depth component. */
		private float far;
		private Matrix4 projection;
		
		static Matrix4[] rotMatrices = new Matrix4[] {
			new Matrix4(new float[] {	// x+
				0, 0, -1, 0,
				0, -1, 0, 0,
				-1, 0, 0, 0,
				0, 0, 0, 1
			}),
			
			new Matrix4(new float[] {	// x-
				0, 0, 1, 0,
				0, -1, 0, 0,
				1, 0, 0, 0,
				0, 0, 0, 1
			}),			
			
			new Matrix4(new float[] {	// y- !!!
				1, 0, 0, 0,
				0, 0, -1, 0,
				0, 1, 0, 0,
				0, 0, 0, 1
			}),
			
			new Matrix4(new float[] {	// y+ !!!
				1, 0, 0, 0,
				0, 0, 1, 0,
				0, -1, 0, 0,
				0, 0, 0, 1
			}),
			
			new Matrix4(new float[] {	// z+
				1, 0, 0, 0,
				0, -1, 0, 0,
				0, 0, -1, 0,
				0, 0, 0, 1
			}),
			
			new Matrix4(new float[] {	// z-
				-1, 0, 0, 0,
				0, -1, 0, 0,
				0, 0, +1, 0,
				0, 0, 0, 1
			})
		};
		
		
		
		static Matrix4		am 				= new Matrix4();
		static Matrix4		am2				= new Matrix4();
		static Matrix4[]	pViewMatrices 	= new Matrix4[6];
		static {
			assert rotMatrices.length == 6 : "Hurf durf; enough programming for today! go write some PHP.";
			for(int i = 0; i < 6; i++) {
				pViewMatrices[i] = new Matrix4();
			}
		}
		
		public DWPComponent(Vector3 lightPosition, float near, float far) {
			super();
			this.lightPosition = lightPosition;
			this.far = far;
			
			projection = new Matrix4().setPerspectiveProjection(90, 1, near, far);
		}
		
		@Override
		public void setup(Material m, RendererState rs, Matrix4 modelMatrix) {
			
			for(int i = 0; i < pViewMatrices.length; i++) {
				am.set(rotMatrices[i]);
				am.mul(am2.setTranslate(lightPosition.copy().inv()));
				
				pViewMatrices[i].set(projection);
				pViewMatrices[i].mul(am);
			}
			
			m.shader.setUMatrix4a("vpMatrices", pViewMatrices);
			m.shader.setUMatrix4("mMatrix", modelMatrix);
			m.shader.setUVector4f("lightPos_wc", new float[] { lightPosition.x, lightPosition.y, lightPosition.z, 1.0f });
			m.shader.setU1f("far", far);
		}

		@Override
		public int setupTexture(Material m, RendererState rs, int slot) {
			return 0;
		}

		@Override
		public void cleanUp(Material m, RendererState rs) {
			
		}

		@Override
		public void dispose() { }
		
	}
	
	public DepthWriterPoint(Vector3 lightPosition, float near, float far) {
		super(ResourceLoader.shader("depthWriterPoint"));
		addComponent(new DWPComponent(lightPosition, near, far));
		
		ignoreLights = true;
	}
}
