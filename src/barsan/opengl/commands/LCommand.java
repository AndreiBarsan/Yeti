package barsan.opengl.commands;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import barsan.opengl.Yeti;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.lights.DirectionalLight;
import barsan.opengl.rendering.lights.Light;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.lights.SpotLight;
import barsan.opengl.rendering.lights.Light.LightType;
import barsan.opengl.util.ReflectUtil;

public class LCommand implements YetiCommand {

	@Override
	public String invoke(String[] args) {
		Scene s = Yeti.get().getCurrentScene();
		try {
			if(args.length <= 2) {
				return "At least 3 parameters needed.";
			}
			
			int id = Integer.parseInt(args[0]);
			List<Light> la = s.getLights();
			if(la.size() <= id) {
				return "Only " + la.size() + " lights available."; 
			}
			
			Light currentLight = la.get(id);
			Class<?> LC = 
				currentLight.getType() == LightType.Directional ? DirectionalLight.class
				: currentLight.getType() == LightType.Point ?	PointLight.class
				: SpotLight.class;
			
			Field lightFields[] = LC.getDeclaredFields();
			Field target = null;
			for(Field f : lightFields) {
				if(f.getName().toUpperCase().equals(args[1].toUpperCase())) {
					target = f;
					break;
				}
			}
			
			if(target == null) {
				return "Field " + args[1] + " not found.";
			}
			
			try {
				String parseParams[] = Arrays.copyOfRange(args, 2, args.length);
				ReflectUtil.setParameter(currentLight, target, parseParams);
				return "Set field " + target.getName() + " to " + Arrays.toString(parseParams);
				
			} catch(IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				return "Bad parameter value(s)";
			}
		
		} catch(NumberFormatException e) {
			return "The first parameter must be a number.";
		}
	} 
}