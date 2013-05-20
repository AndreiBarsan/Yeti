package barsan.opengl.resources;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;

import barsan.opengl.Yeti;
import barsan.opengl.rendering.AnimatedModel;
import barsan.opengl.rendering.AnimatedModel.Frame;
import barsan.opengl.rendering.CubeTexture;
import barsan.opengl.rendering.Model;
import barsan.opengl.rendering.Shader;
import barsan.opengl.rendering.StaticModel;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

//TODO: switch to manual texture loading here; performance reasons explained:
//http://stackoverflow.com/questions/1927419/loading-pngs-into-opengl-performance-issues-java-jogl-much-slower-than-c-sha
public class ResourceLoader {
	
	public static final String EXT_VERTEX 		= ".vsh";
	public static final String EXT_FRAGMENT 	= ".fsh";
	public static final String EXT_GEOMETRY 	= ".gsh";
	
	public static final String RESBASE = "res/";
	public static final String SHADERBASE = "";
	public static final String MODELBASE = "models/";
	public static final String TEXTUREBASE = "tex/";
	
	static boolean initialized = false;
	
	static HashMap<String, Shader> shaders = new HashMap<>();
	static HashMap<String, StaticModel> models = new HashMap<>();
	static HashMap<String, CubeTexture> cubeTextures = new HashMap<>();
	static HashMap<String, AnimatedModel> animatedModels = new HashMap<>();
	
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

	public static void loadObj(String name, String fileName) {
		try(Scanner s = new Scanner(new File(RESBASE + MODELBASE + fileName))) {
			models.put(name, ModelLoader.fromObj(Yeti.get().gl, s));
		} catch (FileNotFoundException e) {
			Yeti.screwed("Failed to load static Wavefront model.", e);
		}
	}
	
	public static void loadKeyFrameAnimatedObj(String name, String folderName) {
		try {
			File folder = new File(RESBASE + MODELBASE + folderName);
			if(!folder.isDirectory()) throw new IOException("Must specify a directory");
			
			GL2 gl = Yeti.get().gl;
			ArrayList<Frame> frames = new ArrayList<>();
			
			File[] files = folder.listFiles();
			Arrays.sort(files, new Comparator<File>() {
				public int compare(File o1, File o2) {
					String o1n = o1.getName();
					String o2n = o2.getName();
					Integer i1 = Integer.valueOf(o1n.substring(o1n.indexOf('_') + 1, o1n.indexOf('.')));
					Integer i2 = Integer.valueOf(o2n.substring(o1n.indexOf('_') + 1, o2n.indexOf('.')));
					return i1.compareTo(i2);
				}
			});
			
			for(int i = 0; i < files.length; i++) {
				// TODO: maybe only load every X frame (not every single one is needed
				// for a good animation; not a priority in our small game nevertheless)
				File entry = files[i];
				frames.add(new Frame(ModelLoader.fromObj(gl, new Scanner(entry)), 0.1f));
			}
			
			animatedModels.put(name, new AnimatedModel(name, frames));
		} catch (IOException e) {
			Yeti.screwed("Failed to load animated Wavefront model.", e);
		}
	}
	
	public static void loadCubeTexture(String name, String ext) {
		CubeTexture tex = new CubeTexture();
		GLProfile glp = Yeti.get().gl.getGLProfile();
		
		try {
			for(int i = 0; i < 6; i++) {
				String fname = RESBASE + TEXTUREBASE + name + "_" + tex.names[i] + "." + ext;
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

	// TODO: separately cache fragment, vertex and geometry shaders for cross-linking
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
		if(linkRules.containsKey(baseVName)) {
			String other = linkRules.get(baseVName);
			String ext = other.substring(other.lastIndexOf('.'), other.length());
			if(ext.equals(EXT_FRAGMENT)) {
				baseFName = other;
			} else if(ext.equals(EXT_GEOMETRY)) {
				baseGName = other;
			}
		}
		
		File vFile = new File(baseFolder + baseVName);
		File gFile = new File(baseFolder + baseGName);
		File fFile = new File(baseFolder + baseFName);
		
		if(! vFile.exists() || ! fFile.exists()) {
			Yeti.screwed("Incomplete shader [" + name + "]. It needs both a vertex " +
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

	public static void loadTexture(String fileName) {
		String resName = fileName.substring(0, fileName.lastIndexOf('.'));
		System.out.println(resName);
		loadTexture(resName, fileName);
	}
	
	public static void loadTexture(String name, String fileName) {
		try {
		TextureData tdata = TextureIO.newTextureData(
				Yeti.get().gl.getGLProfile(),
				new File(RESBASE + TEXTUREBASE + fileName),
				true, 
				null);
		textureData.put(name, tdata);
		
		// Blocking call; could be improved; but that's something we'll do
		// in the Future<T> :D
		Texture tex = TextureIO.newTexture(tdata);
		textures.put(name, tex);
		
		} catch(Exception e) {
			Yeti.screwed("Error loading texture!", e);
		}
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
			loadShader(name);
		}
	}
	
	public static Shader shader(String name) {
		return shaders.get(name);
	}

	public static StaticModel model(String name) {
		return models.get(name);
	}
	
	public static AnimatedModel animatedModel(String name) {
		return animatedModels.get(name);
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
		
		for(Model m : models.values()) {
			m.dispose();		
		}
		models.clear();
		
		for(AnimatedModel am : animatedModels.values()) {
			am.dispose();
		}
		animatedModels.clear();
		
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
