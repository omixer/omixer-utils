package io.omixer.utils.utils;

/**
 * Arithmetic helpers.
 *
 * @author <a href="mailto:youssef.darzi@omixer.io">Youssef Darzi</a>
 */
public final class MathUtils {

	/**
	 * A fold change that shows the direction of the change: left ÷ right when left is the larger, −(right ÷ left)
	 * when right is the larger, and 0 when they are equal. A 0 value counts as 1 in the ratio. Values are assumed to be
	 * positive.
	 *
	 * <table border="1">
	 * <caption>Examples</caption>
	 * <tr><th>Left</th><th>Right</th><th>Returned value</th><th>Change</th></tr>
	 * <tr><td>1</td><td>2</td><td>-2</td><td>down</td></tr>
	 * <tr><td>2</td><td>1</td><td>2</td><td>up</td></tr>
	 * <tr><td>1</td><td>0</td><td>1</td><td>up</td></tr>
	 * <tr><td>0</td><td>1</td><td>-1</td><td>down</td></tr>
	 * <tr><td>1</td><td>1</td><td>0</td><td>none</td></tr>
	 * </table>
	 *
	 * @param lhsValue the left value
	 * @param rhsValue the right value
	 * @return the signed fold change
	 */
	public static final double computeRatio(Double lhsValue, Double rhsValue) {

		// 
		double ratio = Double.NaN;

		final Double ZERO = 0d;
		
		int comparison = lhsValue.compareTo(rhsValue);

		if (comparison == 0) {
			// no fold change difference
			ratio = 0;
		} else if (comparison < 0) {
			// if lhs = 0
			if (lhsValue.compareTo(ZERO) == 0) {
				ratio = -rhsValue;
			} else {
				ratio = -(rhsValue / lhsValue);
			}
		} else {
			// lhs > rhs
			if (rhsValue.compareTo(ZERO) == 0) {
				ratio = lhsValue;
			} else {
				ratio = lhsValue / rhsValue;
			}
		}
		
		return ratio;
	}

	
	/**
	 * @param array numbers, possibly none
	 * @return the cumulative sums: element i is the sum of the numbers 0 to i, added in order
	 */
	public static <T extends Number> double[] accumulate(T[] array) {
		double[] accumulator = new double[array.length];
		double sum = 0;
		for (int i = 0; i < array.length; i++) {
			sum += array[i].doubleValue();
			accumulator[i] = sum;
		}
		return accumulator;
	}	
}
