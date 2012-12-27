package barsan.opengl.util;

public interface Log {
	public String getName();
	public void log(String message);
	public void log(String message, Throwable cause);
}
