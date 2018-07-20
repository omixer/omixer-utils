package org.omixer.utils.utils;

public final class MathUtils {
	
	/**
	 * 
	 * 
	 * <div>
	 * <p>
	 * Strategy to highlight fold change. <br />
	 * Note that all values are assumed to be positive.
	 * </p>
	 * <p>
	 * This table summarize the behavior of his method.
	 * </p>
	 * </div> <div>
	 * <table border="1">
	 * <tr>
	 * <td>Left</td>
	 * <td>Right</td>
	 * <td>Ratio L/R</td>
	 * <td>Returned value</td>
	 * <td>Change</td>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td>2</td>
	 * <td>1/2</td>
	 * <td>-2</td>
	 * <td>Down</td>
	 * </tr>
	 * <tr>
	 * <td>2</td>
	 * <td>1</td>
	 * <td>2</td>
	 * <td>2</td>
	 * <td>Up</td>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td>0</td>
	 * <td>1/0</td>
	 * <td>1</td>
	 * <td>Up</td>
	 * </tr>
	 * <tr>
	 * <td>0</td>
	 * <td>1</td>
	 * <td>0/1</td>
	 * <td>-1</td>
	 * <td>Down</td>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td>1</td>
	 * <td>1/1</td>
	 * <td>0</td>
	 * <td>Null</td>
	 * </tr>
	 * </table>
	 * </div>
	 * 
	 * @param lhsValues
	 * @param rhsValues
	 * @return
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
	 * Create an array of cumulative sums
	 * 
	 * @param array
	 * @return returns the cumulative sum array
	 */
	public static <T extends Number> double[] accumulate(T[] array) {
		// Initialize the accumulator
		double[] accumulator = new double[array.length];  
		// accumulate
		accumulator[0] = array[0].doubleValue();
		for (int i = 1; i < array.length; i++) {
			accumulator[i] = accumulator[i - 1] + array[i].doubleValue();
		}
		return accumulator;
	}	
}
