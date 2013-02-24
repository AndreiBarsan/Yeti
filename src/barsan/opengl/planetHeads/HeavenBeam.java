package barsan.opengl.planetHeads;

import barsan.opengl.flat.Entity2D;
import barsan.opengl.math.Rectangle;
import barsan.opengl.rendering.materials.Material;
import barsan.opengl.resources.ResourceLoader;
import barsan.opengl.util.Color;

public class HeavenBeam extends Entity2D {

	public HeavenBeam(float x, float y) {
		super(new Rectangle(x, y, 2f, 100f), false, false, ResourceLoader.model("end"));
		
		graphics.getTransform().updateScale(1f, 8.0f, 1f);
		graphics.setCastsShadows(false);
		Material beamMat = graphics.getMaterial();
		beamMat.setDiffuse(new Color(0.33f, 0.44f, 0.94f, 0.4f));
		beamMat.setAmbient(Color.TRANSPARENTBLACK.copy());
		beamMat.setCheckDepthBuffer(false);
		beamMat.setWriteDepthBuffer(false);
	}
	float angle = 0;
	@Override
	public void update(float delta) {
		angle += delta * 10.0f;
		angle %= 360;
		graphics.getTransform().updateRotation(0.0f, 1.0f, 0.0f, angle);
		super.update(delta);
	}

}
