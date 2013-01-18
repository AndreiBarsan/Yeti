package barsan.opengl.rendering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Scanner;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import barsan.opengl.Yeti;
import barsan.opengl.math.Vector3;

// TODO: IMPORTANT! Interactively add to VBO for faster building!!!
// TODO: fail on freeform geomerty
// TODO: future - load & render freeform stuff
/**
 * @author Andrei Barsan
 */
public class Model {
	
	static final int COORDS_PER_POINT = 3;
	static final int T_COORDS_PER_POINT = 2;

	public static class Face {
		public Vector3[] points;
		public Vector3[] texCoords;
		public Vector3[] normals;
		public Vector3 tangent;
		public Vector3 bitangent;
		
		public int[] pindex, tindex, nindex;
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < 3; i++) {
				sb.append(String.format("%d/%d/%d ", pindex[i], tindex[i], nindex[i]));
			}
			return sb.toString();
		}
	}
	
	public static class Group {
		// TODO: only use these things as temporary storage (or use them to optimize
		// the rendering later)
		public ArrayList<Vector3> geometry = new ArrayList<>();
		public ArrayList<Vector3> texture = new ArrayList<>();
		public ArrayList<Vector3> normals = new ArrayList<>();
		// tangents not here since this class is only used as a helper building
		// the object - tangents are all computed on the fly
		public ArrayList<Face> faces = new ArrayList<>();
	}
	
	// Vertex buffers for faster rendering
	private VBO vertices;
	private VBO texcoords;
	private VBO normals;
	//private VBO tangents;
	//private VBO bitangents;
	
	protected HashMap<String, Group> groups = new HashMap<>();
	public Group master = new Group();
	
	private String name;
	protected final GL gl;
	
	/**
	 * Whether GL_TRIANGLES or GL_QUADS is being used.
	 */
	private int faceMode;

	/**
	 * Actual number of vertices per triangle.
	 */
	private int pointsPerFace;
	
	// TODO: maybe refactor this away
	/**
	 * Note: only loads triangle faces! The model class *can* however render
	 * quads and polygon faces.
	 * @param gl	The gl context.
	 * @param input Input source.
	 * @return		The newly loaded model, fresh from the oven!
	 */
	public static Model fromObj(GL gl, Scanner input) {
		Model model = new Model(gl, "");
		
		// Counters
		int vc = 0, tc = 0, nc = 0, fc = 0;

		ArrayList<String> openGroups = new ArrayList<>();
		openGroups.add("default");
		
		while(input.hasNextLine()) {
			String line = input.nextLine();
			if(line.length() == 0) continue;
			
			char lead = line.charAt(0);
			switch(lead) {
				case '#':
					continue;
					
				case 'g':
					openGroups.clear();
					for(String gname : line.split("\\s")) {
						openGroups.add(gname);
						if(! model.groups.containsKey(gname)) {
							model.groups.put(gname, new Group());
						}
					}
					break;
					
				case 'v':
					if(line.length() == 1) {
						Yeti.screwed("Error: blank vertex data");
						throw new InputMismatchException();
					}
					
					switch(line.charAt(1)) {
						case ' ':
						{
							// TODO: hack warning
							Vector3 res = readVertex(line.substring(2));
							model.master.geometry.add(res);
							for(String gname : openGroups) {
								model.groups.get(gname).geometry
									.add(res);
							}
							vc++;
							break;
						}
						
						case 't':
						{
							Vector3 res = readTexCoords(line.substring(3));
							model.master.texture.add(res);
							for(String gname : openGroups) {
								model.groups.get(gname).texture
									.add(readTexCoords(line.substring(3)));
							}
							tc++;
							break;
						}
							
						case 'n': 
						{
							Vector3 res = readVertex(line.substring(3));
							model.master.normals.add(res);
							for(String gname : openGroups) {
								model.groups.get(gname).normals
									.add(readVertex(line.substring(3)));
							}
							nc++;
							break;
						}
					}
					
					break;
					
				case 'f':
					Face result = readFace(model, line.substring(2));
					model.master.faces.add(result);
					for(String gname : openGroups) {
						model.groups.get(gname).faces.add(result);
					}
					fc++;
					// System.out.println("DEBUG: " + result.toString());
					break;
					
				case 'o':
					if(! model.name.equals(""))
						Yeti.screwed("Warning: object name defined twice!");
					// TODO: not hack
					model.setName(line.substring(2));
					break;
					
				case 's':
					// TODO: shading groups
					break;
			
				case 'm':
					if(line.startsWith("mtllib")) {
						Yeti.warn("Skipping material definition.");
					}
					break;
					
				case 'u':
					if(line.startsWith("usemtl")) {
						// see above
					}
					break;
				default:
					Yeti.screwed("Unrecognized identifier: " + lead);
					continue;
			}
		}
		
		if(model.name == "") {
			model.name = "unnamed_model";
		}
		
		if(model.pointsPerFace == 3) {
			model.faceMode = GL2.GL_TRIANGLES;
		} else if(model.pointsPerFace == 4) {
			model.faceMode = GL2.GL_QUADS;
		} else {
			Yeti.screwed("Model with non-quad or triangle faces are not supported.");
		}
		model.buildVBOs();
		
		Yeti.debug(String.format("Finished loading model %s! Vertices: %d, Texture coords: %d, Normal coords: %d, Faces: %d, Groups: %d",
				model.name, vc, tc, nc, fc, model.groups.size()));
		return model;
	}
	
	private static Vector3 readVertex(String s) {
		String[] res = s.split("\\s");
		return new Vector3(	Float.parseFloat(res[0]), 
							Float.parseFloat(res[1]),
							Float.parseFloat(res[2]));
	}
	
	/**
	 * Reads some texture coords, in either X/Y/Z or U/V coords
	 * @param s
	 * @return
	 */
	private static Vector3 readTexCoords(String s) {
		String[] res = s.split("\\s");
		if(res.length == 3)	return new Vector3(	Float.parseFloat(res[0]), 
							Float.parseFloat(res[1]),
							Float.parseFloat(res[2]));
		else return new Vector3(Float.parseFloat(res[0]), 
								Float.parseFloat(res[1]),
								0);
	}
	
	// Prevents having to do a shitload of memory allocations
	// FIXME: doesn't support arbitrary polygons
	static int[] 	verts = new int[3], 
					texs = new int[3],
					norms = new int[3];
	static Vector3 aux_vector1 = new Vector3(), aux_vector2 = new Vector3();
	private static Face readFace(Model model, String s) {
		int subCount = 0;
		Group master = model.master;
		Face face = new Face();
		String[] res = s.split("\\s");
		
		if(res.length < 3) {
			throw new InputMismatchException(String.format("Bad number of geometry/texture/normal coordinates (%d)!", res.length));
		}
		
		if(model.pointsPerFace != 0 && model.pointsPerFace != res.length) {
			throw new UnsupportedOperationException(String.format(
				"Meshes with varying face sizes (quads and triangles mixsed together, for instance) not supported. " +
				"(found %d points per face when expecting %d)", res.length, model.pointsPerFace));
		} else {
			if(model.pointsPerFace == 0) {
				model.pointsPerFace = res.length;
			}
		}
		
		for(int i = 0; i < res.length; i++) {
			if(res[i].contains("/")) {
				String[] bits = res[i].split("/");
				if(subCount == 1) {
					throw new InputMismatchException("Invalid combination of geometry/texture/normal coordinates.");
				}
				
				verts[i] = Integer.parseInt(bits[0]);
				if(bits[1].length() > 0) {
					if(subCount == 2) {
						throw new InputMismatchException("Invalid combination of geometry/texture/normal coordinates.");
					}
					texs[i] = Integer.parseInt(bits[1]);
					subCount = 3;
				} else {
					if(subCount == 3) {
						throw new InputMismatchException("Invalid combination of geometry/texture/normal coordinates.");
					}
					subCount = 2;
				}
				norms[i] = Integer.parseInt(bits[2]);
				
			} else {
				if(subCount != 1 && subCount != 0) {
					throw new InputMismatchException("Invalid combination of geometry/texture/normal coordinates.");
				}
				verts[i] = Integer.parseInt(res[i]);
				subCount = 1;
			}
		}
		
		// Cache all the vertex data in the faces
		face.points = new Vector3[model.pointsPerFace];
		for(int i = 0; i < model.pointsPerFace; i++)
			face.points[i] = master.geometry.get(verts[i] - 1);
		
		if(master.texture.size() > 0) {
			face.texCoords = new Vector3[model.pointsPerFace];
			if(texs[0] != 0)
			for(int i = 0; i < model.pointsPerFace; i++)
				face.texCoords[i] = master.texture.get(texs[i] - 1);
		}
		
		if(master.normals.size() > 0) {
			if(norms[0] != 0) {
				face.normals = new Vector3[model.pointsPerFace];
				//face.autoNormal = false;
				for(int i = 0; i < model.pointsPerFace; i++) 
					face.normals[i] = master.normals.get(norms[i] - 1);
			} else {
				//face.autoNormal = true;
				// Just compute the face's normal
				if(face.points.length > 3) {
					throw new Error("Cannot compute face normal!");
					// TODO: maybe compute quad normals if possible
					// TODO: is auto-splitting of bad quads considered sensible behavior?
				}
				Vector3 normal = aux_vector1.set(face.points[1]).sub(face.points[0])
						.cross(aux_vector2.set(face.points[2]).sub(face.points[0]));
				
				for(int i =0; i < model.pointsPerFace; i++) {
					face.normals[i] = new Vector3(normal);
				}
			}
		}
		
		// Save the original inidices for debugging purposes
		face.pindex = verts;
		face.tindex = texs;
		face.nindex = norms;
		return face;
	}
	
	public static Model buildPlane(float width, float height, int sdivw, int sdivh) {
		GL2 gl = Yeti.get().gl.getGL2();
		Model result = new Model(gl, "plane");
		
		float uw = width / sdivw;
		float uh = height / sdivh;
		
		int tw = (sdivw % 2 == 0) ? sdivw / 2 - 1 : sdivw / 2;
		int th = (sdivh % 2 == 0) ? sdivh / 2 - 1 : sdivh / 2;
		for(int x = -sdivw / 2; x < tw; x++) {
			for(int y = -sdivh / 2; y < th; y++) {
				Face f = new Face();
				f.points = new Vector3[] {
					new Vector3(x * uw, 0, y * uh),
					new Vector3(x * uw, 0, (y + 1) * uh),
					new Vector3((x + 1) * uw, 0, (y + 1) * uh),
					new Vector3((x + 1) * uw, 0, y * uh)
				};
				f.texCoords = new Vector3[] {
					new Vector3(0, 0, 0),
					new Vector3(0, 1, 0),
					new Vector3(1, 1, 0),
					new Vector3(1, 0, 0)
				};
				f.normals = new Vector3[] {
					new Vector3(0, 1, 0),
					new Vector3(0, 1, 0),
					new Vector3(0, 1, 0),
					new Vector3(0, 1, 0)
				};
				
				result.addFace(f);
			}
		}
		
		result.setPointsPerFace(4);
		result.buildVBOs();
		return result;
	}
	
	public static Model buildQuad(float width, float height) {
		return buildQuad(width, height, true);
	}
	
	/**
	 * Builds a simple quad.
	 * 
	 * @param xz whether to build the quad in the xz plane. xy otherwise.
	 */
	public static Model buildQuad(float width, float height, boolean xz) {
		GL2 gl = Yeti.get().gl.getGL2();
		Model result = new Model(gl, "quad");
		result.setPointsPerFace(4);
		
		float hw = width / 2.0f;
		float hh = height / 2.0f;
		
		Face face = new Face();
		face.texCoords = new Vector3[] {
				new Vector3(0, 0, 0),
				new Vector3(0, 1, 0),
				new Vector3(1, 1, 0),
				new Vector3(1, 0, 0)
			};
		if(xz) {
			face.points = new Vector3[] {
				new Vector3(-hw, 0, -hh),
				new Vector3(-hw, 0,  hh),
				new Vector3( hw, 0,  hh),
				new Vector3( hw, 0, -hh)
			};
			face.normals = new Vector3[] {
				new Vector3(0, 1, 0),
				new Vector3(0, 1, 0),
				new Vector3(0, 1, 0),
				new Vector3(0, 1, 0)
			};
		} else {
			face.points = new Vector3[] {
				new Vector3(-hw, -hh, 0),
				new Vector3(-hw,  hh, 0),
				new Vector3( hw,  hh, 0),
				new Vector3( hw, -hh, 0)
			};
			face.normals = new Vector3[] {
				new Vector3(0, 0, 1),
				new Vector3(0, 0, 1),
				new Vector3(0, 0, 1),
				new Vector3(0, 0, 1)
			};
		}
		result.addFace(face);
		
		result.buildVBOs();
		return result;
	}
	
	public Model(GL gl, String name) {
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
		
		/*
		if(master.faces.get(0).texCoords != null && master.faces.get(0).normals != null) {
			
			for(Face f : master.faces) {
				
				Vector3 dp1 = f.points[1].copy().sub(f.points[0]);
				Vector3 dp2 = f.points[2].copy().sub(f.points[1]);
				
				float duv1x = f.points[1].x - f.points[0].x;
				float duv2x = f.points[2].x - f.points[1].x;
				
				float duv1y = f.points[1].y - f.points[0].y;
				float duv2y = f.points[2].y - f.points[1].y;
				
				float r = 1.0f / (duv1x * duv2y - duv1y * duv2x);
				f.tangent = dp1.copy().mul(duv2y).sub(dp2.copy().mul(duv1y)).mul(r);
				f.bitangent = dp2.copy().mul(duv1x).sub(dp1.copy().mul(duv2x)).mul(r);
			}
			
			tangents.open();
			for(Face f : master.faces) {
				tangents.append(f.tangent);
				tangents.append(f.tangent);
				tangents.append(f.tangent);
			}
			tangents.close();
			bitangents.open();
			for(Face f : master.faces) {
				bitangents.append(f.bitangent);
				bitangents.append(f.bitangent);
				bitangents.append(f.bitangent);
			}
			bitangents.close();
		}
		//*/
		
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
	
	public void dispose() {
		gl.glDeleteBuffers(3, new int[] { 	vertices.getHandle(), 
											normals.getHandle(),
											texcoords.getHandle(),
										//	tangents.getHandle(),
										//	bitangents.getHandle()
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
	
	/*
	public VBO getTangents() {
		return tangents;
	}
	
	public VBO getBitangents() {
		return bitangents;
	}
	//*/

	public int getFaceMode() {
		return faceMode;
	}

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
