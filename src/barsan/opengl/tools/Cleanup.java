package barsan.opengl.tools;

import java.io.File;

import barsan.opengl.Yeti;
import barsan.opengl.util.Settings;

public class Cleanup {

	static void log(String message) {
		System.out.println("[LOG] " + message);
	}
	
	public static void main(String[] args) {
		File settingsFile = new File(Settings.SETTINGS_FILE);
		if(settingsFile.exists()) {
			log("Found settings file, cleaning up...");
			if(settingsFile.delete()) {
				log("Deleted settings successfully!");
			} else {
				log("Problem deleting settings file! Check your permissions!");
			}
		}
		
		log("Done running cleanup.");
	}
}
