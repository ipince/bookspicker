package com.bookspicker.server.data.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.bookspicker.server.data.tools.DataUtils.Material;
import com.bookspicker.shared.School;
import com.bookspicker.shared.Term;
import com.zenkey.net.prowser.Prowser;
import com.zenkey.net.prowser.Request;
import com.zenkey.net.prowser.Tab;

/**
 * This is the FIRST step in a data scrape. It fetches book data
 * from The Coop and saves it to a file. The data saved is the
 * RAW data obtained from the Coop's website.
 * 
 * The Coop stores its class information with sectionIds. That
 * is, a "class" is identified by a sectionId. In order to get
 * the sectionIds for the classes we're interested in, we first
 * need to get the classIds. To get the classIds, we need to get
 * the courseIds. Finally, to get the couseIds we need the termId,
 * as well as the campusId, storeId, and catalogId. These last
 * 4 ids are currently gathered manually. See BnCollegeMapping
 * for details.
 * 
 * Before running a fetch/scrape, be sure to set the
 * <code>FILE_PREFIX</code> variable in <code>DataUtils</code>
 * accordingly.
 * 
 * @author Rodrigo Ipince
 */
public class MitMaterialFetcher {
	
	private static final String[] DONE = new String[] {
	};

	private static final int NUM_THREADS = 4;
	private static final String BASE_URL_START = "http://";
	private static final String BASE_URL_END = ".bncollege.com/webapp/wcs/stores/servlet/";
	
	private static final String DROPDOWN_COMMAND = "TextBookProcessDropdownsCmd";
	private static final String BOOKLIST_COMMAND = "TBListView";
	
	private static final String XPATH_PREFIX = "/body/div[2]/fmt:setlocale/fmt:setbundle/flow:fileref/flow:fileref/fmt:setlocale/fmt:setbundle/div[4]/div[2]/form/";
	private static final String TITLE_XPATH = "div[5]/div/div/ul/li[2]/a";
	private static final String AUTHOR_XPATH = "div[5]/div/div[2]/table/tbody/tr/td[2]/ul/li[1]";
	private static final String EDITION_XPATH = "div[5]/div/div[2]/table/tbody/tr/td[2]/ul/li[2]";
	private static final String PUBLISHER_XPATH = "div[5]/div/div[2]/table/tbody/tr/td[2]/ul/li[3]";
	private static final String ISBN_XPATH = "div[5]/div/div[2]/table/tbody/tr/td[2]/ul/li[4]";
	private static final String NECESSITY_XPATH = "div[5]/div/div/ul/li[3]";
	private static final String NEW_PRICE_XPATH = "div[5]/div/div[2]/table/tbody/tr/td[3]/ul/li[3]/label";
	private static final String USED_PRICE_XPATH ="div[5]/div/div[2]/table/tbody/tr/td[3]/ul/li[2]/label";
//	private static final String AUTHOR_XPATH = "div[5]/table/tbody/tr/td[2]/table/tbody/tr[3]/td/span";
//	private static final String EDITION_XPATH = "div[5]/table/tbody/tr/td[2]/table/tbody/tr[5]/td";
//	private static final String PUBLISHER_XPATH = "div[5]/table/tbody/tr/td[2]/table/tbody/tr[5]/td";
//	private static final String ISBN_XPATH = "div[5]/table/tbody/tr/td[2]/table/tbody/tr[5]/td/div";
//	private static final String NECESSITY_XPATH = "div[5]/table/tbody/tr/td[4]/table/tbody/tr/td/span[@class='Rcolor']";
//	private static final String NEW_PRICE_XPATH = "div[5]/table/tbody/tr/td[4]/table/tbody/tr[5]/td[3]/span";
//	private static final String USED_PRICE_XPATH ="div[5]/table/tbody/tr/td[4]/table/tbody/tr[7]/td[3]/span";
	
