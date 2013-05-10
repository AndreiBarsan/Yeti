package barsan.opengl.util;

import java.lang.reflect.Field;

import barsan.opengl.Yeti;
import barsan.opengl.math.Vector3;

public class ReflectUtil {

	public static void setParameter(Object obj, Field field, String[] args) throws NumberFormatException, IllegalArgumentException, IllegalAccessException {
		field.setAccessible(true);
		if(field.getType() == float.class) {
			field.set(obj, Float.parseFloat(args[0]));
		}
		else if(field.getType() == Vector3.class) {
			field.set(obj, new Vector3(
					Float.parseFloat(args[0]),
					Float.parseFloat(args[1]),
					Float.parseFloat(args[2])));
		}
		else {
			Yeti.debug("Type not currently supported: " + field.getType().getSimpleName());
		}
		
	}

}
