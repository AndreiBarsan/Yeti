package barsan.opengl.rendering;

import com.jogamp.opengl.GL;

import barsan.opengl.math.Vector3;
import barsan.opengl.resources.Face;
import barsan.opengl.resources.ModelLoader;

public class Cube extends StaticModel {
	
	public Cube(GL gl, float f) {
		this(gl, f, false);
	}

	public Cube(GL gl, float f, boolean insideOut) {
		super(gl, "procedural cube");
		
		float hs = f / 2;
		
		Vector3 down = 	new Vector3(0, -1, 0);
		Vector3 up = 	new Vector3(0, 1, 0);
		Vector3 left = 	new Vector3(1, 0, 0);
		Vector3 right = new Vector3(-1, 0, 0);
		Vector3 front = new Vector3(0, 0, 1);
		Vector3 back = 	new Vector3(0, 0, -1);
		
		if(!insideOut) {
			Face face = new Face();
			face.points = new Vector3[] {
					new Vector3( hs, -hs, -hs),
					new Vector3( hs, -hs,  hs),
					new Vector3(-hs, -hs,  hs),
					new Vector3(-hs, -hs, -hs)				
			};
			face.normals = new Vector3[] { down, down, down, down };
			master.faces.add(face);
			
			face = new Face();
			face.points = new Vector3[] {
					new Vector3(-hs,  hs, -hs),
					new Vector3(-hs,  hs,  hs),
					new Vector3( hs,  hs,  hs),
					new Vector3( hs,  hs, -hs)
			};
			face.normals = new Vector3[] { up, up, up, up };
			master.faces.add(face);
			
			face = new Face();
			face.points = new Vector3[] {
					new Vector3(hs,  hs, -hs),
					new Vector3(hs,  hs,  hs),
					new Vector3(hs, -hs,  hs),
					new Vector3(hs, -hs, -hs)
			};
			face.normals = new Vector3[] { left, left, left, left };
			master.faces.add(face);
			
	
			face = new Face();
			face.points = new Vector3[] {
					new Vector3(-hs, -hs, -hs),
					new Vector3(-hs, -hs,  hs),
					new Vector3(-hs,  hs,  hs),
					new Vector3(-hs,  hs, -hs)
			};
			face.normals = new Vector3[] { right, right, right, right };
			master.faces.add(face);
			
			face = new Face();
			face.points = new Vector3[] {
					new Vector3( hs, -hs, hs),
					new Vector3( hs,  hs, hs),
					new Vector3(-hs,  hs, hs),
					new Vector3(-hs, -hs, hs)
			};
			face.normals = new Vector3[] { front, front, front, front };
			master.faces.add(face);
			
			face = new Face();
			face.points = new Vector3[] {
					new Vector3(-hs, -hs, -hs),
					new Vector3(-hs,  hs, -hs),
					new Vector3( hs,  hs, -hs),
					new Vector3( hs, -hs, -hs)
			};
			face.normals = new Vector3[] { back, back, back, back };
			master.faces.add(face);
		} else {
			Face face = new Face();
			face.points = new Vector3[] {
					new Vector3(-hs, -hs, -hs),
					new Vector3(-hs, -hs,  hs),
					new Vector3( hs, -hs,  hs),
					new Vector3( hs, -hs, -hs)			
			};
			face.normals = new Vector3[] { down, down, down, down };
			master.faces.add(face);
			
			face = new Face();
			face.points = new Vector3[] {
					new Vector3( hs,  hs, -hs),
					new Vector3( hs,  hs,  hs),
					new Vector3(-hs,  hs,  hs),
					new Vector3(-hs,  hs, -hs),					
			};
			face.normals = new Vector3[] { up, up, up, up };
			master.faces.add(face);
			
			face = new Face();
			face.points = new Vector3[] {
					new Vector3(hs, -hs, -hs),
					new Vector3(hs, -hs,  hs),
					new Vector3(hs,  hs,  hs),
					new Vector3(hs,  hs, -hs)
			};
			face.normals = new Vector3[] { left, left, left, left };
			master.faces.add(face);
			
	
			face = new Face();
			face.points = new Vector3[] {
					new Vector3(-hs,  hs, -hs),
					new Vector3(-hs,  hs,  hs),
					new Vector3(-hs, -hs,  hs),
					new Vector3(-hs, -hs, -hs)
			};
			face.normals = new Vector3[] { right, right, right, right };
			master.faces.add(face);
			
			face = new Face();
			face.points = new Vector3[] {
					new Vector3(-hs, -hs, hs),
					new Vector3(-hs,  hs, hs),
					new Vector3( hs,  hs, hs),
					new Vector3( hs, -hs, hs)					
			};
			face.normals = new Vector3[] { front, front, front, front };
			master.faces.add(face);
			
			face = new Face();
			face.points = new Vector3[] {
					new Vector3( hs, -hs, -hs),
					new Vector3( hs,  hs, -hs),
					new Vector3(-hs,  hs, -hs),
					new Vector3(-hs, -hs, -hs)
			};
			face.normals = new Vector3[] { back, back, back, back };
			master.faces.add(face);
		}
		
		// Build faces as quads
		setPointsPerFace(4);
		

		boolean old = ModelLoader.loadingFronLHCoords;
		ModelLoader.loadingFronLHCoords = false;
		
		buildVBOs();
		
		ModelLoader.loadingFronLHCoords = old;
	}

}
