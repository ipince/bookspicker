package com.bookspicker.server.data.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bookspicker.server.data.tools.DataUtils.ClassBean;
import com.bookspicker.shared.School;
import com.bookspicker.shared.SchoolClass;
import com.bookspicker.shared.Term;

/**
 * Processes class data, fetched from opendata.mit.edu.
 * 
 * To update the Database with the term's class data, first
 * download the "CIS Course Catalog" tab-delimited file from
 * OpenData and save it under the raw class data folder. Then
 * assign SOURCE to the filename and run MitClassDataFetcher.
 * 
 * The description of what each column means can be found here:
 * http://web.mit.edu/warehouse/metadata/fields/cis_course_catalog.html
 * 
 * @author Rodrigo Ipince
 */
public class MitClassDataFetcher {
	
	private static final boolean PROCESS_CLASS_DATA = true;
	private static final boolean BOOK_DATA = false;
	private static final boolean LIBRARIES_DATA = false;

	private static final String SOURCE = "cis_course_catalog.dat"; // needed for class + libraries
	private static final String SOURCE_DEL = "\t"; // tab-delimited
	private static final String DEST = "cis_course_catalog.dat";
	private static final String DEST_DEL = "|";

	private static final String[] COURSES = new String[]{
		"21A", "21F", "21H", "21L", "21M", "21W", "24",
		"AS", "CMS", "CSB", "ESD", "HST", "MAS", "MS", "MS",
		"PBS", "SDM", "SP", "STS", "SWE"
	};

	private static final Pattern isbn = Pattern.compile("[0-9|X]{10}");

	private static BufferedReader data;
	
	private static String filePrefix; // set on main() after determining school/term

	public static void main(String[] args) {
		// Choose school and term!
		School school = DataUtils.getSchoolFromUser();
		Term term = DataUtils.getTermFromUser();
		
		filePrefix = school.toString() + "_" + term.toString() + "_" + DataUtils.FILE_DATE_PREFIX + "_";
		
		if (!DataUtils.checkPathAndFilePrefix(filePrefix))
			System.exit(1);
		
		run(filePrefix + SOURCE);
	}
	
