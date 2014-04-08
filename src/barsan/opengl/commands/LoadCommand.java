package barsan.opengl.commands;

import barsan.opengl.Yeti;
import barsan.opengl.rendering.Scene;

public class LoadCommand implements YetiCommand {

	@Override
	public String invoke(String[] args) {
		if(args.length > 0) {
			Class<?> scene = null;
			for(Class<?> c : Yeti.getAvailableScenes()) {
				if(c.getSimpleName().toUpperCase().equals(args[0].toUpperCase())) {
					scene = c;
					try {
						Yeti.get().loadScene((Scene) c.newInstance());
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
					break;
				}
			}
			
			if(null == scene) {
				return "Scene " + args[0] + " not found.";
			}
			else {
				return "Loading scene: " + scene.getSimpleName();
			}
		}
		else {
			return "Please specify a scene name";
		}
	}
	
}