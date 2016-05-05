import java.util.*;
import java.io.*;


public class Utilities
{
	public static final int stabilizationTimeout = 1000;// every two second
	public static final int fixFingersTimeout = 3000;// every second
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
		{
			return true;
		}

		return false;
	}

	public static boolean checkRangeKey(int key,int a,int b)
	{
		// if 
		if(a > b)
		{
			if(key < a && key <=b)
			{
				return true;
			}
			if(key > a)
			{
				return true;
			}
		}

		if(a < b)
		{
			if(key>a && key <=b)
			{
				return true;
			}
		}


		return false;
	}

	public static boolean check(int key,int a,int b)
	{
		if(a<b)
		{
			if(key>a )
			{}
		}
		else
		{

		}
		return false;

	}


}