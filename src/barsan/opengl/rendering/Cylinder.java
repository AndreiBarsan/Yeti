package barsan.opengl.rendering;

import javax.media.opengl.GL2;

import barsan.opengl.math.Vector3;
import barsan.opengl.resources.ModelLoader.Face;

public class Cylinder extends StaticModel {

	public Cylinder(GL2 gl, int precision, float diameter, float height) {
		super(gl, "procedural cylinder");
		assert(precision >= 3);
		
		generateDisk(precision, diameter, 0, 		-1.0f);
		generateBody(precision, diameter, 0, height);
		generateDisk(precision, diameter, height, 	 1.0f);
		
		setPointsPerFace(3);
		
		buildVBOs();
	}
	
	private void generateDisk(int precision, float diameter, float y, float up) {
		double step = (Math.PI * 2) / (double)precision * up; 
		double r = diameter / 2.0d;
		double angle = 0.0d;
		
		for(int i = 0; i < precision; i++) {
			Face face = new Face();
			face.points = new Vector3[] {
				new Vector3(0.0f, y, 0.0f),
				new Vector3((float) (Math.cos(angle) * r), y, (float) (-Math.sin(angle) * r)),
				new Vector3((float) (Math.cos(angle + step) * r), y, (float) (-Math.sin(angle + step) * r))
			};
			face.normals = new Vector3[] {
				new Vector3(0.0f, up, 0.0f),
				new Vector3(0.0f, up, 0.0f),
				new Vector3(0.0f, up, 0.0f)
			};
			angle += step;
			master.faces.add(face);
		}
	}
	
	private void generateBody(int precision, float diameter, float low, float high) {
		double step = (Math.PI * 2) / (double)precision; 
		double r = diameter / 2.0d;
		double angle = 0.0d;
		
		
		for(int i = 0; i < precision; i++) {
			Face face = new Face();
			double ca = Math.cos(angle);
			double sa = -Math.sin(angle);
			double cas = Math.cos(angle + step);
			double sas = -Math.sin(angle + step);
			face.points = new Vector3[] {
				new Vector3((float) (ca * r), high, (float) (sa * r)),
				new Vector3((float) (cas * r), low, (float) (sas * r)),
				new Vector3((float) (cas * r), high, (float) (sas * r))
			};
			face.normals = new Vector3[] {
					new Vector3((float) (ca * r), 0, (float) (sa * r)),
					new Vector3((float) (cas * r), 0, (float) (sas * r)),
					new Vector3((float) (cas * r), 0, (float) (sas * r))
			};
			master.faces.add(face);
			face = new Face();
			face.points = new Vector3[] {
					new Vector3((float) (ca * r), low, (float) (sa * r)),
					new Vector3((float) (cas * r), low, (float) (sas * r)),
					new Vector3((float) (ca * r), high, (float) (sa * r)),
			};
			face.normals = new Vector3[] {
					new Vector3((float) (ca * r), 0, (float) (sa * r)),
					new Vector3((float) (cas * r), 0, (float) (sas * r)),
					new Vector3((float) (ca * r), 0, (float) (sa * r)),
				};
			angle += step;
			master.faces.add(face);
		}
	}

}
