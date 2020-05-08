package org.emoflon.cep.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class IOUtils {
	public static File getFile(String path) throws IOException {
		File file = new File(path);
		if(!file.exists())
			throw new IOException("File :"+path+"does not exist!");
		
		return file;
	}
	
	public static String loadTextFile(String path) throws IOException {
		File file = getFile(path);
		String content = new String(Files.readAllBytes(file.toPath()));
		return content;
	}
}
