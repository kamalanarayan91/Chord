

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hasher {

	public static String getHashString(String input,long identifierNumber){


		MessageDigest SHA;
		String output ="";
		long Hash = 0;
		try {
			SHA = MessageDigest.getInstance("SHA1");
			byte[] md5sum = SHA.digest(input.getBytes());
			Hash = ((new BigInteger(1, md5sum)).remainder(BigInteger.valueOf(Math.abs(identifierNumber)))).intValue();
			output = Long.toString(Hash);
			//System.out.println(output);
			//output = String.format("%08X", new BigInteger(1, md5sum));
			//System.out.println(output);
			return output;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return output;
	}

}