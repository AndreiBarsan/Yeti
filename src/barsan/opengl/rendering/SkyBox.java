package barsan.opengl.rendering;

import barsan.opengl.Yeti;
import barsan.opengl.math.Matrix4;
import barsan.opengl.math.Matrix4Stack;
import barsan.opengl.rendering.cameras.Camera;
import barsan.opengl.rendering.materials.CubeMapMaterial;

import com.jogamp.opengl.util.texture.Texture;

public class SkyBox extends StaticModelInstance {

	private Camera camera;

	public SkyBox(CubeTexture cubeTexture, Camera toFollow) {
		super(new Cube(Yeti.get().gl, 20.0f, true), new CubeMapMaterial());

		Texture t = cubeTexture.getTexture();
		CubeMapMaterial cmm = (CubeMapMaterial) getMaterial();
		cmm.setTexture(t);
		
		cmm.setWriteDepthBuffer(false);
		cmm.setCheckDepthBuffer(false);
		

		// this.cubeTexture = cubeTexture;
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
}
