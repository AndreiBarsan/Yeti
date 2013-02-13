package barsan.opengl.resources;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import javax.media.opengl.GL2GL3;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;

import barsan.opengl.Yeti;
import barsan.opengl.rendering.CubeTexture;
import barsan.opengl.rendering.StaticModel;
import barsan.opengl.rendering.Shader;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

public class ResourceLoader {
	
	static final String EXT_VERTEX 		= ".vsh";
	static final String EXT_FRAGMENT 	= ".fsh";
	static final String EXT_GEOMETRY 	= ".gsh";
	
	static final String RESBASE = "res/";
	static final String SHADERBASE = "";
	
	static boolean initialized = false;
	
	static HashMap<String, Shader> shaders = new HashMap<>();
	static HashMap<String, StaticModel> models = new HashMap<>();
	static HashMap<String, CubeTexture> cubeTextures = new HashMap<>();
	
	// TODO: refactor this away
	static HashMap<String, Texture> textures = new HashMap<>();
	static HashMap<String, TextureData> textureData = new HashMap<>();
	
	static HashMap<String, String> linkRules = new HashMap<>();
	static {
		// Custom linking rules (e.g. in the case of the animated phong, there
		// are only a few differences in the vertex shader but both *share* the
		// same fragment shader.
		linkRules.put("animatedPhong.vsh", "phong.fsh");
	}
	
	public static void init() {
		Yeti.debug("Resource loader initialized.");
		initialized = true;
	}

	public static void loadObj(String name, String fileName) throws IOException {
		Scanner s = new Scanner(new File(fileName));
		models.put(name, ModelLoader.fromObj(Yeti.get().gl, s));
	}
	
	public static void loadShader(String name, String fileName)
			throws FileNotFoundException {
		
		File vFile = new File(fileName + EXT_VERTEX);
		File gFile = new File(fileName + EXT_GEOMETRY);
		File fFile = new File(fileName + EXT_FRAGMENT);
		
		loadShader(name, vFile, fFile, gFile.exists() ? gFile : null);
	}
	
	public static void loadCubeTexture(String name, String ext) {
		CubeTexture tex = new CubeTexture();
		GLProfile glp = Yeti.get().gl.getGLProfile();
		
		try {
			for(int i = 0; i < 6; i++) {
				String fname = "res/tex/" + name + "_" + tex.names[i] + "." + ext;
				TextureData data = TextureIO.newTextureData(
						glp,
						new File(fname),
						true, null);
				tex.getTexture().updateImage(Yeti.get().gl, data, CubeTexture.cubeSlots[i]);
			}
		} catch (GLException | IOException e) {
			Yeti.screwed("Error loading textures", e);
		}
		
		cubeTextures.put(name, tex);
	}
	
	// clean version
	public static void loadShader(String name) throws FileNotFoundException {
		if(shaders.containsKey(name)) {
			Yeti.warn("Warning, overwriting shader named: " + name);
		}
		
		String baseFolder = RESBASE + SHADERBASE;
		String baseVName = name + EXT_VERTEX;
		String baseGName = name + EXT_GEOMETRY;
		String baseFName = name + EXT_FRAGMENT;
		
		// Only branch based on differend vertex shaders (since that's what we
		// also base our read all function on).
		System.out.println(baseVName);
		if(linkRules.containsKey(baseVName)) {
			String other = linkRules.get(baseVName);
			String ext = other.substring(other.lastIndexOf('.'), other.length());
			System.out.println(ext);
			if(ext.equals(EXT_FRAGMENT)) {
				baseFName = other;
			} else if(ext.equals(EXT_GEOMETRY)) {
				baseGName = other;
			}
		}
		
		File vFile = new File(baseFolder + baseVName);
		File gFile = new File(baseFolder + baseGName);
		File fFile = new File(baseFolder + baseFName);
		
		if(!vFile.exists() || !fFile.exists()) {
			Yeti.screwed("Incomplete shader [" + name + "]. It needs both a vertex" +
					"and a fragment shader. Did you forget a custom linking rule?");
		}
		
		Scanner vinput = null, finput = null, ginput = null;
		try{
			vinput = new Scanner(vFile);
			finput = new Scanner(fFile);
			if(gFile.exists()) ginput = new Scanner(gFile);
			
			shaders.put(name, new Shader(Yeti.get().gl, name,
					vinput.useDelimiter("\\Z").next(),
					finput.useDelimiter("\\Z").next(),
					ginput != null ? ginput.useDelimiter("\\Z").next() : null
				));
		} finally {
			if(vinput != null) vinput.close();
			if(ginput != null) ginput.close();
			if(finput != null) finput.close();
		}
	}
	
