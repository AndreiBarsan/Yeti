package barsan.opengl.tests.math;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import barsan.opengl.math.Rectangle;

public class RectangleTest {

	@Test
	public void testContainsFloatFloat() {
		Rectangle toTest = new Rectangle(-4, -4, 8, 8);
		assertTrue(toTest.contains(0, 0));
		
		assertFalse(toTest.contains(-4, -4));
		assertFalse(toTest.contains(-4, -2));
		assertFalse(toTest.contains(8, 8));
		
		assertTrue(toTest.contains(0, 0));

		assertFalse(toTest.contains(-4.00f, -4.05f));
		assertFalse(toTest.contains(-4.05f, -4.00f));
		assertFalse(toTest.contains(-5.00f, -5.00f));
	}

	@Test
	public void testOverlaps() {
		Rectangle r1 = new Rectangle(3, 4, 2, 5);
		Rectangle r2 = new Rectangle(4, 7, 1, 2);
		Rectangle r3 = new Rectangle(4, 7, 9, 240);
		Rectangle r4 = new Rectangle(24, 10, 3, 4);
		
		assertTrue(r1.overlaps(r2));
		assertTrue(r1.overlaps(r3));
		assertFalse(r1.overlaps(r4));
	}

	@Test
	public void testIntersect() {
		//fail("Not yet implemented");
	}

}
