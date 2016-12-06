package barsan.opengl.rendering;

import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import barsan.opengl.Yeti;
import com.jogamp.opengl.GL4;

public abstract class Model {
	
	/** Whether GL_TRIANGLES or GL_QUADS is being used. */
	private int faceMode;
	
	/** Actual number of vertices per triangle. */
	protected int pointsPerFace;
	
	/** List of objects containing VBO sub-array coordinates and the material
	 *  required to render each of them */
	protected List<MaterialGroup> defaultMaterialGroups;
	
	/**
	 * This shouldn't be invoked directly in new code, and should be phased out
	 * of the forward renderer as well.
	 * 
	 * @param arrayLength 	How many things are to be drawn. Used so both static
	 * 						and dynamic meshes can polymorphically return their
	 * 						correct element count.
	 */
	@Deprecated
	public void render(int arrayLength) {
		GL gl = Yeti.get().gl;
		gl.glDrawArrays(faceMode, 0, arrayLength);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
	}
	
	public List<MaterialGroup> getDefaultMaterialGroups() {
		return defaultMaterialGroups;
	}
	
	public void setDefaultMaterialGroups(List<MaterialGroup> mGroups) {
		this.defaultMaterialGroups = mGroups;
	}
	
	/** 
	 * The number of data entities this model has (groups of vertex/normal etc.
	 * data.
	 */
	public abstract int getArrayLength();
	
	/** Resets the OpenGL state set by the model's usage. */
	public void cleanUp(int... indices) {
		GL4 gl = Yeti.get().gl;
		for(int el : indices) {
			if(el >= 0) {
				gl.glDisableVertexAttribArray(el);
			}
		}
	}
	
	/**
	 * Cleans up all the native resourses (such as VBOs) used by this model.
	 */
	public abstract void dispose();
	
	/**
	 * Updates faceMode based on pointsPerFace, crashing with an error message
	 * if not using triangles or quads.
	 */
	private void updateFaceMode() {
		if(pointsPerFace == 3) {
			faceMode = GL2.GL_TRIANGLES;
		} else if(pointsPerFace == 4) {
			faceMode = GL2.GL_QUADS;
		} else {
			Yeti.screwed("Disallowed number of points per face (can only be 3 or 4).");
		}
	}
	
	/** Provides a VBO of the model's texture coordinates; FIXME: more generalised approach */
	public abstract VBO getTexCoords();

	public int getFaceMode() {
		return faceMode;
	}

	public void setFaceMode(int faceMode) {
		this.faceMode = faceMode;
	}

	/** @note Also updates faceMode */
	public void setPointsPerFace(int pointsPerFace) {
		this.pointsPerFace = pointsPerFace;
		updateFaceMode();
	}

	public int getPointsPerFace() {
		return pointsPerFace;
	}
}
