package silverSol.math;

public class OpenGLMath {

	public static final int BYTES_PER_INT = 4;
	public static final int BYTES_PER_FLOAT = 4;
	
	public static int getIntByteSize(int numberOfInts) {
		return numberOfInts * BYTES_PER_INT;
	}
	
	public static int getFloatByteSize(int numberOfFloats) {
		return numberOfFloats * BYTES_PER_FLOAT;
	}
	
}
