package org.emoflon.cep.generator;

public final class StringUtil {
	
	public static String firstToUpper(String str) {
		return str.substring(0, 0).toUpperCase()+str.substring(1);
	}
	
	public static String lastSegment(String str, String separator) {
		String[] split = str.split(separator);
		return split[split.length-1];
	}
	
}
