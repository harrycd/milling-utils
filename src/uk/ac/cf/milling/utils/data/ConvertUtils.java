/**
 * 
 */
package uk.ac.cf.milling.utils.data;

/**
 * Data type conversion - convenience methods.
 * @author Theocharis Alexopoulos
 * @date 23 Sep 2020
 *
 */
public class ConvertUtils {
	/**
	 * @param arr - array to be converted to float[]
	 * @return a float[] array containing elements of arr casted to float
	 */
	public static float[] castToFloatArray(double[] arr) {
		float[] floatArr = new float[arr.length];
		for (int i = 0; i < arr.length; i++) {
			floatArr[i] = (float) arr[i];
		}
		return floatArr;
	}

	/**
	 * @param arr - array to be converted to float[]
	 * @return a float[] array containing elements of arr casted to float
	 */
	public static float[] castToFloatArray(long[] arr) {
		float[] floatArr = new float[arr.length];
		for (int i = 0; i < arr.length; i++) {
			floatArr[i] = (float) arr[i];
		}
		return floatArr;
	}

	/**
	 * @param arr - array to be converted to float[]
	 * @return a float[] array containing elements of arr casted to float
	 */
	public static float[] castToFloatArray(int[] arr) {
		float[] floatArr = new float[arr.length];
		for (int i = 0; i < arr.length; i++) {
			floatArr[i] = (float) arr[i];
		}
		return floatArr;
	}

}
