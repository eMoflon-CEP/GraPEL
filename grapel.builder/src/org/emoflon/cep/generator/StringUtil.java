package org.emoflon.cep.generator;

public final class StringUtil {
	
	/**
	 * Creates a string, that is a copy of the input but the first character is upper case
	 * @param str the input string, that should start with an upper case character
	 * @return the input string with the first character in upper case
	 */
	public static String firstToUpper(String str) {
		return str.substring(0, 1).toUpperCase()+str.substring(1);
	}
	
	/**
	 * Gets the last segment of a string
	 * @param str the string to be segmented
	 * @param separator the separator that splits the input string
	 * @return the last segment of the string
	 */
	public static String lastSegment(String str, String separator) {
		String[] split = str.split(separator);
		if(split.length <= 0)
			return str;
		
		return split[split.length-1];
	}
	
}
