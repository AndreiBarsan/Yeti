package barsan.opengl.resources;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import barsan.opengl.Yeti;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.Model;
import barsan.opengl.rendering.StaticModel;

public class ModelLoader {
	
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
	
	public static class Face {
		public Vector3[] points;
		public Vector3[] texCoords;
		public Vector3[] normals;
		public Vector3[] tangents;
		public Vector3[] binormals;
		
		public int[] pindex, tcindex, nindex, tindex;
		
		public void computeTangents() {
			tangents = new Vector3[normals.length];
			binormals = new Vector3[normals.length];
			
			for(int i = 0; i < normals.length; ++i) {
				Vector3 normal = normals[i];
				if(normal == null) {
					assert false : "NOPE";
				}
				Vector3 t = new Vector3(-normal.z, 0, normal.x).normalize();
				if(normal.z == normal.x) {
					t.set(1.0f, 0.0f, 0.0f);
				}
								
				tangents[i] = t;
				binormals[i] = new Vector3(t).cross(normal).normalize();
			}
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
	
	/**
	 * Note: only loads triangle faces! The model class *can* however render
	 * quads and polygon faces.
	 * @param gl	The gl context.
	 * @param input Input source.
	 * @return		The newly loaded model, fresh from the oven!
	 */
	public static StaticModel fromObj(GL gl, Scanner input) {
		StaticModel model = new StaticModel(gl, "");
		
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
						if(! model.getGroups().containsKey(gname)) {
							model.getGroups().put(gname, new Group());
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
								model.getGroups().get(gname).geometry
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
								model.getGroups().get(gname).texture
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
								model.getGroups().get(gname).normals
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
						model.getGroups().get(gname).faces.add(result);
					}
					fc++;
					break;
					
				case 'o':
					if(! model.getName().equals("")) {
						Yeti.screwed("Warning: object name defined twice!");
					}
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
		
		if(model.getName() == "") {
			model.setName("unnamed_model");
		}
		
		if(model.getPointsPerFace() == 3) {
			model.setFaceMode(GL2.GL_TRIANGLES);
		} else if(model.getPointsPerFace() == 4) {
			model.setFaceMode(GL2.GL_QUADS);
		} else {
			Yeti.screwed("Models with non-quad or triangle faces are not supported.");
		}
		model.buildVBOs();
		
		/*
		Yeti.debug(String.format("Finished loading model %s! Vertices: %d, Texture coords: %d, Normal coords: %d, Faces: %d, Groups: %d",
				model.getName(), vc, tc, nc, fc, model.getGroups().size()));
				*/
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
	private static Face readFace(StaticModel model, String s) {
		int subCount = 0;
		Group master = model.master;
		Face face = new Face();
		String[] res = s.split("\\s");
		
		if(res.length < 3) {
			throw new InputMismatchException(String.format("Bad number of geometry/texture/normal coordinates (%d)!", res.length));
		}
		
		if(model.getPointsPerFace() != 0 && model.getPointsPerFace() != res.length) {
			throw new UnsupportedOperationException(String.format(
				"Meshes with varying face sizes (quads and triangles mixsed together, for instance) not supported. " +
				"(found %d points per face when expecting %d)", res.length, model.getPointsPerFace()));
		} else {
			if(model.getPointsPerFace() == 0) {
				model.setPointsPerFace(res.length);
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
		face.points = new Vector3[model.getPointsPerFace()];
		for(int i = 0; i < model.getPointsPerFace(); i++)
			face.points[i] = master.geometry.get(verts[i] - 1);
		
		if(master.texture.size() > 0) {
			face.texCoords = new Vector3[model.getPointsPerFace()];
			if(texs[0] != 0)
			for(int i = 0; i < model.getPointsPerFace(); i++)
				face.texCoords[i] = master.texture.get(texs[i] - 1);
		}
		
		if(master.normals.size() > 0) {
			if(norms[0] != 0) {
				face.normals = new Vector3[model.getPointsPerFace()];
				//face.autoNormal = false;
				for(int i = 0; i < model.getPointsPerFace(); i++) 
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
				
				for(int i = 0; i < model.getPointsPerFace(); i++) {
					face.normals[i] = new Vector3(normal);
				}
			}
		}
		
		// Save the original inidices for debugging purposes
		face.pindex = verts;
		face.tcindex = texs;
		face.nindex = norms;
		return face;
	}
	
	public static StaticModel buildPlane(float width, float height, int sdivw, int sdivh) {
		GL2 gl = Yeti.get().gl.getGL2();
		StaticModel result = new StaticModel(gl, "plane");
		
		float uw = width / sdivw;
		float uh = height / sdivh;
		
		int tw = (sdivw % 2 == 0) ? sdivw / 2 - 1 : sdivw / 2;
		int th = (sdivh % 2 == 0) ? sdivh / 2 - 1 : sdivh / 2;
		for(int x = -sdivw / 2; x <= tw; x++) {
			for(int y = -sdivh / 2; y <= th; y++) {
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
	
	/** Builds a Quad over the plane defined by X and Z. Useful for floors. */
	public static StaticModel buildQuadXZ(float width, float height) {
		return buildQuad(width, height, true);
	}
	
	/** Builds a Quad over the plane defined by X and Y. Useful for screen-oriented
	 * stuff, like billboards and post-process stuff. */
	public static StaticModel buildQuadXY(float width, float height) {
		return buildQuad(width, height, false);
	}
	
	/**
	 * Builds a simple quad.
	 * @param xz whether to build the quad in the xz plane. xy otherwise.
	 */
	private static StaticModel buildQuad(float width, float height, boolean xz) {
		GL2 gl = Yeti.get().gl.getGL2();
		StaticModel result = new StaticModel(gl, "quad");
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
}
