package barsan.opengl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;

import barsan.opengl.Yeti;

public class Settings implements Serializable {
	// The configuration layout is changing fast, and persistance is not really
	// important yet
	private static final long serialVersionUID = 42L;
	
	// Not to be saved
	public transient int width;
	public transient int height;
	public transient boolean playing = false;
	
	private final static transient String SETTINGS_FILE = "settings.dat";
	
	// Actual settings and stats
	public int lastSceneIndex = 0; 
	public int anisotropySamples = 1;
	
	// Allows any sort of custom entries to be saved and read
	private HashMap<String, Object> customSetting = new HashMap<>();
	
	public void setCustom(String setting, Object value) {
		customSetting.put(setting, value);
	}
	
	public Object getCustom(String setting) {
		return customSetting.get(setting);
	}
	
	public void clearCustom(String setting) {
		customSetting.remove(setting);
	}
	
	public static Settings load() {
		Object result = null;
		try(InputStream es = new FileInputStream(SETTINGS_FILE)) {
			if(es != null) {
				ObjectInputStream oi = new ObjectInputStream(es);
				result = oi.readObject();
				oi.close();
			}
		} catch(IOException e) {
			Yeti.debug("No previous settings found - creating new profile!");
			result = new Settings();
		} catch(ClassNotFoundException e) {
			Yeti.screwed("Problem loading settings!", e);
		}
		
		if(! (result instanceof Settings)) {
			Yeti.warn("Incompatible settings object loaded. Creating new profile.");
			result = new Settings();
		}
	
		Settings settings = (Settings)result;
		return settings;
	}
	
	public static void save(Settings settings) {
		synchronized(settings) {
			File outFile = new File(SETTINGS_FILE);
			try(OutputStream os = new FileOutputStream(outFile)) {
				ObjectOutputStream out = new ObjectOutputStream(os);
				out.writeObject(settings);
				out.close();
			} catch (IOException e) {
				Yeti.screwed("Cannot save settings!", e);
			}
		}
	}
}
