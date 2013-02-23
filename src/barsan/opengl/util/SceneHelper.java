package barsan.opengl.util;

import barsan.opengl.Yeti;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.OrthographicCamera;
import barsan.opengl.rendering.PerspectiveCamera;
import barsan.opengl.rendering.Scene;
import barsan.opengl.rendering.lights.DirectionalLight;

public class SceneHelper {
	
	/** Does generic initialization work. Helpful for quick prototyping.
	 *  <ul>
	 *  	<li>adds a basic directional light</li>
	 *  </ul>
	 */
	public static void quickSetup(Scene scene) {
		scene.getLights().add(new DirectionalLight(new Vector3(1.0f, 1.0f, 0.0f).normalize()));
	}
	
	public static void quickSetup2D(Scene scene) {
		scene.getLights().add(new DirectionalLight(new Vector3(0.0f, 0.0f, -1.0f)));
		Settings s = Yeti.get().settings;
		OrthographicCamera oc = new OrthographicCamera(s.width, s.height);
		oc.lookAt(new Vector3(0.0f, 0.0f, -100.0f), new Vector3(), new Vector3(0.0f, 1.0f, 0.0f));
		scene.setCamera(oc);		
	}
}
