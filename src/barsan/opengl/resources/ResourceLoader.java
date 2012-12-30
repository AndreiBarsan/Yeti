package barsan.opengl.resources;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import javax.media.opengl.GL2;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;

import barsan.opengl.Yeti;
import barsan.opengl.rendering.CubeTexture;
import barsan.opengl.rendering.Model;
import barsan.opengl.rendering.Shader;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

public class ResourceLoader {
	
	static final String EXT_VERTEX 		= ".vsh";
	static final String EXT_FRAGMENT 	= ".fsh";
	
	static boolean initialized = false;
	static GL2 gl;
	
	static HashMap<String, Shader> shaders = new HashMap<>();
	static HashMap<String, Model> models = new HashMap<>();
	static HashMap<String, CubeTexture> cubeTextures = new HashMap<>();
	
	// TODO: refactor this away
	static HashMap<String, Texture> textures = new HashMap<>();
	static HashMap<String, TextureData> textureData = new HashMap<>();
	
	public static void init(GL2 gl) {
		ResourceLoader.gl = gl;
		System.out.println("Resource loader initialized.");
		initialized = true;
		try {
			loadAllShaders("res");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadObj(String name, String fileName) throws IOException {
		Scanner s = new Scanner(new File(fileName));
		models.put(name, Model.fromObj(gl, s));
	}
	
	public static void loadShader(String name, String fileName)
			throws FileNotFoundException {
		loadShader(name, new File(fileName + EXT_VERTEX), new File(fileName + EXT_FRAGMENT));
	}
	
	public static void loadCubeTexture(String name, String ext) {
		CubeTexture tex = new CubeTexture();
		GLProfile glp = gl.getGLProfile();
		
		try {
			for(int i = 0; i < 6; i++) {
				String fname = "res/tex/" + name + "_" + tex.names[i] + "." + ext;
				TextureData data = TextureIO.newTextureData(
						glp,
						new File(fname),
						true, null);
				tex.getTexture().updateImage(gl, data, tex.cubeSlots[i]);
			}
		} catch (GLException | IOException e) {
			Yeti.screwed("Error loading textures", e);
		}
		
		cubeTextures.put(name, tex);
	}
	
	public static void loadShader(String name, File vertexFile, File fragmentFile)
			throws FileNotFoundException {
		
		if(shaders.containsKey(name)) {
			Yeti.warn("Warning, overwriting shader named: " + name);
		}
		
		Scanner vinput = new Scanner(vertexFile);
		Scanner finput = new Scanner(fragmentFile);
		shaders.put(name, new Shader(gl, name,
			vinput.useDelimiter("\\Z").next(),
			finput.useDelimiter("\\Z").next()
		));
		vinput.close();
		finput.close();
	}

	public static void loadTexture(String name, String fileName) throws GLException, IOException {
		//Texture tex = TextureIO.newTexture(new File(fileName), false);
		TextureData tdata = TextureIO.newTextureData(
				gl.getGLProfile(),
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
		for(File f : shaderFiles) {
			String name = f.getName().substring(0, f.getName().lastIndexOf('.'));
			loadShader(name, f, new File(folderName + "/" + name + EXT_FRAGMENT));
		}
	}
	
	public static Shader shader(String name) {
		return shaders.get(name);
	}

	public static Model model(String name) {
		return models.get(name);
	}
	
	public static CubeTexture cubeTexture(String name) {
		return cubeTextures.get(name);
	}
	
	public static void cleanUp() {
		for(Shader s : shaders.values())
			gl.glDeleteProgram(s.getHandle());
		
		for(Model m : models.values())
			m.dispose();		
	}
	
	public static Texture texture(String name) {
		return textures.get(name);
	}
	public static TextureData textureData(String name) {
		return textureData.get(name);
	}
}
