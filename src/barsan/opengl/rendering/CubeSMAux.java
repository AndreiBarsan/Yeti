package barsan.opengl.rendering;

import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Vector3;

public class CubeSMAux {

	public static final Matrix4[] rotMatrices = new Matrix4[] {
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
	
	public static Matrix4	am 				= new Matrix4();
	public static Matrix4	am2				= new Matrix4();
	public static Matrix4[]	pViewMatrices 	= new Matrix4[6];
	static {
		assert rotMatrices.length == 6 : "Hurf durf; enough programming for today! go write some PHP.";
		for(int i = 0; i < 6; i++) {
			pViewMatrices[i] = new Matrix4();
		}
	}
	
	public static void setup(Vector3 lightPosition, Matrix4 projection) {
		for(int i = 0; i < CubeSMAux.pViewMatrices.length; i++) {
			CubeSMAux.am.set(CubeSMAux.rotMatrices[i]);
			CubeSMAux.am.mul(CubeSMAux.am2.setTranslate(lightPosition.copy().inv()));
			
			CubeSMAux.pViewMatrices[i].set(projection);
			CubeSMAux.pViewMatrices[i].mul(CubeSMAux.am);
		}
	}
}
