package barsan.opengl.rendering;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Transform;
import barsan.opengl.rendering.Nessie.GBuffer;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.resources.ResourceLoader;

/** 
 * @author Andrei Bârsan
 */
public class DRLightPass extends Technique {
	
	private StaticModel sphere;
	private ModelInstance sphereInstance;

	public DRLightPass() {
		super(ResourceLoader.shader("DRLight"));
		sphere = ResourceLoader.model("DR_sphere");
		sphereInstance = new StaticModelInstance(sphere);
	}
	
	// Setup common items
	@Override
	public void setup(RendererState rs) {
		super.setup(rs);
		
		view.set(rs.getCamera().getView());
		projection.set(rs.getCamera().getProjection());
		
		// Note: should this remain like so and just allow custom textures starting
		// with index 3/4 (4 if we start using the 4th component of the GBuffer)?
		program.setU1i("positionMap", 0);
		program.setU1i("colorMap", 1);
		program.setU1i("normalMap", 2);
		
		program.setUVector2f("screenSize", Yeti.get().settings.width, Yeti.get().settings.height);
		program.setUVector3f("eyeWorldPos", rs.getCamera().getPosition());
	}
	
	public void drawPointLight(PointLight pointLight, RendererState rs) {
		
		float scale = pointLight.getBoundingRadius();
		//System.out.println(scale);
		//scale = 4.0f;
		Transform t = new Transform().setTranslate(pointLight.getPosition())
				.setScale(scale);
		t.refresh();
		Matrix4 modelMatrix = t.get();
		
		viewModel.set(view).mul(modelMatrix);
		
		MVP.set(projection).mul(view).mul(modelMatrix);
		
		float matSpecularIntensity = 10.0f;
		float specularPower = 128.0f;
		// TODO: remove unnecessary computations
		
		program.setUMatrix4("mvpMatrix", MVP);
		program.setU1f("matSpecularIntensity", matSpecularIntensity);
		program.setU1f("specularPower", specularPower);
		//program.setU1i("lightType", 0); 	// use for dir/spot?
		
		program.setUVector3f("pointLight.Base.Color", pointLight.getDiffuse());
		program.setU1f("pointLight.Base.AmbientIntensity", 0.5f);
		program.setU1f("pointLight.Base.DiffuseIntensity", pointLight.getDiffuse().a);
		program.setUVector3f("pointLight.Position", pointLight.getPosition());
		
		program.setU1f("pointLight.Atten.Constant", pointLight.getConstantAttenuation());
		program.setU1f("pointLight.Atten.Linear", pointLight.getLinearAttenuation());
		program.setU1f("pointLight.Atten.Quadratic", pointLight.getQuadraticAttenuation());

		
		sphereInstance.techniqueRender();
	}

}
