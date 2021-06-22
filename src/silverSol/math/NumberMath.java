package silverSol.math;

public class NumberMath {
	
	/**
	 * Returns the smallest of the values passed as parameters.
	 * @param values The values to be evaluated
	 * @return The smallest of the passed values
	 */
	public static int min(int... values) {
		int lowestInteger = Integer.MAX_VALUE;
		for(int value : values) {
			lowestInteger = (value < lowestInteger) ? value : lowestInteger;
		}
		
		return lowestInteger;
	}
	
	/**
	 * Returns the smallest of the values passed as parameters.
	 * @param values The values to be evaluated
	 * @return The smallest of the passed values
	 */
	public static float min(float... values) {
		float lowestFloat = Float.POSITIVE_INFINITY;
		for(float value : values) {
			lowestFloat = (value < lowestFloat) ? value : lowestFloat;
		}
		
		return lowestFloat;
	}
	
	/**
	 * Returns the index of the smallest of the values passed as parameters.
	 * @param values The values to be evaluated
	 * @return The index of the smallest of the passed values
	 */
	public static int minIndex(int... values) {
		int lowestInteger = Integer.MAX_VALUE;
		int lowestIndex = -1;
		
		for(int i = 0; i < values.length; i++) {
			int value = values[i];
			if(value < lowestInteger) {
				lowestInteger = value;
				lowestIndex = i;
			}
		}
		
		return lowestIndex;
	}
	
	/**
	 * Returns the index of the smallest of the values passed as parameters.
	 * @param values The values to be evaluated
	 * @return The index of the smallest of the passed values
	 */
	public static int minIndex(float... values) {
		float lowestFloat = Float.MAX_VALUE;
		int lowestIndex = -1;
		
		for(int i = 0; i < values.length; i++) {
			float value = values[i];
			if(value < lowestFloat) {
				lowestFloat = value;
				lowestIndex = i;
			}
		}
		
		return lowestIndex;
	}
	
	/**
	 * Returns the largest of the values passed as parameters.
	 * @param values The values to be evaluated
	 * @return The largest of the passed values
	 */
	public static int max(int... values) {
		int highestInteger = -Integer.MAX_VALUE - 1;
		for(int value : values) {
			highestInteger = (value > highestInteger) ? value : highestInteger;
		}
		
		return highestInteger;
	}
	
	/**
	 * Returns the largest of the values passed as parameters.
	 * @param values The values to be evaluated
	 * @return The largest of the passed values
	 */
	public static float max(float... values) {
		float highestFloat = Float.NEGATIVE_INFINITY;
		for(float value : values) {
			highestFloat = (value > highestFloat) ? value : highestFloat;
		}
		
		return highestFloat;
	}
	
	/**
	 * Returns the index of the largest of the values passed as parameters.
	 * @param values The values to be evaluated
	 * @return The index of the largest of the passed values
	 */
	public static int maxIndex(int... values) {
		int highestInteger = -Integer.MAX_VALUE - 1;
		int highestIndex = -1;
		
		for(int i = 0; i < values.length; i++) {
			int value = values[i];
			if(value > highestInteger) {
				highestInteger = value;
				highestIndex = i;
			}
		}
		
		return highestIndex;
	}
	
	/**
	 * Returns the index of the largest of the values passed as parameters.
	 * @param values The values to be evaluated
	 * @return The index of the largest of the passed values
	 */
	public static int maxIndex(float... values) {
		float highestFloat = Float.NEGATIVE_INFINITY;
		int highestIndex = -1;
		
		for(int i = 0; i < values.length; i++) {
			float value = values[i];
			if(value > highestFloat) {
				highestFloat = value;
				highestIndex = i;
			}
		}
		
		return highestIndex;
	}
	
	/**
	 * Returns the given value clamped between the given bounds.
	 * @param value The number to clamp
	 * @param min The minimum bound
	 * @param max The maximum bound
	 * @return The int value clamped between the two bounds
	 */
	public static int clamp(int value, int min, int max) {
		int minClamp = (value < min) ? min : value;
		return (minClamp > max) ? max : minClamp;
	}
	
	/**
	 * Returns the given value clamped between the given bounds.
	 * @param value The number to clamp
	 * @param min The minimum bound
	 * @param max The maximum bound
	 * @return The float value clamped between the two bounds
	 */
	public static float clamp(float value, float min, float max) {
		float minClamp = (value < min) ? min : value;
		return (minClamp > max) ? max : minClamp;
	}
	
	/**
	 * Linearly interpolates between the two bounds.
	 * @param startNumber The first bound
	 * @param finalNumber The second bound
	 * @param interpolationFactor The percentage of the offset to add the the first bound to arrive at the result
	 * @return The interpolated value between the two bounds
	 */
	public static float interpolate(float startNumber, float finalNumber, float interpolationFactor) {
		return startNumber + ((finalNumber - startNumber) * interpolationFactor);
	}
	
	public static float sigmoid(float input) {
		return (float) (1 / (1 + Math.pow(Math.E, -input)));
	}
}
