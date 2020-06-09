package org.emoflon.cep.generator;

public final class StringUtil {
	
	public static String firstToUpper(String str) {
		return str.substring(0, 1).toUpperCase()+str.substring(1);
	}
	
	public static String lastSegment(String str, String separator) {
		String[] split = str.split(separator);
		if(split.length <= 0)
			return str;
		
		return split[split.length-1];
	}
	
}
