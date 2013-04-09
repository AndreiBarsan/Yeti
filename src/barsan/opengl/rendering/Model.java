package barsan.opengl.rendering;

import javax.media.opengl.GL2;

import barsan.opengl.Yeti;

public abstract class Model {
	/** Whether GL_TRIANGLES or GL_QUADS is being used. */
	private int faceMode;
	/** Actual number of vertices per triangle. */
	protected int pointsPerFace;

	/**
	 * A model should render itself after the whole context has been set up by
	 * the material and then the model instance.
	 * 
	 * Models and batching
	 *  - this shouldn't interfere with the basic planned batching system
	 *  (batch setupMaterial) foreach entry, entry.render
	 *  
	 * @param arrayLength 	How many things are to be drawn. Used so both static
	 * 						and dynamic meshes can polymorphically return their
	 * 						correct element count.
	 */
	public void render(int arrayLength) {
		GL2 gl = Yeti.get().gl;
		gl.glDrawArrays(faceMode, 0, arrayLength);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
	}
	
	/**
	 * Cleans up all the native resourses (such as VBOs) used by this model.
	 */
	public abstract void dispose();
	
	/** 
	 * The number of data entities this model has (groups of vertex/normal etc.
	 * data.
	 */
	public abstract int getArrayLength();
	
	/** Resets the OpenGL state set by the model's usage. */
	public void cleanUp(int... indices) {
		GL2 gl = Yeti.get().gl;
		for(int el : indices) {
			if(el >= 0) {
				gl.glDisableVertexAttribArray(el);
			}
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
		if(pointsPerFace == 3) {
			faceMode = GL2.GL_TRIANGLES;
		} else if(pointsPerFace == 4) {
			faceMode = GL2.GL_QUADS;
		} else {
			Yeti.screwed("Disallowed.");
		}
	}

	public int getPointsPerFace() {
		return pointsPerFace;
	}
}
