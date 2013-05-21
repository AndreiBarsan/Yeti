package barsan.opengl.rendering;

import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import barsan.opengl.Yeti;
import barsan.opengl.math.Vector3;

import com.jogamp.common.nio.Buffers;

/**
 * Java wrapper for a GL data array stored in the graphics device memory.
 * @author Andrei Barsan
 */
public class VBO {
	
	private final int nativeHandle;
	public final int type;
	private boolean open;
	
	private FloatBuffer localBuffer;
	private int elementGroupCount;
	private int elementSizeOf;
	private int elementType;
	private int elementGroupSize;
	
	/**
	 * Default allocation of a GL_ARRAY_BUFFER of FLOATS in groups of 3.
	 * @param size
	 */
	public VBO(int size) {
		this(GL.GL_ARRAY_BUFFER, size, 3, Buffers.SIZEOF_FLOAT, GL.GL_FLOAT);
	}
	
	/**
	 * Default use case - just assumes we're storing floats in groups of 3 (Vector3s).
	 * @param gl	The GL contex used.
	 * @param type	The type of buffer to create (e.g. GL_ARRAY_BUFFER).
	 * @param size	How many element groups should the buffer be able to hold (assumes each group has three float elements).
	 */
	public VBO(int type, int size) {
		this(type, size, 3, Buffers.SIZEOF_FLOAT, GL.GL_FLOAT);
	}
	
	/**
	 * @param gl	The GL contex used.
	 * @param type	The type of buffer to create (e.g. GL_ARRAY_BUFFER).
	 * @param size	How many element groups should the buffer be able to hold (assumes each group has three float elements).
	 * @param elementGroupSize How many elements in a group (e.g. 3 for 3D coordinates such as vertices, 2 for 2D texture coords).
	 */
	public VBO(int type, int size, int elementGroupSize) {
		this(type, size, elementGroupSize, Buffers.SIZEOF_FLOAT, GL.GL_FLOAT);
	}
	
	public VBO(int type, int elementGroupCount, int elementGroupSize, int elementSizeOf, int elementType) {
		GL2GL3 gl = Yeti.get().gl;
		
		int buff[] = new int[] { -1 };
		gl.glGenBuffers(1, buff, 0);
		nativeHandle = buff[0];
		
		this.type = type;
		this.elementGroupCount = elementGroupCount;
		this.elementGroupSize = elementGroupSize;
		this.elementSizeOf = elementSizeOf;
		this.elementType = elementType;
		
		if(elementSizeOf < 1 || elementSizeOf > 4) {
			Yeti.screwed("Only 1, 2, 3 and 4-byte elements allowed!");
		}
		
		if((type & (GL2.GL_ARRAY_BUFFER | GL2.GL_ELEMENT_ARRAY_BUFFER
				| GL2.GL_NORMAL_ARRAY | GL2.GL_TEXTURE_COORD_ARRAY)) == 0 ) {
			Yeti.screwed("Bad buffer type!");
		}
		
		gl.glBindBuffer(type, nativeHandle);
		gl.glBufferData(type, elementGroupCount * elementGroupSize * elementSizeOf,
				null, GL2.GL_DYNAMIC_DRAW);
		
		localBuffer = gl.glMapBuffer(type, GL2.GL_WRITE_ONLY)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		
		gl.glUnmapBuffer(type);
		
		open = false;
	}
	
	/**
	 * Opens the VBO for quicker mapping.
	 */
	public void open() {
		if(open) {
			Yeti.screwed("VBO already opened!");
		}
		GL2GL3 gl = Yeti.get().gl;
		gl.glBindBuffer(type, nativeHandle);
		gl.glMapBuffer(type, GL2.GL_WRITE_ONLY);
		open = true;
	}
	
	/**
	 * Completes the insertion of data.
	 */
	public void close() {
		if(!open) {
			Yeti.screwed("Closing unopened VBO!");
		}
		GL2GL3 gl = Yeti.get().gl;
		gl.glUnmapBuffer(type);	
		open = false;
	}
	
	public VBO append(float elements[]) {
		localBuffer.put(elements);
		return this;
	}
	
	public VBO append(Vector3 element) {
		localBuffer.put(element.x);
		localBuffer.put(element.y);
		localBuffer.put(element.z);
		return this;
	}
	
	public VBO append(Vector3 elements[]) {
		if(elementGroupSize != 3) {
			warnSizeMismatch();
		}
		
		for(Vector3 v : elements) {
			localBuffer.put(v.x);
			localBuffer.put(v.y);
			localBuffer.put(v.z);
		}		
		return this;
	}
	
	public VBO append(List<Vector3> elements) {
		if(elementGroupSize != 3) { 
			warnSizeMismatch();
		}
			
		for(Vector3 v : elements) {
			localBuffer.put(v.x);
			localBuffer.put(v.y);
			localBuffer.put(v.z);
		}
		return this;
	}
	
	public VBO quickAppend(List<Vector3> elements) {
		open();
		append(elements);
		close();
		return this;
	}
	
	public VBO quickAppend(Vector3[] elements) {
		open();
		append(elements);
		close();
		return this;
	}
	
	public VBO appendReverse(Vector3 elements[]) {
		if(elementGroupSize != 3) {
			warnSizeMismatch();
		}
		
		for(int i = elements.length - 1; i >=0; --i) {
			localBuffer.put(elements[i].x);
			localBuffer.put(elements[i].y);
			localBuffer.put(elements[i].z);
		}		
		return this;
	}
	
	public VBO appendReverse(List<Vector3> elements) {
		if(elementGroupSize != 3) { 
			warnSizeMismatch();
		}
			
		for(int i = elements.size() - 1; i >= 0; --i) {
			localBuffer.put(elements.get(i).x);
			localBuffer.put(elements.get(i).y);
			localBuffer.put(elements.get(i).z);
		}
		return this;
	}
	
	public VBO quickAppendReverse(Vector3 elements[]) {
		open();
		appendReverse(elements);
		close();
		return this;
	}
	
	public VBO quickAppendReverse(List<Vector3> elements) {
		open();
		appendReverse(elements);
		close();
		return this;
	}
	
	/**
	 * Most common use case. Assumes the data in the buffer is composed of float
	 * vectors of three elements each.
	 * 
	 * @param attributeIndex Attribute's index in the shader
	 * @return This object for chaining.
	 */
	public VBO use(int attributeIndex) {
		return useImpl(attributeIndex, elementGroupSize, elementType, false, 0, 0);
	}
	
	private VBO useImpl(int attributeIndex, int groupSize, int dataType, 
			boolean normalized, int stride, long offset) {
		GL2GL3 gl = Yeti.get().gl;
		gl.glBindBuffer(type, nativeHandle);
		gl.glEnableVertexAttribArray(attributeIndex);
		gl.glVertexAttribPointer(	attributeIndex, 
									groupSize, /* THIS IS NR OF STUFF PER GROUP, NOT SIZEOF FLOAT */
									dataType, normalized, stride, offset);
		return this;
	}
	
	public int getSize() {
		return elementGroupCount;
	}
	
	public int getSizeOfElement() {
		return elementSizeOf;
	}

	public int getHandle() {
		return nativeHandle;
	}

	public void cleanUp(int indexUsed) {
		GL2 gl = Yeti.get().gl.getGL2();
		gl.glBindBuffer(type, 0);
		gl.glDisableVertexAttribArray(indexUsed);
	}
	
	private void warnSizeMismatch() {
		Yeti.warn("Putting vector3s in a VBO that has a group size of " + elementGroupSize + " !");
	}
}