	private static final Pattern AUTHOR_PATTERN = Pattern.compile("Author: (.*?)$");
	private static final Pattern EDITION_PATTERN = Pattern.compile("Edition:(.*?)$");
	private static final Pattern PUBLISHER_PATTERN = Pattern.compile("Publisher:(.*?)$");
	private static final Pattern ISBN_PATTERN = Pattern.compile("ISBN:\\D*(((\\d{12}|\\d{9})(\\d|X)|N/A))$");
	private static final Pattern PRICE_PATTERN = Pattern.compile("\\$(.*?)$");
	
	private static final String COURSE_IDS_NAME = "courseIds.dat";
	private static final String CLASS_IDS_NAME = "classIds.dat";
	private static final String SECTION_IDS_NAME = "sectionIds.dat";

	// These were used to find information based on regular expressions. Not used anymore, left here for reference.
//	private static final Pattern selectValue = Pattern.compile("'([0-9]+[A-Z]?\\_?[0-9]*)'");
//	private static final Pattern selectDisplay = Pattern.compile(">(.*)</");
//	private static final Pattern titlePattern = Pattern.compile("title=\\\"(.*?)\\\"></a>", Pattern.MULTILINE);
//	private static final Pattern authorPattern = Pattern.compile("<td><span>([^&\\*]*?)</span></td>", Pattern.MULTILINE);
//	private static final Pattern editionPattern = Pattern.compile("Edition:(.*?)<br", Pattern.MULTILINE);
//	private static final Pattern publisherPattern = Pattern.compile("Publisher:(.*?)<br", Pattern.MULTILINE);
//	private static final Pattern necessityPattern = Pattern.compile("<spanclass=\\\"Rcolor\\\">(.*?)<input", Pattern.MULTILINE);

	private static String filePrefix; // set on main() after determining school/term
	private static String courseIdsFilename;
	private static String classIdsFilename;
	private static String sectionIdsFilename;

	// Use a Prowser to fetch URLs
	private final Prowser prowser = new Prowser();
	private final Tab mainTab = prowser.createTab();
	private final HtmlCleaner cleaner = new HtmlCleaner();
	private final School school;
	private final Term term;
	private final String course;

	/**
	 * Mapping from course (e.g. "6", "21F") to The Coop's
	 * web service id for that course (e.g. "398452234")
	 */
	private Map<String, String> courseIds;
	
	/**
	 * Mapping from a class (e.g. "6-.002") to The Coop's
	 * web service id for that class (e.g. "ALKWJNEF39")
	 */
	private Map<String, String> classIds;
	
	/**
	 * Mapping from a section (e.g. "6-.002-A") to The Coop's
	 * web service id for that section (e.g. "3O94WN54")
	 */
	private Map<String, String> sectionIds;
	
	private BlockingQueue<RequestWrapper> input;
	private BlockingQueue<Pair<RequestWrapper, String>> output;

	public static void main(String[] args) {
		// Choose school and term!
		School school = DataUtils.getSchoolFromUser();
		Term term = DataUtils.getTermFromUser();
		
		filePrefix = DataUtils.DATA_PATH + school.toString() + DataUtils.DIR_SEP +
			term.toString() + DataUtils.DIR_SEP + 
			DataUtils.FILE_DATE_PREFIX + 
			DataUtils.DIR_SEP + DataUtils.MAT_RAW_PATH;
		courseIdsFilename = filePrefix + COURSE_IDS_NAME;
		classIdsFilename = filePrefix + CLASS_IDS_NAME;
		sectionIdsFilename = filePrefix + SECTION_IDS_NAME;
		
		if (!DataUtils.checkPathAndFilePrefix(filePrefix))
			System.exit(1);
		
		MitMaterialFetcher fetcher = new MitMaterialFetcher(school, term, null);
		fetcher.run();
	}
	
	public MitMaterialFetcher(School school, Term term, String course) {
		if (school == null || term == null)
			throw new IllegalArgumentException("School and Term cannot be null!");
		this.school = school;
		this.term = term;
		this.course = course;
	}
	
