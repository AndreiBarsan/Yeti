package barsan.opengl.rendering.techniques;

import barsan.opengl.Yeti;
import barsan.opengl.math.MathUtil;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Transform;
import barsan.opengl.math.Vector3;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.rendering.StaticModel;
import barsan.opengl.rendering.StaticModelInstance;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.lights.SpotLight;
import barsan.opengl.rendering.lights.Light.LightType;
import barsan.opengl.resources.ResourceLoader;

/** 
 * @author Andrei Bârsan
 */
public class DRLightPass extends Technique {
	
	private StaticModel sphere;
	private ModelInstance sphereInstance;
	private ModelInstance coneInstance;

	public DRLightPass() {
		super(ResourceLoader.shader("DRLight"));
		sphere = ResourceLoader.model("DR_sphere");
		sphereInstance = new StaticModelInstance(sphere);
		coneInstance = new StaticModelInstance(ResourceLoader.model("DR_cone"));
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
	
	public void drawSpotLight(ModelInstance volume, SpotLight spotLight, RendererState rs) {
		
		Matrix4 modelMatrix = volume.getTransform().get();
		viewModel.set(view).mul(modelMatrix);
		MVP.set(projection).mul(view).mul(modelMatrix);
		
		program.setUMatrix4("vMatrix", view);
		program.setUMatrix4("mvpMatrix", MVP);
		
		program.setU1i("lightType", LightType.Spot.ordinal());
		program.setUVector3f("spotLight.Base.Base.Color", spotLight.getDiffuse());
		program.setU1f("spotLight.Base.Base.AmbientIntensity", 0.0f);
		program.setU1f("spotLight.Base.Base.DiffuseIntensity", spotLight.getDiffuse().a);
		program.setUVector3f("spotLight.Base.Position", spotLight.getPosition());
		
		program.setU1f("spotLight.Base.Atten.Constant", spotLight.getConstantAttenuation());
		program.setU1f("spotLight.Base.Atten.Linear", spotLight.getLinearAttenuation());
		program.setU1f("spotLight.Base.Atten.Quadratic", spotLight.getQuadraticAttenuation());

		program.setUVector3f("spotLight.Direction", spotLight.getDirection());
		program.setU1f("spotLight.CosInner", spotLight.getCosInner());
		program.setU1f("spotLight.CosOuter", spotLight.getCosOuter());
		program.setU1f("spotLight.Exponent", spotLight.getExponent());
		
		volume.techniqueRender();
	}
	
	public void drawPointLight(PointLight pointLight, RendererState rs) {
		float scale = pointLight.getBoundingRadius();
		Transform t = new Transform().setTranslate(pointLight.getPosition())
				.setScale(scale);
		t.refresh();
		Matrix4 modelMatrix = t.get();
		
		viewModel.set(view).mul(modelMatrix);
		MVP.set(projection).mul(view).mul(modelMatrix);
		
		program.setUMatrix4("vMatrix", view);
		
		program.setUMatrix4("mvpMatrix", MVP);
		program.setU1i("lightType", LightType.Point.ordinal());
		
		program.setUVector3f("pointLight.Base.Color", pointLight.getDiffuse());
		program.setU1f("pointLight.Base.AmbientIntensity", 0.0f);
		program.setU1f("pointLight.Base.DiffuseIntensity", pointLight.getDiffuse().a);
		program.setUVector3f("pointLight.Position", pointLight.getPosition());
		
		program.setU1f("pointLight.Atten.Constant", pointLight.getConstantAttenuation());
		program.setU1f("pointLight.Atten.Linear", pointLight.getLinearAttenuation());
		program.setU1f("pointLight.Atten.Quadratic", pointLight.getQuadraticAttenuation());

		sphereInstance.techniqueRender();
	}

}
