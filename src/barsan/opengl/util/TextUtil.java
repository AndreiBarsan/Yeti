package barsan.opengl.util;

public class TextUtil {
	
	/** Splits the input CamelCaseString into a string with spaces Camel Case String. */
	public static String splitCamelCase(String input) {
		// This regex wizardry splits the string while keeping the split token
		// in the "next" group (zero-width positive lookahead)
		String[] parts = input.split("(?=[A-Z])");
		StringBuilder sb = new StringBuilder();
		for(String s : parts) {
			sb.append(s).append(" ");
		}
		return sb.substring(1, sb.length() - 1);
	}
}
