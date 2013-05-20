package barsan.opengl.rendering;

import barsan.opengl.rendering.materials.Material;

public class MaterialGroup {
	public int beginIndex;
	public int length;
	public Material material;
	public MaterialGroup(int beginIndex, int length, Material material) {
		this.beginIndex = beginIndex;
		this.length = length;
		this.material = material;
	}
	
	protected MaterialGroup(MaterialGroup other) {
		beginIndex = other.beginIndex;
		length = other.length;
		material = other.material;
	}
	
	/**
	 * Returns a copy of the other material group. 
	 * Important: the actual material data object is a shallow copy.
	 */
	public MaterialGroup copy() {
		return new MaterialGroup(this);
	}
}