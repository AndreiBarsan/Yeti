package barsan.opengl.rendering.materials;

import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.Fog;
import barsan.opengl.rendering.RendererState;

public class FogComponent implements MaterialComponent {

	@Override
	public void setup(Material m, RendererState rs, Matrix4 modelMatrix) {
		Fog fog = rs.getFog();
		m.shader.setU1i("fogEnabled", 1);
		m.shader.setU1f("minFogDistance", fog.minDistance);
		m.shader.setU1f("maxFogDistance", fog.maxDistance);
		m.shader.setUVector4f("fogColor", fog.color.getData());
	}

	@Override
	public int setupTexture(Material m, RendererState rs, int slot) {
		return 0;
	}

	@Override
	public void cleanUp(Material m, RendererState rs) {
		m.shader.setU1i("fogEnabled", 0);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
