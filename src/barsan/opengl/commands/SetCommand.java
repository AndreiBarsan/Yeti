package barsan.opengl.commands;

import barsan.opengl.rendering.Renderer;

public class SetCommand implements YetiCommand {

	@Override
	public String invoke(String[] args) {
		if(args.length != 2) {
			return "set requires 2 parameters: a name and a value";
		}
		
		String var = args[0];
		String val = args[1];
		
		switch(var) {
		case "drawaxes":
			if(val.equals(Boolean.TRUE.toString()) || val.equals(Boolean.FALSE.toString())) {
				Renderer.renderDebug = Boolean.parseBoolean(val);
				return "drawaxes set to [" + val + "]";
			}
			else {
				return "[" + val + "] doesn't look like a boolean to me.";
			}
			
		}
		
		return "Unknown setting [" + var + "].";
	}
	
}