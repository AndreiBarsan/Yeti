package barsan.opengl.commands;

import barsan.opengl.Yeti;

public class ExitCommand implements YetiCommand {

	@Override
	public String invoke(String[] args) {
		Yeti.quit();
		return "Shutting down Yeti...";
	}
}