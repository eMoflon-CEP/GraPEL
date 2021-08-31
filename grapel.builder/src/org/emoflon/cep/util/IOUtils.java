package org.emoflon.cep.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Utilities for interacting with file IO
 */
public final class IOUtils {
	/**
	 * Gets the file at the given path
	 * @param path to the file
	 * @return the file at the location given by the path
	 * @throws IOException if no file is present at the given path
	 */
	public static File getFile(String path) throws IOException {
		File file = new File(path);
		if(!file.exists())
			throw new IOException("File :"+path+"does not exist!");
		
		return file;
	}
	
	/**
	 * Loads a text file at a given path
	 * @param path to the text file
	 * @return a string with the contents of the file.
	 * @throws IOException if no file is present at the location given by the path
	 */
	public static String loadTextFile(String path) throws IOException {
		File file = getFile(path);
		String content = new String(Files.readAllBytes(file.toPath()));
		return content;
	}
}
