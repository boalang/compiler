package boa.functions;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Special Functions
 * 
 * These functions have special properties, such as variable types, variable
 * numbers of parameters, or parameters that are types rather than values. Some
 * of the syntax used to describe them, e.g. "...", default arguments and
 * overloading, is not part of the Sawzall language.
 * 
 * @author anthonyu
 * 
 */
public class BoaSpecialIntrinsics {
	private static MessageDigest md;
	private static Map<String, String> regexMap;

	static {
		try {
			BoaSpecialIntrinsics.md = MessageDigest.getInstance("SHA");
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		BoaSpecialIntrinsics.regexMap = new HashMap<String, String>();
		BoaSpecialIntrinsics.regexMap.put("int,16", "(0x)?[A-Fa-f0-9]+h?");
		BoaSpecialIntrinsics.regexMap.put("int,10", "[+-]?[0-9]+");
		BoaSpecialIntrinsics.regexMap.put("int,8", "0[0-7]+");
		BoaSpecialIntrinsics.regexMap.put("string", "\\S+");
		BoaSpecialIntrinsics.regexMap.put("time", "[0-9]+");
		BoaSpecialIntrinsics.regexMap.put("fingerprint", "[0-9]+");
		BoaSpecialIntrinsics.regexMap.put("float", "[-+]?[0-9]*\\.?[0-9]+(e[-+]?[0-9]+)?");
	}

	/**
	 * If <em>condition</em> is false, print the <em>message</em> to standard
	 * error, with the prefix assertion failed:, and exit. The message may be
	 * empty or absent altogether.
	 * 
	 * @param condition
	 *            The condition to be checked
	 * 
	 * @param message
	 *            A {@link String} containing the message to be printed upon
	 *            failure
	 * 
	 * @return True iff <em>condition</em> is true
	 */
	@FunctionSpec(name = "assert", formalParameters = { "bool", "string" })
	public static void azzert(final boolean condition, final String message) {
		if (!condition)
			throw new RuntimeException("assertion failed: " + message);
	}

	/**
	 * If <em>condition</em> is false, print the <em>message</em> to standard
	 * error, with the prefix assertion failed:, and exit. The message may be
	 * empty or absent altogether.
	 * 
	 * @param condition
	 *            The condition to be checked
	 * 
	 * @return True iff <em>condition</em> is true
	 */
	@FunctionSpec(name = "assert", formalParameters = { "bool" })
	public static void azzert(final boolean condition) {
		if (!condition)
			throw new RuntimeException("assertion failed");
	}

	private static byte[] longToByteArray(final long l) {
		return new byte[] { (byte) (l >> 56 & 0xff), (byte) (l >> 48 & 0xff), (byte) (l >> 40 & 0xff), (byte) (l >> 32 & 0xff), (byte) (l >> 24 & 0xff),
				(byte) (l >> 16 & 0xff), (byte) (l >> 8 & 0xff), (byte) (l >> 0 & 0xff), };
	}

	private static long byteArrayToLong(final byte[] bs) {
		return (long) (0xff & bs[0]) << 56 | (long) (0xff & bs[1]) << 48 | (long) (0xff & bs[2]) << 40 | (long) (0xff & bs[3]) << 32
				| (long) (0xff & bs[4]) << 24 | (long) (0xff & bs[5]) << 16 | (long) (0xff & bs[6]) << 8 | (long) (0xff & bs[7]) << 0;
	}

	/**
	 * The fingerprintof function returns the 64-bit fingerprint of the
	 * argument, which may be of any type.
	 * 
	 * @param d
	 *            A double to be fingerprinted
	 * 
	 * @return The fingerprint of d
	 */
	@FunctionSpec(name = "fingerprintof", returnType = "fingerprint", formalParameters = { "float" })
	public static long fingerprintOf(final double d) {
		return BoaSpecialIntrinsics.byteArrayToLong(BoaSpecialIntrinsics.md.digest(BoaSpecialIntrinsics.longToByteArray(Double.doubleToRawLongBits(d))));
	}

	/**
	 * The fingerprintof function returns the 64-bit fingerprint of the
	 * argument, which may be of any type.
	 * 
	 * @param s
	 *            A {@link String} to be fingerprinted
	 * 
	 * @return The fingerprint of s
	 */
	@FunctionSpec(name = "fingerprintof", returnType = "fingerprint", formalParameters = { "string" })
	public static long fingerprintOf(final String s) {
		return BoaSpecialIntrinsics.byteArrayToLong(BoaSpecialIntrinsics.md.digest(s.getBytes()));
	}

	/**
	 * The fingerprintof function returns the 64-bit fingerprint of the
	 * argument, which may be of any type.
	 * 
	 * @param bs
	 *            An array of byte to be fingerprinted
	 * 
	 * @return The fingerprint of bs
	 */
	@FunctionSpec(name = "fingerprintof", returnType = "fingerprint", formalParameters = { "bytes" })
	public static long fingerprintOf(final byte[] bs) {
		return BoaSpecialIntrinsics.byteArrayToLong(BoaSpecialIntrinsics.md.digest(bs));
	}

	/**
	 * The fingerprintof function returns the 64-bit fingerprint of the
	 * argument, which may be of any type.
	 * 
	 * @param b
	 *            A boolean to be fingerprinted
	 * 
	 * @return The fingerprint of b
	 */
	@FunctionSpec(name = "fingerprintof", returnType = "fingerprint", formalParameters = { "bool" })
	public static long fingerprintOf(final boolean b) {
		if (b)
			return 1;

		return 0;
	}

	/**
	 * The fingerprintof function returns the 64-bit fingerprint of the
	 * argument, which may be of any type.
	 * 
	 * @param l
	 *            A long to be fingerprinted
	 * 
	 * @return The fingerprint of l
	 */
	@FunctionSpec(name = "fingerprintof", returnType = "fingerprint", formalParameters = { "fingerprint" })
	public static long fingerprintOf(final long l) {
		return BoaSpecialIntrinsics.byteArrayToLong(BoaSpecialIntrinsics.md.digest(BoaSpecialIntrinsics.longToByteArray(l)));
	}

	// TODO: implement new()

	public static String regex(final String type, final long base) {
		if (BoaSpecialIntrinsics.regexMap.containsKey(type + "," + base))
			return BoaSpecialIntrinsics.regexMap.get(type + "," + base);
		else
			throw new RuntimeException("unimplemented");
	}

	public static String regex(final String type) {
		if (BoaSpecialIntrinsics.regexMap.containsKey(type))
			return BoaSpecialIntrinsics.regexMap.get(type);
		else
			throw new RuntimeException("unimplemented");
	}
}
