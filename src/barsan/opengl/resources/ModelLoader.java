package barsan.opengl.resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import barsan.opengl.Yeti;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.MaterialGroup;
import barsan.opengl.rendering.StaticModel;
import barsan.opengl.rendering.materials.Material;

public class ModelLoader {
	
	public static class Group {
		public ArrayList<Vector3> geometry = new ArrayList<>();
		public ArrayList<Vector3> texture = new ArrayList<>();
		public ArrayList<Vector3> normals = new ArrayList<>();
		
		public ArrayList<Face> faces = new ArrayList<>();
	}
	
	/**
	 * Whether the models we're importing are using a left-handed coordinate system,
	 * meaning they need to get z-flipped for our OpenGL right-handed one.
	 * 
	 * FIXME: make sure you don't mess up the quads used when rendering on FBOs
	 */
	public static boolean loadingFronLHCoords = true;
	
	/** Counter helping provide unambiguous names for unnamed models. */
	private static int umCount = 0;
	
	/**
	 * Read a Wavefront object file and return a model. Automatically loads requred
	 * textures defined in detected .mtl files <b>from the default texture folder</b>
	 * and not from the same folder as the models and .mtl files.
	 * @throws IOException 
	 */
	public static StaticModel fromObj(GL gl, File input, int explicitPointsPerFace) throws IOException {
		StaticModel model = new StaticModel(gl, "");
		
		if(explicitPointsPerFace != 0) {
			model.setPointsPerFace(explicitPointsPerFace);
		}
		
		// Counters
		int vc = 0, tc = 0, nc = 0, fc = 0;

		HashMap<String, Material> materials = new HashMap<String, Material>();
		ArrayList<MaterialGroup> matGroups = new ArrayList<MaterialGroup>();
		
		MaterialGroup currentMatGroup = null;
		
		ArrayList<String> lines = new ArrayList<String>();
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input)))) {
			String lineBuffer = null;
			while(null != (lineBuffer = br.readLine())) {
				lines.add(lineBuffer);
			}
		}

			for(String line : lines) {
				if(line.length() == 0) continue;
				char lead = line.charAt(0);
				switch(lead) {
					case '#':
						continue;
						
					case 'g':
						String mn = model.getName() == null ? model.getName() : "unnamed";
						if(Yeti.get().settings.debugModels) {
							Yeti.debug("Model [" + mn + "]: groups currently disabled.");
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
								Vector3 res = readVertex(line.substring(1).trim());
								if(loadingFronLHCoords) {
									res.z *= -1.0f;
								}
								model.master.geometry.add(res);
								
								vc++;
								break;
							}
							
							case 't':
							{
								Vector3 res = readTexCoords(line.substring(3));
								model.master.texture.add(res);
								tc++;
								break;
							}
								
							case 'n': 
							{
								Vector3 res = readVertex(line.substring(3));
								if(loadingFronLHCoords) {
									res.z *= -1.0f;
								}
								model.master.normals.add(res);
								
								nc++;
								break;
							}
						}
						
						break;
						
					case 'f': 
						List<Face> result = readFace(currentMatGroup, model, 
								line.substring(1).trim(),
								vc, nc, tc);
						model.master.faces.addAll(result);
						fc += result.size();
						break;
						
					case 'o':
						if(! model.getName().equals("")) {
							Yeti.screwed("Warning: object name defined twice!");
						}
						
						model.setName(line.substring(1).trim());
						break;
						
					case 's':
						// TODO: shading groups
						break;
				
					case 'm':
						if(line.startsWith("mtllib")) {
							List<Material> lm = MTLLoader.load(line.split("\\s")[1]);
							assert ! lm.isEmpty() 
								: "Loaded MTL file: " + line.split("\\s")[1]
								+ ", but no materials were actually defined.";
							
							for(Material m : lm) {
								materials.put(m.getName(), m);
							}
						}
						break;
						
					case 'u':
						if(line.startsWith("usemtl")) {
							if(!matGroups.isEmpty()) {
								currentMatGroup.length = fc - currentMatGroup.beginIndex;
							}
							
							String matName = line.split("\\s+")[1];
							Material material = materials.get(matName);
							assert material != null : "Material not defined: " + matName;
							currentMatGroup = new MaterialGroup(fc, 0, material);
							matGroups.add(currentMatGroup);
						}
						break;					
				}
			}
		
		
		if(!matGroups.isEmpty()) {
			// Finish off the last material group
			MaterialGroup mg = matGroups.get(matGroups.size() - 1);
			mg.length = fc - mg.beginIndex;
		}
		
		if(model.getName() == "") {
			model.setName("unnamed_model_" + umCount++);
		}
		
		model.setDefaultMaterialGroups(matGroups);	
		model.buildVBOs();
		
		if(Yeti.get().settings.debugModels) {
			Yeti.debug(String.format("Finished loading model %s! Vertices: %d, Texture coords: %d, Normal coords: %d, Faces: %d, Groups: %d",
				model.getName(), vc, tc, nc, fc, model.getGroups().size()));
		}
		
		return model;
	}
	
	private static Vector3 readVertex(String s) {
		String[] res = s.split("\\s+");
		return new Vector3(	Float.parseFloat(res[0]), 
							Float.parseFloat(res[1]),
							Float.parseFloat(res[2]));
	}
	
	/** Reads a set texture coords, in either X/Y/Z or U/V coords */
	private static Vector3 readTexCoords(String s) {
		String[] res = s.split("\\s");
		if(res.length == 3) {	
			return new Vector3(	Float.parseFloat(res[0]), 
								Float.parseFloat(res[1]),
								Float.parseFloat(res[2]));
		}
		else {
			return new Vector3(	Float.parseFloat(res[0]), 
								Float.parseFloat(res[1]),
								0);
		}
	}
	
	
	static int[] 	verts = new int[64], 
					texs = new int[64],
					norms = new int[64];
	static Vector3 aux_vector1 = new Vector3(), aux_vector2 = new Vector3();
	
	/**
	 * Reads a face and returns it, splitting quads if needed (e.g. if the model
	 * is set to only use triangles).
	 */
	private static List<Face> readFace(MaterialGroup mg, StaticModel model, String s,
			int currentVertex, int currentNormal, int currentTC) {
		
		int subCount = 0;
		Group master = model.master;
		Face face = new Face();
		String[] res = s.split("\\s");
		boolean goingToSplit = false;
		ArrayList<Face> out = new ArrayList<Face>();
		
		if(res.length < 3) {
			throw new InputMismatchException(String.format("Bad number of geometry/texture/normal coordinates (%d)!", res.length));
		}
				
		if(model.getPointsPerFace() == 0) {
			if(Yeti.get().settings.debugModels) {
				Yeti.debug("Model [" + model.getName() + "]: now using " + res.length + " points per face.");
			}
			model.setPointsPerFace(res.length);
		}
		else if(model.getPointsPerFace() != res.length) {
			if(model.getPointsPerFace() == 3) {
				goingToSplit = true;
			} else {
				throw new UnsupportedOperationException("Mesh with varying face sizes but" +
						" no way of separating them.");
			}
		}
	
		for(int i = 0; i < res.length; i++) {
			if(res[i].contains("/")) {
				String[] bits = res[i].split("/");
				
				if(subCount == 1) {
					throw new InputMismatchException("Invalid combination of geometry/texture/normal coordinates.");
				}
				
				verts[i] = Integer.parseInt(bits[0]);
				
				// Negative indices provide relative references
				if(verts[i] < 0) {
					verts[i] = currentVertex + verts[i] + 1;
				}
				
				if(bits[1].length() > 0) {
					if(subCount == 2) {
						throw new InputMismatchException("Invalid combination of geometry/texture/normal coordinates.");
					}
					texs[i] = Integer.parseInt(bits[1]);
					if(texs[i] < 0) {
						texs[i] = currentTC + texs[i] + 1;
					}
					subCount = 3;
				} else {
					if(subCount == 3) {
						throw new InputMismatchException("Invalid combination of geometry/texture/normal coordinates.");
					}
					subCount = 2;
				}
				norms[i] = Integer.parseInt(bits[2]);
				if(norms[i] < 0) {
					norms[i] = currentNormal + norms[i] + 1;
				}
				
			} else {
				if(subCount != 1 && subCount != 0) {
					throw new InputMismatchException("Invalid combination of geometry/texture/normal coordinates.");
				}
				verts[i] = Integer.parseInt(res[i]);
				if(verts[i] < 0) {
					verts[i] = currentVertex - verts[i] + 1;
				}
				subCount = 1;
			}
		}
		
		// Cache all the vertex data in the faces
		face.points = new Vector3[res.length];
		for(int i = 0; i < res.length; i++) {
			int index = verts[i] - 1;
			face.points[i] = master.geometry.get(index).copy();
		}
		
		if(master.texture.size() > 0) {
			face.texCoords = new Vector3[res.length];
			if(texs[0] != 0) {
				for(int i = 0; i < res.length; i++) {
					face.texCoords[i] = master.texture.get(texs[i] - 1).copy();
				}
			}
		}
		
		if(master.normals.size() > 0) {
			if(norms[0] != 0) {
				face.normals = new Vector3[res.length];
				for(int i = 0; i < res.length; i++)  {
					face.normals[i] = master.normals.get(norms[i] - 1).copy();
				}
			} else {
				// Just compute the face's normal
				if(face.points.length > 3) {
					throw new Error("Cannot compute face normal!");
				}
				
				Vector3 normal = aux_vector1.set(face.points[1]).sub(face.points[0])
									.cross(aux_vector2.set(face.points[2]).sub(face.points[0]));
				
				for(int i = 0; i < res.length; i++) {
					face.normals[i] = new Vector3(normal);
				}
			}
		}
		
		if(goingToSplit) {
			out.addAll(face.split());
		}
		else {
			out.add(face);
		}
		
		// Save the original inidices for debugging purposes
		face.pindex = verts;
		face.tcindex = texs;
		face.nindex = norms;
		return out;
	}
	
	public static StaticModel buildPlane(float width, float height, int sdivw, int sdivh) {
		GL gl = Yeti.get().gl.getGL();
		StaticModel result = new StaticModel(gl, "plane");
		
		boolean old = loadingFronLHCoords;
		loadingFronLHCoords = false;
		
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
		
		loadingFronLHCoords = old;
		
		return result;
	}
	
	/** Creates a simple 2x2 XY-aligned quad. */
	public static StaticModel makeScreenQuad() {
		return buildQuadXY(2.0f, 2.0f);
	}
	
	/** Builds a Quad over the plane defined by X and Z. Useful for floors. */
	public static StaticModel buildQuadXZ(float width, float height) {
		return buildQuad(width, height, true);
	}
	
	/** 
	 * Builds a Quad over the plane defined by X and Y. Useful for screen-oriented
	 * stuff, like billboards and post-process stuff. 
	 */
	public static StaticModel buildQuadXY(float width, float height) {
		return buildQuad(width, height, false);
	}
	
	/**
	 * Builds a simple quad.
	 * @param xz whether to build the quad in the xz plane. xy otherwise.
	 */
	private static StaticModel buildQuad(float width, float height, boolean xz) {
		GL gl = Yeti.get().gl;
		StaticModel result = new StaticModel(gl, "quad");
		result.setPointsPerFace(4);
		
		boolean old = loadingFronLHCoords;
		loadingFronLHCoords = false;
		
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
				new Vector3( hw, 0, -hh),
				
			};
			face.normals = new Vector3[] {
				new Vector3(0, 1, 0),
				new Vector3(0, 1, 0),
				new Vector3(0, 1, 0),
				new Vector3(0, 1, 0),
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
		
		loadingFronLHCoords = old;
		return result;
	}
}
