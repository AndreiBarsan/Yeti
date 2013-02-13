package barsan.opengl.rendering;

import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import barsan.opengl.Yeti;
import barsan.opengl.resources.ModelLoader.Face;
import barsan.opengl.resources.ModelLoader.Group;

/**
 * @author Andrei Barsan
 */
public class StaticModel {
	
	public static final int COORDS_PER_POINT = 3;
	public static final int T_COORDS_PER_POINT = 2;

	// Vertex buffers for faster rendering
	private VBO vertices;
	private VBO texcoords;
	private VBO normals;
	
	protected HashMap<String, Group> groups = new HashMap<>();
	public Group master = new Group();
	
	private String name;
	protected final GL gl;
	
	/** Whether GL_TRIANGLES or GL_QUADS is being used. */
	private int faceMode;

	/** Actual number of vertices per triangle. */
	private int pointsPerFace;
	
	public StaticModel(GL gl, String name) {
		this.gl = gl;
		this.name = name;
		this.pointsPerFace = 3; // default
		groups.put("default", new Group());
	}
	
	static float uv[] = new float[2];
	public void buildVBOs() {
		assert master.faces.size() > 0 : "Empty model";
		// Tried & tested - this is just the right buffer length
		int size = master.faces.size() *  pointsPerFace;
		
		vertices = new VBO(GL2.GL_ARRAY_BUFFER, size, COORDS_PER_POINT);
		normals = new VBO(GL2.GL_ARRAY_BUFFER, size, COORDS_PER_POINT);
		texcoords = new VBO(GL2.GL_ARRAY_BUFFER, size, T_COORDS_PER_POINT);
		//tangents = new VBO(GL2.GL_ARRAY_BUFFER, size, COORDS_PER_POINT);
		//bitangents = new VBO(GL2.GL_ARRAY_BUFFER, size, COORDS_PER_POINT);
		
		vertices.open();
		for(Face f : master.faces) {
			for(int i = pointsPerFace - 1; i >= 0; i--) {
				vertices.append(f.points[i]);
			}
		}
		vertices.close();
		
		if(master.faces.get(0).normals != null) {
			normals.open();
			for(Face f : master.faces) {
				for(int i = pointsPerFace - 1; i >= 0; i--) {
					normals.append(f.normals[i]);
				}
			}
			normals.close();
		}
		
		if(master.faces.get(0).texCoords != null) {
			texcoords.open();
			for(Face f : master.faces) {
				for(int i = pointsPerFace - 1; i >= 0; i--) {
					uv[0] = f.texCoords[i].x;
					uv[1] = f.texCoords[i].y;
					texcoords.append(uv);
				}
			}
			texcoords.close();
		}
		
		Yeti.debug(String.format("VBOs for \"%s\" built. Normal element count: %d; Geometry element count: %d",
				getName(), vertices.getSize(), normals.getSize()));
	}
	
	public void addFace(Face face) {
		addFace("default", face);
	}
	
	public void addFace(String groupName, Face face) {
		groups.get(groupName).faces.add(face);
		master.faces.add(face);
	}
	
	public void render() {
		gl.glDrawArrays(faceMode, 0, vertices.getSize());
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
	}
	
	public void dispose() {
		gl.glDeleteBuffers(3, new int[] { 	vertices.getHandle(), 
											normals.getHandle(),
											texcoords.getHandle(),
										}, 0);
	}
	
	public VBO getVertices() {
		return vertices;
	}
	
	public VBO getNormals() {
		return normals;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public VBO getTexcoords() {
		return texcoords;
	}

	public int getFaceMode() {
		return faceMode;
	}
		
	public void setFaceMode(int faceMode) {
		this.faceMode = faceMode;
	}

	public Map<String, Group> getGroups() {
		return groups;
	}
	
	/** @note Also updates faceMode */
	public void setPointsPerFace(int pointsPerFace) {
		this.pointsPerFace = pointsPerFace;
		if(pointsPerFace == 3) {
			faceMode = GL2.GL_TRIANGLES;
		} else if(pointsPerFace == 4) {
			faceMode = GL2.GL_QUADS;
		} else {
			Yeti.screwed("Disallowed.");
		}
	}
	
	public int getPointsPerFace() {
		return pointsPerFace;
	}
}
