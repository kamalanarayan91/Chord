import java.util.*;
import java.io.*;


public class Utilities
{
	public static final int stabilizationTimeout = 2000;// every one second
	public static final int fixFingersTimeout = 1000;// every second
	public static final int totalNodes = 8;	
	public static final int m = 3; // 2 ^ 3

	/**
	 * Checks if the key is between a and b.
	 * @param  key [description]
	 * @param  a   [description]
	 * @param  b   [description]
	 * @return     [description]
	 */
	public static boolean checkRange(int key, int a, int b)
	{
		if(key > a && key < b)
			return true;
		return false;
	}
}