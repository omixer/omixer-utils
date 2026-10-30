package io.omixer.utils.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MathUtilsTestCase {

	protected final double delta = 10e-16;

	@Test
	public void testComputeRatio() {
		assertEquals(-2d, MathUtils.computeRatio(1d, 2d), delta);
		assertEquals(2d, MathUtils.computeRatio(2d, 1d), delta);
		assertEquals(1d, MathUtils.computeRatio(1d, 0d), delta);
		assertEquals(-1d, MathUtils.computeRatio(0d, 1d), delta);
		assertEquals(0d, MathUtils.computeRatio(1d, 1d), delta);
	}

	@Test
	public void testAccumulate() {
		// given an array this function will create cumulative sum
		Integer[] numbers = new Integer[] {1, 2, 3, 4, 5};
		
		double[] accumulator = MathUtils.accumulate(numbers);
		assertEquals(1, accumulator[0], delta);
		assertEquals(3, accumulator[1], delta);
		assertEquals(6, accumulator[2], delta);
		assertEquals(10, accumulator[3], delta);
		assertEquals(15, accumulator[4], delta);
	}

	@Test
	public void accumulateGivesNothingForAnEmptyArray() {
		assertEquals(0, MathUtils.accumulate(new Integer[0]).length);
	}
}
