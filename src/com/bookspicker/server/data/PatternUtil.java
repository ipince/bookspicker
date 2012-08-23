package com.bookspicker.server.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bookspicker.shared.Constants;

public class PatternUtil {

	private static final Pattern ISBN_REGEX =
		Pattern.compile(Constants.ISBN_REGEX_STR, Pattern.CASE_INSENSITIVE);
	private static final Pattern CLASS_REGEX =
		Pattern.compile(Constants.CLASS_REGEX_STR, Pattern.CASE_INSENSITIVE);
	
	/**
	 * Determines if the given string corresponds to a valid
	 * ISBN number.
	 * 
	 * @param isbnString the string to be tested
	 * @return true if isbnString corresponds to a valid ISBN,
	 * 	false otherwise
	 */
	public static boolean isIsbn(String isbnString) {
		Matcher matcher = ISBN_REGEX.matcher(isbnString);
		return matcher.find();
	}
	
	/**
	 * Determines if the given string represents valid MIT class.
	 * MIT allows classes of the form:
	 * 		6.002
	 * 		21M.011
	 * 		SP.704
	 * 		18.100C
	 * 		MAS.101
	 * 
	 * @param classString the string to be tested
	 * @return true if classString is a valid MIT class, false
	 * 	otherwise.
	 */
	public static boolean isClass(String classString) {
		Matcher matcher = CLASS_REGEX.matcher(classString);
		return matcher.find();
	}

}
