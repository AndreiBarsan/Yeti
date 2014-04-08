package barsan.opengl.resources;

import java.util.ArrayList;
import java.util.List;

import barsan.opengl.math.Vector3;

/**
 * Signifies a model's face. Noteworthy is the fact that this object only bears
 * real significance as a mesh is being built or loaded. After the VBOs are baked,
 * the VBOs are the ones used for the actual rendering logic, NOT the face objects.
 * 
 * @author Andrei Bârsan
 */
public class Face {
	public Vector3[] points;
	public Vector3[] texCoords;
	public Vector3[] normals;
	public Vector3[] tangents;
	public Vector3[] binormals;
	
	/** Old indices that were used to build this face. Used for debugging. */
	public int[] pindex, tcindex, nindex, tindex;
	
	public void computeTangBinorm() {
		tangents = new Vector3[normals.length];
		binormals = new Vector3[normals.length];
		
		for(int i = 0; i < normals.length; ++i) {
			Vector3 normal = normals[i];
			if(normal == null) {
				assert false : "Cannot compute tangents and binormals since the normals aren't set.";
			}
			Vector3 t = new Vector3(-normal.z, 0, normal.x).normalize();
			if(normal.z == normal.x) {
				t.set(1.0f, 0.0f, 0.0f);
			}
							
			tangents[i] = t;
			/**
			 * 8 Apr 2014 - switched the operator order because bump mapping was
			 * broken after updating to JOGL 2.0.2 from 2.0.0.
			 */
			binormals[i] = new Vector3(normal).cross(t).normalize();
		}
	}
	
	/**
	 * Splits a quad face into two triangles. Assumes tangents and binormals
	 * haven't been computed yet.
	 */
	public List<Face> split() {
		assert points.length == 4 : "Can only split quads!";
		ArrayList<Face> out = new ArrayList<Face>();
		
		Face f1 = new Face();
		Face f2 = new Face();
		f1.points = Vector3.copyOfIndices(points, 0, 1, 2);
		f2.points = Vector3.copyOfIndices(points, 2, 1, 3);
		
		if(null != normals) {
			f1.normals = Vector3.copyOfIndices(normals, 0, 1, 2);
			f2.normals = Vector3.copyOfIndices(normals, 2, 1, 3);
		}
		
		if(null != texCoords) {
			f1.texCoords = Vector3.copyOfIndices(texCoords, 0, 1, 2);
			f2.texCoords = Vector3.copyOfIndices(texCoords, 2, 1, 3);
		}
		
		out.add(f1);
		out.add(f2);
		
		return out;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < 3; i++) {
			sb.append(String.format("%d/%d/%d ", pindex[i], tcindex[i], nindex[i]));
		}
		return sb.toString();
	}
}