	private static void run(String filename) {
		
		try {
			if (PROCESS_CLASS_DATA) {
				// create input buffer
				System.out.println("Path: " + DataUtils.CLASS_RAW_PATH); // path
				FileReader input = new FileReader(DataUtils.CLASS_RAW_PATH + filename);
				data = new BufferedReader(input);
				convertCisData();
				data.close();
			}
			
			// Hasn't been used since Fall 2009
//			if (BOOK_DATA) {
//				convertJsonBookData();
//			}
//			
//			if (LIBRARIES_DATA) {
//				System.out.println("Path: " + PATH); // path
//				FileReader input = new FileReader(PATH + SOURCE);
//				data = new BufferedReader(input);
//				convertBookData();
//				data.close();
//			}

		} catch (FileNotFoundException e) {
			// fail...
			System.err.println("You must specify the absolute pathname for your data");
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("You must specify the absolute pathname for your data");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void convertBookData() {
		try {
			String line;
			List<String> classTexts = new ArrayList<String>();
			List<String> listings = new ArrayList<String>();
			String[] parts;
			int count = 1;
			
			SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Date now = new Date();
			
			String subject = "";
			String term = "2010FA";
			String isbn = "";
			String required = "REQUIRED";
			String edition = "0";
			String addedBy = "bookspicker";
			String comment = "";
			String modified = timestamp.format(now);
			
			while ((line = data.readLine()) != null) {
				if (count == 1) { // Skip first record (column names)
					count++;
					continue;
				}

//				if (count > 1000 && count < 1500)
//					System.out.println(count + " " + line);
//				count++;

				parts = line.split(SOURCE_DEL);

				if (parts.length == 0) {
					System.err.println("Warning! Skipping record");
					continue;
				}

				if (parts.length != 12) {
					System.err.println("Warning! The following line wasn't parsed correctly. It had only " + parts.length + " parts:");
					System.err.println(line);
				}

				// Clean up and add to list
				for (int i = 0; i < parts.length; i++) {
					// Only care about subject id and isbn
					if (i == 4) { // subject id
						subject = parts[i].trim();
						if (subject.isEmpty()) {
							System.err.println("Warning! Subject is empty; skipping record");
							break;
						}
					} else if (i == 9) { // isbn
						isbn = parts[i].trim();
						isbn = cleanIsbn(isbn);
						if (isbn == null)
							break;
					}
				}
				
				// Do not make entries for duplicates
				if (!subject.isEmpty() && isbn != null &&
						!listings.contains(subject + isbn)) {
					listings.add(subject + isbn);
					classTexts.add(subject + DEST_DEL + term + DEST_DEL + 
							isbn + DEST_DEL + required + DEST_DEL + edition + 
							DEST_DEL + addedBy + DEST_DEL + comment
							+ DEST_DEL + modified);
				}
			}

			// We went through the entire file, so flush to file
//			flushToFile(classTexts, DEST);

		} catch (IOException e) {
			System.out.println("An IO error occurred while reading data");
		}
	}

	/**
	 * Reads the CIS Course Catalog data and extracts the
	 * data points that we care about. The file should have the
	 * following columns (* denotes ones we care about):
	 * 
	 * 0  ACADEMIC_YEAR (*)
	 * 1  SUBJECT_ID (*)
	 * 2  SUBJECT_CODE
	 * 3  SUBJECT_NUMBER
	 * 4  SOURCE_SUBJECT_ID
	 * 5  PRINT_SUBJECT_ID
	 * 6  IS_PRINTED_IN_BULLETIN
	 * 7  DEPARTMENT_CODE
	 * 8  DEPARTMENT_NAME
	 * 9  EFFECTIVE_TERM_CODE
	 * 10 SUBJECT_SHORT_TITLE
	 * 11 SUBJECT_TITLE (*)
	 * 12 IS_VARIABLE_UNITS
	 * 13 LECTURE_UNITS
	 * 14 LAB_UNITS
	 * 15 PREPARATION_UNITS
	 * 16 TOTAL_UNITS
	 * 17 DESIGN_UNITS
	 * 18 GRADE_TYPE
	 * 19 GRADE_TYPE_DESC
	 * 20 GRADE_RULE
	 * 21 GRADE_RULE_DESC
	 * 22 HGN_CODE
	 * 23 HGN_DESC
	 * 24 HGN_EXCEPT
	 * 25 GIR_ATTRIBUTE
	 * 26 GIR_ATTRIBUTE_DESC
	 * 27 COMM_REQ_ATTRIBUTE
	 * 28 COMM_REQ_ATTRIBUTE_DESC
	 * 29 TUITION_ATTRIBUTE
	 * 30 TUITION_ATTRIBUTE_DESC
	 * 31 WRITE_REQ_ATTRIBUTE
	 * 32 WRITE_REQ_ATTRIBUTE_DESC
	 * 33 SUPERVISOR_ATTRIBUTE
	 * 34 SUPERVISOR_ATTRIBUTE_DESC
	 * 35 PREREQUISITES
	 * 36 SUBJECT_DESCRIPTION
	 * 37 JOINT_SUBJECTS (*)
	 * 38 SCHOOL_WIDE_ELECTIVES
	 * 39 MEETS_WITH_SUBJECTS
	 * 40 EQUIVALENT_SUBJECTS
	 * 41 IS_OFFERED_THIS_YEAR (*)
	 * 42 IS_OFFERED_FALL_TERM (*)
	 * 43 IS_OFFERED_IAP
	 * 44 IS_OFFERED_SPRING_TERM (*)
	 * 45 IS_OFFERED_SUMMER_TERM
	 * 46 FALL_INSTRUCTORS
	 * 47 SPRING_INSTRUCTORS
	 * 48 STATUS_CHANGE
	 * 49 LAST_ACTIVITY_DATE (*)
	 * 50 WAREHOUSE_LOAD_DATE (*)
	 */
	private static void convertCisData() {
		
		try {
			String line;
			String[] parts;
			int count = 1;
			int numColumns = 0;
			String[] columns = null;
			
			// Columns
			int academicYearCol = 0;
			int subjectIdCol = 1;
			int subjectTitleCol = 11;
			int jointSubjectsCol = 37;
			int offeredThisYearCol = 41;
			int offeredFallTermCol = 42;
			int offeredSpringTermCol = 44;
			int lastActivityDateCol = 49;
			int warehouseLoadDateCol = 50;
			
			// To parse and output dates
			SimpleDateFormat dateParser = new SimpleDateFormat("yyyyMMdd");
			Date date;
			
			ClassBean fallBean;
			ClassBean springBean;
			
			List<ClassBean> fallClasses = new ArrayList<ClassBean>();
			List<ClassBean> springClasses = new ArrayList<ClassBean>();
			
			while ((line = data.readLine()) != null) {
				if (count == 1) { // first record is column names
					columns = line.split(SOURCE_DEL);
					numColumns = columns.length;
					count++;
					continue;
				}
				count++;

				parts = line.split(SOURCE_DEL);

				if (parts.length == 0) {
					System.err.println("Warning! Skipping record:");
					System.err.println(line);
					continue;
				}

				if (parts.length != numColumns) {
					System.err.println("Warning! The following line wasn't parsed correctly (found " + parts.length + "):");
					System.err.println(line);
					continue;
				}

				// Clean up and add to list
				fallBean = new ClassBean();
				springBean = new ClassBean();
				
				// Year 2010-2011 corresponds to "2011",
				// so we only care about 2011 for Fall 2010
				if (!"2011".equals(parts[academicYearCol].trim()))
					continue;
				
				fallBean.classCode = parts[subjectIdCol].trim();
				springBean.classCode = parts[subjectIdCol].trim();
				
				fallBean.title = parts[subjectTitleCol].trim();
				springBean.title = parts[subjectTitleCol].trim();
				
				if ("Y".equals(parts[offeredThisYearCol].trim())) {
					if ("Y".equals(parts[offeredFallTermCol].trim())) {
						fallBean.term = Term.FALL2010; // TODO: un-hardcode this
					}
					
					if ("Y".equals(parts[offeredSpringTermCol].trim())) {
						springBean.term = Term.SPRING2011; // TODO: un-hardcode this
					}
				}
				
				fallBean.jointSubjects = parts[jointSubjectsCol].trim();
				springBean.jointSubjects = parts[jointSubjectsCol].trim();
				
				try {
					date = dateParser.parse(parts[lastActivityDateCol].trim());
					fallBean.lastActivityDate = date;
					springBean.lastActivityDate = date;
					
					date = dateParser.parse(parts[warehouseLoadDateCol].trim());
					fallBean.warehouseLoadDate = date;
					springBean.warehouseLoadDate = date;
				} catch (ParseException e) {
					// do nothing
					System.err.println("Unable to parse date: " + parts[lastActivityDateCol].trim());
				}
				
				if (fallBean.isComplete()) {
					fallClasses.add(fallBean);
				} else {
					System.err.println("Class incomplete:\n" + fallBean.toString());
				}
				
				if (springBean.isComplete()) {
					springClasses.add(springBean);
				} else {
					System.err.println("Class incomplete:\n" + springBean.toString());
				}
				
			}
			
			System.out.println("Processed " + count + " records");
			System.out.println("Of those, " + fallClasses.size() + " are in the Fall term");
			System.out.println("Of those, " + springClasses.size() + " are in the Spring term");

			// We went through the entire file, so flush to file
			fallClasses.addAll(springClasses); // merge
			writeClassesToFile(fallClasses, filePrefix + DEST);

		} catch (IOException e) {
			System.out.println("An IO error occurred while reading data");
		}
	}

	private static void convertJsonBookData() {
		List<String> bookList = new ArrayList<String>();
		List<String> classTexts = new ArrayList<String>();
		List<String> isbns = new ArrayList<String>();
		
		// First read courses 1 through 22
		for (int c = 1; c <= 22; c++) {
//			convertClassTexts(PATH + SUBPATH + c + ".json", bookList, isbns, classTexts);
		}
		
		// Then read other ones
		for (String name : COURSES) {
//			convertClassTexts(PATH + SUBPATH + name + ".json", bookList, isbns, classTexts);
		}

//		for (String b : bookList)
//			System.out.println(b);
//		for (String c : classTexts)
//			System.out.println(c);
		
		// We went through the entire file, so flush to file
//		flushToFile(bookList, "converted-books.dat");
//		flushToFile(classTexts, "converted-class-books.dat");
	}

	private static void convertClassTexts(String fileName, List<String> bookList,
			List<String> isbns, List<String> classTexts) {
		String jsonStr = "";
		String str;
		String bookStr;
		String subject;
		String isbn;
		String term = "2010FA";
		String required = "REQUIRED";
		String edition = "0";
		String addedBy = "bookspicker";
		String comment = "";
		
		SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date now = new Date();
		
		BufferedReader reader = null;
		
		try {
			FileReader input = new FileReader(fileName);
			reader = new BufferedReader(input);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		try {
			// Read entire file
			while ((str = reader.readLine()) != null)
				jsonStr = jsonStr.concat(str);

			System.out.println(jsonStr);

			// Parse JSON object
			JSONObject json = new JSONObject(jsonStr);
			JSONArray books = json.getJSONArray("items");
			JSONObject book;
			for (int i = 0; i < books.length(); i++) {
				book = books.getJSONObject(i);
				bookStr = convertBook(book, isbns);
				if (bookStr == null)
					continue;
				bookList.add(bookStr);
				subject = book.get("class-textbook-of").toString();
				isbn = cleanIsbn(book.getString("isbn").toString()); // Shouldn't fail
				classTexts.add(subject + DEST_DEL + term + DEST_DEL + 
						isbn + DEST_DEL + required + DEST_DEL + edition + 
						DEST_DEL + addedBy + DEST_DEL + comment
						+ DEST_DEL + timestamp.format(now));
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (JSONException je) {
			je.printStackTrace();
		}
	}

	private static String convertBook(JSONObject book, List<String> isbns) {
		StringBuilder sb = new StringBuilder();
		String[] columns = new String[]{
				"isbn",
				"title",
				"author",
				"year",
				"label",
				"type",
				"publisher"
		};

		String val = "";
		String author = "";
		for (String col : columns) {
			try {
				val = book.get(col).toString();
				if (col.equals("isbn")) { // Clean up ISBN
					if ((val = cleanIsbn(val)) == null || isbns.contains(val)) {
						System.out.println("Skipping record...");
						return null;
					} else {
						isbns.add(val); // add ISBN to list
					}
				} else if (col.equals("title")) { // Clean title
					int index = val.lastIndexOf("/");
					if (index > 0) {
						author = val.substring(index).replace("/", "").trim();
						val = val.substring(0, index).trim();
					}
				} else if (col.equals("author")) {
					if (val.isEmpty() && !author.isEmpty())
						val = author;
				}
				sb.append(val);
				if (!col.equals(columns[columns.length - 1]))
					sb.append(DEST_DEL);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		return sb.toString();
	}

	private static String cleanIsbn(String str) {
		//		System.out.println("Before: " + str);
		str = str.replaceAll("\\(.*\\)", "").replaceAll(":", "").trim();
		if (str.length() == 13)
			str = str.substring(3);
		Matcher m = isbn.matcher(str);
		if (m.find()) { 
			return m.group();
			//			System.out.println(" After: " + str);
		} else {
			System.err.println("Error! Couldn't clean ISBN: " + str);
			return null;
		}
	}

//	private static void flushToFile(List<ClassBean> values, String dest) {
//		try {
//			System.out.println("Writing to: " + PATH + dest);
//			BufferedWriter out = new BufferedWriter(new FileWriter(PATH + dest));
//			for (String line : values)
//				out.write(line + "\n");
//			out.flush();
//			out.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	private static void writeClassesToFile(List<ClassBean> classes, String filename) {
		try {
			//  Create a stream for writing.
			FileOutputStream fos = new FileOutputStream(DataUtils.CLASS_PROCESSED_PATH + filename);

			//  Next, create an object that can write to that file.
			ObjectOutputStream out = new ObjectOutputStream(fos);

			//  Save each object
			int count = 0;
			for (ClassBean classBean : classes) {
				out.writeObject(classBean);
				count++;
			}
			out.flush();
			out.close();
			System.out.println("Wrote " + count + " records to " + filename);
		} catch (IOException e) {
			System.err.println("Error writing file: " + e.getMessage());
		}
	}

}
