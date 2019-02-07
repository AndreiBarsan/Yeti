package barsan.opengl.tests.math;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import barsan.opengl.math.Rectangle;

public class RectangleTest {

	final static float EPSILON = 0.0001f;

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
		Rectangle faller = new Rectangle(0.77f, 0.68f, 2.0f, 4.0f);
		Rectangle ground = new Rectangle(0.0f, 0.0f, 20.0f, 1.0f);

		Rectangle intersection = faller.intersect(ground);

		assertEquals("The whole width of the faller must be in the intersection.",
				faller.width, intersection.width, EPSILON);

		intersection = ground.intersect(faller);

		assertEquals("The whole width of the faller must be in the intersection.",
				faller.width, intersection.width, EPSILON);

		Rectangle sideMoved = new Rectangle(0.29f, -3.0f, 2.0f, 4.0f);
		Rectangle wall = new Rectangle(2.0f, -9.0f, 2.0f, 16.0f);

		intersection = sideMoved.intersect(wall);
		assertEquals("",
				sideMoved.height, intersection.height, EPSILON);

		intersection = wall.intersect(sideMoved);
		assertEquals("",
				sideMoved.height, intersection.height, EPSILON);

		Rectangle positiveWall = new Rectangle(2.0f, 0.0f, 2.0f, 16.0f);
		intersection = sideMoved.intersect(positiveWall);
		assertEquals(0.29f, intersection.width, EPSILON);
		assertEquals(sideMoved.height + sideMoved.y, intersection.height, EPSILON);
	}

}