	// TODO: separately cache fragment, vertex and geometry shaders for cross-linking
	// TODO: only work with files in *this* method (keep all IO in the same place
	// whenever possible!)
	public static void loadShader(String name, File vertexFile, File fragmentFile, File geometryFile)
			throws FileNotFoundException {
		
		assert false : "No longer used.";
		
		if(shaders.containsKey(name)) {
			Yeti.warn("Warning, overwriting shader named: " + name);
		}
		
		File vSource = vertexFile;
		File fSource = fragmentFile;
		File gSource = geometryFile;
		
		// Apply custom link rules for the vertex shader
		if(linkRules.containsKey(vertexFile.getName())) {
			String other = linkRules.get(vertexFile.getName());
			String ext = other.substring(other.lastIndexOf('.'), other.length());
			
			if(ext.equals(EXT_FRAGMENT)) {
				fSource = new File(other);
			} else if(ext.equals(EXT_GEOMETRY)) {
				gSource = new File(other);
			}
		}
		
		Scanner vinput = new Scanner(vSource);
		Scanner finput = new Scanner(fSource);
		
		if(geometryFile != null) {
			Scanner ginput = new Scanner(gSource);
			shaders.put(name, new Shader(Yeti.get().gl, name,
					vinput.useDelimiter("\\Z").next(),
					finput.useDelimiter("\\Z").next(),
					ginput.useDelimiter("\\Z").next()
				));
			ginput.close();			
		} else {
			shaders.put(name, new Shader(Yeti.get().gl, name,
				vinput.useDelimiter("\\Z").next(),
				finput.useDelimiter("\\Z").next()
			));
		}
		vinput.close();
		finput.close();
	}

	public static void loadTexture(String name, String fileName) throws GLException, IOException {
		//Texture tex = TextureIO.newTexture(new File(fileName), false);
		TextureData tdata = TextureIO.newTextureData(
				Yeti.get().gl.getGLProfile(),
				new File(fileName),
				true, 
				null);
		textureData.put(name, tdata);
		
		// BLOCKS
		Texture tex = TextureIO.newTexture(tdata);
		//tex.setTexParameteri(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		textures.put(name, tex);
	}
	
	public static void loadAllShaders(String folderName) throws IOException {
		File folder = new File(folderName);
		if(!folder.isDirectory()) {
			throw new IOException("Given parameter is not a directory!");
		}
		// List only vertex shaders, and automatically open their fragment buddies
		FileFilter filter = new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(EXT_VERTEX);
			}
		};
		File[] shaderFiles = folder.listFiles(filter);
		for(File vf : shaderFiles) {
			String name = vf.getName().substring(0, vf.getName().lastIndexOf('.'));
			//File ff = new File(folderName + "/" + name + EXT_FRAGMENT);
			//File gf = new File(folderName + "/" + name + EXT_GEOMETRY);
			loadShader(name);
		}
	}
	
	public static Shader shader(String name) {
		return shaders.get(name);
	}

	public static StaticModel model(String name) {
		return models.get(name);
	}
	
	public static CubeTexture cubeTexture(String name) {
		return cubeTextures.get(name);
	}
	
	public static void cleanUp() {
		GL2GL3 gl = Yeti.get().gl;
		
		for(Shader s : shaders.values()) {
			gl.glDeleteProgram(s.getHandle());
		}
		shaders.clear();
		
		for(StaticModel m : models.values()) {
			m.dispose();		
		}
		models.clear();
		
		for(CubeTexture ct : cubeTextures.values()) {
			ct.dispose(gl);
		}
		cubeTextures.clear();
		
		for(Texture t : textures.values()) {
			t.destroy(gl);
		}
		textures.clear();
		
		for(TextureData td : textureData.values()) {
			td.destroy();
		}
		textureData.clear();
	}
	
	public static Texture texture(String name) {
		return textures.get(name);
	}
	public static TextureData textureData(String name) {
		return textureData.get(name);
	}
}
