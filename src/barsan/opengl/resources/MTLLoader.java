package barsan.opengl.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import barsan.opengl.Yeti;
import barsan.opengl.rendering.materials.Material;

public class MTLLoader {

	public static List<Material> load(String fileName) {
		Yeti.debug("Loading materials from MTL file:" + fileName);
		
		File file = new File(ResourceLoader.RESBASE + "models/" + fileName);
		List<Material> results = new ArrayList<>();
		
		Scanner s = null;
		try {
			s = new Scanner(file);
			Material current = null;
			while(s.hasNextLine()) {
				String line = s.nextLine();
				
				if(line.startsWith("#")) continue;
				
				String tokens[] = line.split("\\s+");
				
				if(tokens.length == 0 || tokens[0].length() == 0) {
					if(current != null) {
						results.add(current);
					}
				}
				
				if(tokens[0].equals("newmtl")) {
					current = new Material();
					current.setName(tokens[1]);
				}
				else if(tokens[0].equals("map_Kd")) {
					String textureName = tokens[1];
					ResourceLoader.loadTexture(textureName);
					current.setDiffuseMap(ResourceLoader.texture(
							textureName.substring(0, textureName.lastIndexOf('.'))
							));
				}
				else if(tokens[0].equals("map_Bump")) {
					String textureName = tokens[1];
					ResourceLoader.loadTexture(textureName);
					current.setNormalMap(ResourceLoader.texture(
							textureName.substring(0, textureName.lastIndexOf('.'))
							));
				}
			}
		} catch (FileNotFoundException e) {
			Yeti.screwed("Tried to load non-existent .mtl file: " + file.getAbsolutePath());
		} finally {
			if(null != s) {
				s.close();
			}
		}
		
		return results;
	}
}
