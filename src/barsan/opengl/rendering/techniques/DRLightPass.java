package barsan.opengl.rendering.techniques;

import javax.media.opengl.GL2;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Transform;
import barsan.opengl.rendering.ModelInstance;
import barsan.opengl.rendering.Renderer;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.rendering.lights.DirectionalLight;
import barsan.opengl.rendering.lights.Light.LightType;
import barsan.opengl.rendering.lights.PointLight;
import barsan.opengl.rendering.lights.SpotLight;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.resources.ResourceLoader;

/** 
 * @author Andrei Bârsan
 */
public class DRLightPass extends Technique {

	public DRLightPass() {
		super(ResourceLoader.shader("DRLight"));
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
		
		// Common parameters that don't change from volume to volume 
		// NOTE: since we still keep turning this program on and off, these params
		// keep getting set! No need for that, though, we need to find a way to
		// prevent these extra pointless calls
		program.setUVector2f("screenSize", Yeti.get().settings.width, Yeti.get().settings.height);
		program.setUVector3f("eyeWorldPos", rs.getCamera().getPosition());
	}
	
	@Override
	public void loadMaterial(Material material) {
		// nop, abstract technique
	}
	
	public void drawDirectionalLight(ModelInstance quad, DirectionalLight light, RendererState rs) {
		MVP.setIdentity();
		
		program.setUMatrix4("mvpMatrix", MVP);
		program.setU1i("lightType", LightType.Directional.ordinal());
		
		program.setUVector3f("dirLight.Direction", light.getDirection());
		program.setUVector3f("dirLight.Base.Color", light.getDiffuse());
		program.setU1f("dirLight.Base.AmbientIntensity", 0.0f);
		program.setU1f("dirLight.Base.DiffuseIntensity", light.getDiffuse().a);
				
		if(light.castsShadows()) {
			bindFlatMap(rs);
		} else {
			program.setU1i("useShadows", false);
		}
		
		quad.techniqueRender();
	}
	

	public void drawPointLight(ModelInstance volume, PointLight pointLight, RendererState rs) {
		float scale = pointLight.getBoundingRadius();
		Transform t = new Transform().setTranslate(pointLight.getPosition()).setScale(scale);
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

		if(pointLight.castsShadows()) {
			bindCubeMap(rs);
		} else {
			program.setU1i("useShadows", false);
		}
		
		volume.techniqueRender();
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
		
		if(spotLight.castsShadows()) {
			bindFlatMap(rs);
		} else {
			program.setU1i("useShadows", false);
		}
		
		volume.techniqueRender();
	}
	
	private void bindFlatMap(RendererState rs) {
		program.setU1i("shadowMap", 4);
		rs.gl.glActiveTexture(GL2.GL_TEXTURE0 + 4);
		rs.gl.glBindTexture(GL2.GL_TEXTURE_2D, rs.shadowTexture);
		
		Matrix4 projection = rs.depthProjection;
		Matrix4 view = rs.depthView;
		
		Matrix4 VP = new Matrix4(projection).mul(view);
		
		program.setUMatrix4("vpMatrixShadows", VP);
		program.setUMatrix4("biasMatrix", Renderer.shadowBiasMatrix);
		
		program.setU1i("useShadows", true);
		program.setU1i("shadowQuality", rs.getShadowQuality().getFlag());
	}
	
	private void bindCubeMap(RendererState rs) {
		program.setU1i("cubeShadowMap", 5);
		rs.gl.glActiveTexture(GL2.GL_TEXTURE0 + 5);
		rs.gl.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, rs.getGipsyWagonCubeTex());
		
		program.setU1i("useShadows", true);
		program.setU1f("far", rs.getOmniShadowFar());
		program.setU1i("shadowQuality", rs.getShadowQuality().getFlag());
	}
}
