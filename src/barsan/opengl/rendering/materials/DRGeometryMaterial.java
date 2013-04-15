package barsan.opengl.rendering.materials;

import barsan.opengl.math.Matrix4;
import barsan.opengl.rendering.RendererState;
import barsan.opengl.resources.ResourceLoader;

public class DRGeometryMaterial extends Material {

	public DRGeometryMaterial() {
		super(ResourceLoader.shader("DRGeometry"));
		addComponent(new WorldTransform());
		addComponent(new TextureComponent());
		setDiffuseMap(ResourceLoader.texture("cubetex"));
		
		throw new Error("Not used. Use techniques instead!");
	}
	

	@Override
	public void setup(RendererState rendererState, Matrix4 modelMatrix) {
		super.setup(rendererState, modelMatrix);
		
		shader.setUVector4f("matAmbient", ambient.getData());
		shader.setUVector4f("matDiffuse", diffuse.getData());
		shader.setUVector4f("matSpecular", specular.getData());
		shader.setU1i("shininess", shininess);
	}

}
