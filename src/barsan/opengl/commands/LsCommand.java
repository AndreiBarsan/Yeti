package barsan.opengl.commands;

import barsan.opengl.Yeti;

public class LsCommand implements YetiCommand {

	@Override
	public String invoke(String[] args) {
		String out = "Available scenes: ";
		for(Class<?> c : Yeti.getAvailableScenes()) {
			out += c.getSimpleName() + "\n";
		}
		return out;
	}
}