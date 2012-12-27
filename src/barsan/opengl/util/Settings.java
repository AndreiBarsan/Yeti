package barsan.opengl.util;

import java.util.HashMap;

public class Settings {
	
	public boolean warnings = true;
	public boolean debug = true;
	public int width;
	public int height;
	public boolean playing = false;

	private HashMap<String, Object> customSetting = new HashMap<>();
	
	public void setCustom(String setting, Object value) {
		customSetting.put(setting, value);
	}
	
	public Object getCustom(String setting) {
		return customSetting.get(setting);
	}
}
