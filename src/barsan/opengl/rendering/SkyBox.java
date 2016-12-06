package barsan.opengl.rendering;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.rendering.cameras.Camera;
import barsan.opengl.rendering.materials.BasicMaterial;
import barsan.opengl.rendering.materials.CubeMapMaterial;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.rendering.materials.TextureComponent;
import barsan.opengl.rendering.materials.WorldTransform;
import barsan.opengl.resources.ResourceLoader;

import com.jogamp.opengl.util.texture.Texture;

public class SkyBox extends StaticModelInstance {

	private Camera camera;

	static class SkyboxMaterial extends Material {
		
		public SkyboxMaterial(Texture t) {
			super(ResourceLoader.shader("cubeMap"));
			setDiffuseMap(t);
			addComponent(new TextureComponent());
			addComponent(new WorldTransform());
			
			setWriteDepthBuffer(false);
			setCheckDepthBuffer(false);
			
			setIgnoresLights(true);
		}
		
		@Override
		@Deprecated
		public void bindTextureCoodrinates(Model model) {
			// nop
		}
	}
	
	public SkyBox(CubeTexture cubeTexture, Camera toFollow) {
		super(new Cube(Yeti.get().gl, 20.0f, true), new SkyboxMaterial(cubeTexture.getTexture()));
		
		setTransform(new Matrix4());
		camera = toFollow;
		
		setCastsShadows(false);
	}

	@Override
	public void render(RendererState rendererState, Matrix4Stack mstack) {
		getTransform().setMatrix(camera.getView());
		// Invert the camera transform
		// Then clear the rotation, so it stands still around the player. Otherwise,
		// the camera rotation and the skybox rotation cancel each other out!
		getTransform().get().inv().clearRotation();
		
		super.render(rendererState, mstack);
	}
	
	@Override
	public void techniqueRender(RendererState rs) {
		getTransform().setMatrix(camera.getView());
		getTransform().get().inv().clearRotation();
		rs.gl.glDisable(GL.GL_DEPTH_TEST);
		rs.gl.glDepthMask(false);
		
		super.techniqueRender(rs);
		
		rs.gl.glEnable(GL.GL_DEPTH_TEST);
		rs.gl.glDepthMask(true);
	}
}
