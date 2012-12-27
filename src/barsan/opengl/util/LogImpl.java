package barsan.opengl.util;

import java.io.PrintStream;

public class LogImpl implements Log {

	private String name;
	private PrintStream out;
	
	public LogImpl(String name) {
		this(name, System.out);
	}
	
	public LogImpl(String name, PrintStream out) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void log(String message) {
		out.println(message);
	}

	@Override
	public void log(String message, Throwable cause) {
		out.println(message);
		cause.printStackTrace(out);
	}

}
