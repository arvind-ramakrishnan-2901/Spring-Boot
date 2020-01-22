package com.dogbreed.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicationUtils {
	
	/*
	 * Pattern to retrieve the dog name and dog id from the image url
	 */
	private static Pattern URLPATTERN = Pattern.compile("^[^#]*?://.*breeds?/(.*)$");
	
	public static String[] matches(String message) {
		
		Matcher match = URLPATTERN.matcher(message);
		
		String [] breed = null;
		
		if (match.matches()) {
			breed = match.group(1).split("/");
		}
		return breed;
	}
}