	public void run() {
		long start = System.currentTimeMillis();
		boolean refreshCourseIds = false;
		boolean refreshClassIds = false;
		boolean refreshSectionIds = false; // TODO: set with args or something
		boolean success;
		
		// Retrieve course -> courseId mappings, and save to file if necessary
		success = getCourseIds(refreshCourseIds);
		if (refreshCourseIds && success)
			writeToFile(courseIdsFilename, courseIds);
		if (!success) {
			System.err.println("Error retrieving course ids");
			return;
		}

		DataUtils.sleep();
		
		getClassIds(refreshClassIds);
		if (refreshClassIds)
			writeToFile(classIdsFilename, classIds);
		
		getSectionIds(refreshSectionIds);
		if (refreshSectionIds)
			writeToFile(sectionIdsFilename, sectionIds);
		
//		dumpData(sectionIds, "section ids");
		getMaterials();
		
		System.out.println("Took: " + (System.currentTimeMillis() - start) + "ms");
	}

	/**
	 * Retrieves the course->courseId mappings, either from file
	 * or from the web service itself.
	 * 
	 * If forceRequest is false, will try to read the codes from
	 * the saved file. If it fails (or if forceRquest is true), it
	 * will hit the web service and parse the ids.
	 * 
	 * @param forceRequest true if you want the ids to be
	 * retrieved from the web service rather than from file.
	 * @return true if ids were retrieved successfully
	 */
	private boolean getCourseIds(boolean forceRequest) {	    

		// start with a clean map
		courseIds = new HashMap<String, String>();

		if (!forceRequest) {
			try {
				readDataFromFile(courseIdsFilename, courseIds);
				return true;
			} catch (IOException e) {
				// Error reading file, continue to send request
				System.err.println("Warning! Reading file failed. Hitting web service");
			}
		}

		Request req = buildIdRequest("", "", "");
		if (req == null) {
			System.err.println("Error! Could not build courses request");
			return false;
		}
		
		String courseSelect = mainTab.go(req).getPageSource();
		String id;
		try {
			TagNode node = cleaner.clean(courseSelect);
			Object[] options = node.evaluateXPath("/body/select/option");
			TagNode option;
			String course;
			for (Object obj : options) {
				if (obj instanceof TagNode) {
					option = (TagNode) obj;
					id = option.getAttributeByName("value");
					course = option.getText().toString();
					if (!id.isEmpty()) {
						courseIds.put(course, id);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (XPatherException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	private void getClassIds(boolean forceRequest) {

		// start with a clean map
		classIds = new HashMap<String, String>();

		if (!forceRequest) {
			try {
				readDataFromFile(classIdsFilename, classIds);
				return;
			} catch (IOException e) {
				// Error reading file, continue to send request
				System.err.println("Warning! Reading file failed. Hitting web service");
			}
		}

		String classSelect;
		Request req;
		String clas;
		String id;
		int n = 0;
		for (String course : courseIds.keySet()) {
			
			req = buildIdRequest(courseIds.get(course), "", "");
			if (req == null) {
				System.err.println("Warning! Could not build classes request for course " +
						course + ". Class ids will be incomplete");
				continue;
			}
			
			classSelect = mainTab.go(req).getPageSource();
			try {
				TagNode node = cleaner.clean(classSelect);
				Object[] options = node.evaluateXPath("/body/select/option");
				TagNode option;
				for (Object obj : options) {
					if (obj instanceof TagNode) {
						option = (TagNode) obj;
						clas = option.getText().toString();
						id = option.getAttributeByName("value");
						if (!id.isEmpty())
							classIds.put(course + DataUtils.PARTS_SEP + clas, id);
					}
				}
			} catch (IOException e) {
				System.err.println("Warning! Could not get all class ids for course " +
						course + ". Class ids will be incomplete");
				e.printStackTrace();
				continue;
			} catch (XPatherException e) {
				System.err.println("Warning! Could not get all class ids for course " +
						course + ". Class ids will be incomplete");
				e.printStackTrace();
				continue;
			}
			n++;
			System.out.print(".");
			if ((n % 30) == 0)
				System.out.println();
			DataUtils.sleep();
		}
	}

	private void getSectionIds(boolean forceRequest) {
		
		// start with clean map
		sectionIds = new HashMap<String, String>();

		if (!forceRequest) {
			try {
				readDataFromFile(sectionIdsFilename, sectionIds);
				return;
			} catch (IOException e) {
				// Error reading file, continue to send request
				System.err.println("Warning! Reading file failed. Hitting web service");
			}
		}

		String sectionSelect;
		String course;
		int currentSections = 0;
		StringBuilder sb;
		Request req;
		List<String> errors = new ArrayList<String>();
		String section;
		String id;
		int n = 0;
		for (String clas : classIds.keySet()) {
			sb = new StringBuilder();
			course = clas.split(DataUtils.PARTS_SEP)[0];
			sb.append("Query for " + clas + "; course is " + course);
			sb.append("\n");
			
			req = buildIdRequest(courseIds.get(course), classIds.get(clas), "");
			if (req == null) {
				System.err.println("Warning! Could not build sections request for class " +
						clas + ". Section ids will be incomplete");
				continue;
			}
			
			sectionSelect = mainTab.go(req).getPageSource();
			try {
				currentSections = 0;
				TagNode node = cleaner.clean(sectionSelect);
				Object[] options = node.evaluateXPath("/body/select/option");
				TagNode option;
				for (Object obj : options) {
					if (obj instanceof TagNode) {
						option = (TagNode) obj;
						section = option.getText().toString();
						id = option.getAttributeByName("value");
						if (!id.isEmpty()) {
							sectionIds.put(clas + DataUtils.PARTS_SEP + section, id);
							currentSections++;
						}
					}
				}
			} catch (IOException e) {
				System.err.println("Warning! Could not get all section ids for class " +
						clas + ". Section ids will be incomplete");
				e.printStackTrace();
				continue;
			} catch (XPatherException e) {
				System.err.println("Warning! Could not get all section ids for course " +
						clas + ". Section ids will be incomplete");
				e.printStackTrace();
				continue;
			}
			
			sb.append("Found " + currentSections + " sections for this class");
			if (currentSections == 0) {
				sb.append("  <--------- WARNING!!!!");
				sb.append("\n");
				errors.add(sb.toString());
			}
			n++;
			System.out.print(".");
			if ((n % 30) == 0)
				System.out.println();
			DataUtils.sleep();
		}
		System.out.println("Found " + sectionIds.size() + " sections in total");
		
		for (String e : errors)
			System.out.println(e);
		
	}
	
	private void getMaterials() {
		// Set up multiple workers to do the fetching
		input = new LinkedBlockingQueue<RequestWrapper>();
		output = new LinkedBlockingQueue<Pair<RequestWrapper, String>>();
		Thread thread;
		for (int i = 0; i < NUM_THREADS; i++) {
			thread = new Thread(new SeleniumAsyncFetcher(input, output));
			thread.start();
		}
		
		if (course != null) {
			getMaterials(course);
		} else {
			for (String course : courseIds.keySet()) {
				getMaterials(course);
			}
		}
	}

	private void getMaterials(String course) {
		Request req;
		String html = null;
		
		List<Material> materials = new ArrayList<Material>();
		List<String> errors = new ArrayList<String>();
		
		List<String> sortedSectionIds = new ArrayList<String>(sectionIds.keySet());
		Collections.sort(sortedSectionIds);

		StringBuilder stats = new StringBuilder();
		stats.append("Stats for The Coop data scrape\n\n");
//		stats.append("Number of courses: " + sortedCourseIds.size() + "\n");
		stats.append("Number of sections for course " + course + ": " + sortedSectionIds.size() + "\n");

		// Build all the requests and put them in the input queue
		int numRequests = 0;
		int numResponses = 0;
		List<String> done = Arrays.asList(DONE);
		if (done.contains(course)) {
			System.out.println("Not retrieving course " + course + " since it's done");
			return;
		} else {
			System.out.println("Retrieving materials for course " + course);
			System.out.println();
			
			for (String section : sortedSectionIds) {
				
				if (!section.startsWith(course + DataUtils.PARTS_SEP))
					continue;
				
				String sectionId = sectionIds.get(section);
				// Need to remove chars from section id for page to load properly
				sectionId = sectionId.replaceAll("[Y|N].*", "");
				
				System.out.println("Adding request for section: " + section + " (" + sectionId + ")");

				req = buildMaterialRequest(sectionId);
				if (req == null) {
					System.err.println("Warning! Could not build material request for section " +
							sectionId + ". Materials will be incomplete!");
					continue;
				}
				
				// Put the request in the queue
				try {
					input.put(new RequestWrapper(section, sectionId, req));
					numRequests++;
				} catch (InterruptedException e) {
					System.err.println("Interrupted while putting in a request!");
					e.printStackTrace();
				}
			}
		}
		
		// Now just retrieve responses from the output queue and
		// process them
		Pair<RequestWrapper, String> response;
		String section, sectionId;
		while (numResponses < numRequests) { // while there are unhandled requests
			try {
				response = output.take();
				section = response.getFirst().getSection();
				sectionId = response.getFirst().getSectionId();
				req = response.getFirst().getRequest();
				html = response.getSecond();
				System.out.println("Parsing response for section " + section + " (" + sectionId + ")");
				
				try {
					int matches = buildMaterial(req.getUri().toASCIIString(), html, section, sectionId, materials, errors);
					if (matches >= 0) {
						System.out.println("Successfully parsed " + matches + " materials");
						numResponses++;
					} else {
						// Try again by putting it back in the input queue
						System.err.println("Failed to parse response html for section " + section + " (" + sectionId + ").");
						numResponses++;
//						input.put(response.getFirst());
					}
				} catch (IOException e) {
					e.printStackTrace();
					errors.add("Section " + section + " (" + sectionId + "): IOException");
				} catch (XPatherException e) {
					e.printStackTrace();
					errors.add("Section " + section + " (" + sectionId + "): XPatherException");
				}
			} catch (InterruptedException e1) {
				System.err.println("Interrupted while getting or putting a request!");
				e1.printStackTrace();
			}
		}
		stats.append("Found " + materials.size() + " materials for course " + course + "\n");
			
		// save materials for course
		writeMaterialsToFile(course, materials);
		writeStatsAndErrors(stats.toString(), errors, course);
		System.out.println();
		System.out.println("Found " + materials.size() + " materials for course " + course);
	}
	
	public int buildMaterial(String req, String html, String section,
			String sectionId, List<Material> materials, List<String> errors)
			throws IOException, XPatherException {
		TagNode root;
		Object[] titles, authors, editions, publishers, isbns, necessities, newPrices, usedPrices;
		Matcher authorMatcher, editionMatcher, publisherMatcher, isbnMatcher, usedPriceMatcher, newPriceMatcher;
		boolean authorFound, editionFound, publisherFound, isbnFound, usedPriceFound, newPriceFound;
		int matches = 0;
		
		root = cleaner.clean(html);
		titles = root.evaluateXPath(XPATH_PREFIX + TITLE_XPATH);
		authors = root.evaluateXPath(XPATH_PREFIX + AUTHOR_XPATH);
		editions = root.evaluateXPath(XPATH_PREFIX + EDITION_XPATH);
		publishers = root.evaluateXPath(XPATH_PREFIX + PUBLISHER_XPATH);
		isbns = root.evaluateXPath(XPATH_PREFIX + ISBN_XPATH);
		necessities = root.evaluateXPath(XPATH_PREFIX + NECESSITY_XPATH);
		newPrices = root.evaluateXPath(XPATH_PREFIX + NEW_PRICE_XPATH);
		usedPrices = root.evaluateXPath(XPATH_PREFIX + USED_PRICE_XPATH);
		
		if (titles.length == authors.length &&
				titles.length == editions.length &&
				titles.length == publishers.length &&
				titles.length == isbns.length &&
				titles.length == necessities.length &&
				titles.length == newPrices.length &&
				titles.length == usedPrices.length) {
			
			if (titles.length == 0) {
				// no books
				return 0;
			}
			
			for (int i = 0; i < titles.length; i++) {
				
				if (titles[i] instanceof TagNode &&
						authors[i] instanceof TagNode &&
						editions[i] instanceof TagNode &&
						publishers[i] instanceof TagNode &&
						isbns[i] instanceof TagNode &&
						necessities[i] instanceof TagNode &&
						newPrices[i] instanceof TagNode &&
						usedPrices[i] instanceof TagNode) {
					authorMatcher = AUTHOR_PATTERN.matcher(((TagNode) authors[i]).getText().toString().trim());
					authorFound = authorMatcher.find();
					editionMatcher = EDITION_PATTERN.matcher(((TagNode) editions[i]).getText().toString().trim());
					editionFound = editionMatcher.find();
					publisherMatcher = PUBLISHER_PATTERN.matcher(((TagNode) publishers[i]).getText().toString().trim());
					publisherFound = publisherMatcher.find();
					isbnMatcher = ISBN_PATTERN.matcher(((TagNode) isbns[i]).getText().toString().trim());
					isbnFound = isbnMatcher.find();
					usedPriceMatcher = PRICE_PATTERN.matcher(((TagNode) usedPrices[i]).getText().toString().trim());
					usedPriceFound = usedPriceMatcher.find();
					newPriceMatcher = PRICE_PATTERN.matcher(((TagNode) newPrices[i]).getText().toString().trim());
					newPriceFound = newPriceMatcher.find();
					
					if (authorFound && editionFound && publisherFound &&
						isbnFound && usedPriceFound && newPriceFound) {
						materials.add(new Material(section,
								((TagNode) titles[i]).getText().toString().trim(),
								authorMatcher.group(1),
								editionMatcher.group(1),
								publisherMatcher.group(1),
								isbnMatcher.group(1),
								((TagNode) necessities[i]).getText().toString().trim(),
								newPriceMatcher.group(1),
								usedPriceMatcher.group(1),
								req));
						matches++;
					} else {
						System.err.println("Section " + section + " (" + sectionId + "): Edition, publisher, or ISBN not found properly!");
						errors.add("Section " + section + " (" + sectionId + "): Edition, publisher, or ISBN not found properly!");
					}
				} else {
					System.err.println("Section " + section + " (" + sectionId + "): Not all XPath items are TagNodes!");
					errors.add("Section " + section + " (" + sectionId + "): Not all XPath items are TagNodes!");
				}
			}
			return matches;
		} else {
			System.err.println("Section " + section + " (" + sectionId + "): Unequal number matched information!");
			errors.add("Section " + section + " (" + sectionId + "): Unequal number matched information!");
		}
		return -1; // failure
	}

	private Request buildIdRequest(String courseId,
			String classId, String sectionId) {
		BnCollegeMapping mapping = BnCollegeMapping.getMappingsFor(school); // TODO: what if school/term not complete?
		try {
			Request req = new Request(BASE_URL_START + mapping.subdomain + BASE_URL_END + DROPDOWN_COMMAND);
			req.addParameter("campusId", mapping.campusId);
			req.addParameter("termId", mapping.termIds.get(term));
			req.addParameter("deptId", courseId);
			req.addParameter("courseId", classId);
			req.addParameter("sectionId", sectionId);
			req.addParameter("storeId", mapping.storeId);
			req.addParameter("catalogId", mapping.catalogId);
			req.addParameter("langId", "-1");
//			req.addParameter("dojo.transport", "xmlhttp");
//			req.addParameter("dojo.preventCache", "1264056365302");
			
			return req;
		} catch (URISyntaxException e) {
			System.err.println("An error ocurred creating " +
					"the request: ");
			e.printStackTrace();
		}
		return null;
	}
	
	private Request buildMaterialRequest(String sectionId) {
		BnCollegeMapping mapping = BnCollegeMapping.getMappingsFor(school);
		try {
			Request req = new Request(BASE_URL_START + mapping.subdomain + BASE_URL_END + BOOKLIST_COMMAND);
			req.addParameter("storeId", mapping.storeId);
			req.addParameter("langId", "-1");
			req.addParameter("catalogId", mapping.catalogId);
			req.addParameter("savedListAdded", "true");
			req.addParameter("clearAll", "");
			req.addParameter("viewName", "TBWizardView");
			req.addParameter("removeSectionId", "");
			req.addParameter("mcEnabled", "N");
			req.addParameter("section_1", sectionId);
			req.addParameter("numberOfCourseAlready", "0");
//			req.addParameter("viewTextbooks.x", "-1"); // ?
//			req.addParameter("viewTextbooks.y", "-1"); // ?
			req.addParameter("sectionList", "newSectionNumber");
			
			return req;
		} catch (URISyntaxException e) {
			System.err.println("An error ocurred creating " +
					"the request: ");
			e.printStackTrace();
		}
		return null;
	}

	private void readDataFromFile(String filename, Map<String, String> data) throws IOException {
		BufferedReader input =  new BufferedReader(new FileReader(filename));
		String line = null;
		String[] tokens;
		while ((line = input.readLine()) != null) {
			tokens = line.split("\\" + DataUtils.SEP);
			if (tokens.length == 2) {
				data.put(tokens[0], tokens[1]);
			} else {
				System.err.println("Warning! could not parse following line: " + line);
			}
		}
		input.close();
	}

	private void writeToFile(String filename, Map<String, String> data) {

		System.out.println("Writing data to " + filename);
		try {
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			List<String> keys = new ArrayList<String>(data.keySet());
			Collections.sort(keys);
			int count = 0;
			for (String key : keys) {
				out.write(key + DataUtils.SEP + data.get(key));
				out.newLine();
				count++;
			}
			out.close();
			System.out.println("Wrote " + count + " records to " + filename);
		} catch (IOException e) {
			System.err.println("Error writing file: " + e.getMessage());
		}
	}
	
	private void writeMaterialsToFile(String course, List<Material> materials) {
		String filename = filePrefix + course + ".dat";
		System.out.println("Writing data to " + filename);
		try {
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			int count = 0;
			for (Material mat : materials) {
				out.write(mat.toString());
				out.newLine();
				count++;
			}
			out.close();
			System.out.println("Wrote " + count + " records to " + filename);
		} catch (IOException e) {
			System.err.println("Error writing file: " + e.getMessage());
		}
	}
	
	private void writeStatsAndErrors(String stats, List<String> errors, String course) {
		String filename = filePrefix + course + "-stats.dat";
		System.out.println("Writing data to " + filename);
		try {
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(stats);
			for (String err : errors) {
				out.write("Error ocurred with " + err);
				out.newLine();
			}
			out.close();
			System.out.println("Wrote stats successfully");
		} catch (IOException e) {
			System.err.println("Error writing file: " + e.getMessage());
		}		
	}
	
	/**
	 * Prints all the data the given Map to console (used for debugging).
	 */
	@SuppressWarnings("unused")
	private void dumpData(Map<String, String> data, String title) {
		System.out.println("==== Data dump: " + title + " (" + data.size() + ") ====");
		List<String> keys = new ArrayList<String>(data.keySet());
		Collections.sort(keys);
		for (String key : keys)
			System.out.println(key + " -> " + data.get(key));
	}

}
