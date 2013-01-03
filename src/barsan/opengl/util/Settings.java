package barsan.opengl.util;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.HashMap;

public class Settings implements Serializable {
	// The configuration layout is changing fast, and persistance is not really
	// important yet
	private static final long serialVersionUID = 42L;
	
	// Not to be saved
	public transient int width;
	public transient int height;
	public transient boolean playing = false;
	
	private final static transient String SETTINGS_FILE = "settings.dat";
	
	// Logging flags
	public boolean warnings = true;
	public boolean debug = true;
	

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
	
	public static void load() {
		Object.class.getResourceAsStream(SETTINGS_FILE);
		
	}
	
	public static void save() {
		
		//OutputStream os = new FileOutputStream(file);
		//ObjectOutputStream out = new ObjectOutputStream(os);
	}
}
