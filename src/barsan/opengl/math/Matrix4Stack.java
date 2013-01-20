package barsan.opengl.math;

import java.util.Stack;


/**
 * Holds a stack of matrices within, as well as a bunch of pre-calculated ones.
 * 
 * TODO: use pre-allocated arrays to avoid allocations when pushing
 * 
 * @author Andrei Bârsan
 *
 */
public class Matrix4Stack {

	private Stack<Matrix4> stack = new Stack<>();
	
	// This way, no new computations need to run when we pop a matrix from the stack.
	private Stack<Matrix4> cachedValues = new Stack<>();
	
	/**
	 * Initializes the stack with one identity matrix.
	 */
	public Matrix4Stack() {
		push(new Matrix4());
	}
	
	/**
	 * Pushes a matrix onto the stack and caches the latest value of the multiplications.
	 * @param matrix
	 */
	public void push(Matrix4 matrix) {
		stack.push(matrix);
		if(stack.size() == 1) {
			cachedValues.push(new Matrix4(matrix));
		} else {
			//cachedValues.push(new Matrix4(matrix).mul(cachedValues.peek()));
			cachedValues.push(new Matrix4(cachedValues.peek()).mul(matrix));
		}
	}
	
	/**
	 * @return The popped matrix from the stack, not the current pre-multiplied
	 * transform.
	 */
	public Matrix4 pop() {
		cachedValues.pop();
		return stack.pop();
	}
	
	/**
	 * @return The last inserted matrix.
	 */
	public Matrix4 peek() {
		return stack.peek();
	}
	
	/**
	 * @return The transform resulting from multiplying all the matrices in 
	 * the stack.
	 */
	public Matrix4 result() {
		assert(cachedValues.size() == stack.size());
		return cachedValues.peek();
	}
	
	public void clear() {
		cachedValues.clear();
		stack.clear();
	}
	
	public int getSize() {
		assert(cachedValues.size() == stack.size());
		return stack.size();
	}
}
