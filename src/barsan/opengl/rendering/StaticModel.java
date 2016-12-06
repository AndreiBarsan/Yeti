package barsan.opengl.rendering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import barsan.opengl.Yeti;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.resources.Face;
import barsan.opengl.resources.ModelLoader;
import barsan.opengl.resources.ModelLoader.Group;

/**
 * @author Andrei Barsan
 * 
 */
public class StaticModel extends Model {
	
	public static final int COORDS_PER_POINT = 3;
	public static final int TEX_COORDS_PER_POINT = 2;

	// Vertex buffers for faster rendering
	private VBO vertices;
	private VBO texcoords;
	private VBO normals;
	private VBO tangents;
	private VBO binormals;
	
	protected HashMap<String, Group> groups = new HashMap<>();
	public Group master = new Group();
	
	private String name;
	protected final GL gl;	// TODO: maybe refactor this out
	
	public StaticModel(GL gl, String name) {
		this.gl = gl;
		this.name = name;
		groups.put("default", new Group());
	}
	
	static float uv[] = new float[2];
	public void buildVBOs() {
		assert master.faces.size() > 0 : "Empty model";
		
		/* If no materials were loaded/specified, just create a basic one. */
		if(null == defaultMaterialGroups) {
			defaultMaterialGroups = new ArrayList<MaterialGroup>();
		}
		if(defaultMaterialGroups.isEmpty()) {
			MaterialGroup defMG = new MaterialGroup(0, 
					master.faces.size(), 
					new Material());
			defaultMaterialGroups.add(defMG);
			
		}
		
		int size = master.faces.size() * pointsPerFace;
		
		vertices = 	new VBO(GL2.GL_ARRAY_BUFFER, size, COORDS_PER_POINT);
		normals = 	new VBO(GL2.GL_ARRAY_BUFFER, size, COORDS_PER_POINT);
		texcoords = new VBO(GL2.GL_ARRAY_BUFFER, size, TEX_COORDS_PER_POINT);
		tangents = 	new VBO(GL2.GL_ARRAY_BUFFER, size, COORDS_PER_POINT);
		binormals = new VBO(GL2.GL_ARRAY_BUFFER, size, COORDS_PER_POINT);
		
		for(Face f : master.faces) {
			if(ModelLoader.loadingFronLHCoords) {
				vertices.quickAppend(f.points);
			} else {
				vertices.quickAppendReverse(f.points);
			}
			
			if(null != master.faces.get(0).normals) {
				f.computeTangBinorm();
				
				if(ModelLoader.loadingFronLHCoords) {
					normals.quickAppend(f.normals);
					tangents.quickAppend(f.tangents);
					binormals.quickAppend(f.binormals);
				} else {
					normals.quickAppendReverse(f.normals);
					tangents.quickAppendReverse(f.tangents);
					binormals.quickAppendReverse(f.binormals);
				}
			}
			
			if(f.texCoords != null) {
				texcoords.open();
				if(ModelLoader.loadingFronLHCoords) {
					for(Vector3 tc : f.texCoords) {
						uv[0] = tc.x;
						uv[1] = tc.y;
						texcoords.append(uv);
					}
				} else {
					for(int i = f.texCoords.length - 1; i >=0; --i) {
						Vector3 tc = f.texCoords[i];
						uv[0] = tc.x;
						uv[1] = tc.y;
						texcoords.append(uv);
					}
				}
				texcoords.close();
			}
			else {
				texcoords.open();
				texcoords.append(new float[]{ 0, 0 });
				texcoords.append(new float[]{ 0, 0 });
				texcoords.append(new float[]{ 0, 0 });
				texcoords.close();
			}
		}
		
		if(Yeti.get().settings.debugModels) {
			Yeti.debug(String.format("VBOs for \"%s\" built. Normal element count: %d;" +
					" Geometry element count: %d. Also added precomputed tangents and binormals.",
				getName(), vertices.getSize(), normals.getSize()));
		}
	}
	
	public void addFace(Face face) {
		addFace("default", face);
	}
	
	public void addFace(String groupName, Face face) {
		groups.get(groupName).faces.add(face);
		master.faces.add(face);
	}
	
	@Override
	public int getArrayLength() {
		return vertices.getSize();		
	}
	
	@Override
	public VBO getTexCoords() {
		return texcoords;
	}
	
	public void dispose() {
		gl.glDeleteBuffers(5, new int[] { 	vertices.getHandle(), 
											normals.getHandle(),
											tangents.getHandle(),
											binormals.getHandle(),
											texcoords.getHandle()
										}, 0);
	}
	
	public VBO getVertices() {
		return vertices;
	}
	
	public VBO getNormals() {
		return normals;
	}
	
	public VBO getTangents() {
		return tangents;
	}
	
	public VBO getBinormals() {
		return binormals;
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

	public Map<String, Group> getGroups() {
		return groups;
	}
}
