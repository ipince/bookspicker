package com.bookspicker.server.data.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bookspicker.server.data.tools.DataUtils.Material;
import com.bookspicker.shared.School;
import com.bookspicker.shared.Term;
import com.google.gwt.dev.util.collect.HashMap;

/**
 * This is the SECOND step in a data scrape.
 * Processes raw data obtained from The Coop to remove any
 * abbreviations and other formatting problems.
 * 
 * @author Rodrigo Ipince
 *
 */
public class MitMaterialProcessor {
	
	private static String mainPath; // set on main() after determining school/term
	
	private Map<String, Material> publishers = new HashMap<String, Material>();
	private Set<String> titles = new HashSet<String>();
	private Set<String> words = new HashSet<String>();
	
	public static void main(String[] args) {
		// Choose school and term!
		School school = DataUtils.getSchoolFromUser();
		Term term = DataUtils.getTermFromUser();
		
		mainPath = DataUtils.DATA_PATH + school.toString() + DataUtils.DIR_SEP +
			term.toString() + DataUtils.DIR_SEP + 
			DataUtils.FILE_DATE_PREFIX + 
			DataUtils.DIR_SEP;
		
		new MitMaterialProcessor().run();
	}
	
	private void run() {
		List<Material> materials = new ArrayList<Material>();
		
		boolean printTitles = false;
		boolean searchTitles = false;
		boolean printPublishers = false;
		
		File dir = new File(mainPath + DataUtils.MAT_RAW_PATH);
		// Filter files by name
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		    	System.out.println(name);
		    	return !name.endsWith("Ids.dat") && !name.endsWith("stats.dat");
		    }
		};
		String[] children = dir.list(filter);
		String filename, course;
		
		if (children != null) {
		    for (int i=0; i < children.length; i++) {
		        // Get filename of file or directory
		        filename = children[i];
		        course = filename.substring(0,
		        		filename.indexOf(".dat"));
		        
				// empty material list
				materials.clear();

				// load raw data
				try {
					DataUtils.readRawData(mainPath, filename, materials);
				} catch (IOException e) {
					// skip
					System.err.println("Error processing materials for course " + course);
					continue;
				}

				// process
				for (Material mat : materials) {
					mat.section = processSection(mat.section);
					mat.title = processTitle(mat.title);
					mat.author = processAuthor(mat.author);
					mat.edition = processEdition(mat.edition);
					mat.publisher = processPublisher(mat.publisher, mat);
					mat.necessity = processNecessity(mat.necessity);
				}

				// write data to file
				writeMaterialsToFile(course, materials);
		    }
		}
		
		if (searchTitles) {
			Map<Integer, List<String>> counts = new HashMap<Integer, List<String>>();
			Integer count;
			int numSearches = 0;
			System.out.println("Searching titles...");
			for (String title : titles) {
				if ((numSearches % 80) == 0)
					System.out.println();
				System.out.print(".");
				numSearches++;
				count = GoogleSearch.getSearchResultCount(title);
				if (!counts.containsKey(count))
					counts.put(count, new ArrayList<String>());
				
				counts.get(count).add(title);
				DataUtils.sleep();
			}
			
			List<Integer> sortedCounts = new ArrayList<Integer>(counts.keySet());
			Collections.sort(sortedCounts);
			for (Integer c : sortedCounts)
				for (String title : counts.get(c))
					System.out.println(c + ": " + title);
		}
		
		if (printTitles) {
			loadWords();
			
			System.out.println("The titles are: ");
			List<String> sortedTitles = new ArrayList<String>(titles);
			Collections.sort(sortedTitles);
			String[] tokens;
			Set<String> mispellings = new HashSet<String>();
			for (String title : sortedTitles) {
				tokens = title.split("\\s");
				for (String token : tokens)
					if (!isWord(token))
						mispellings.add(title);
//				System.out.println(title);
			}
			
			for (String mispelling : mispellings)
				System.out.println(mispelling);
			System.out.println("Those were " + mispellings.size() + " potential errors");
		}
		
		if (printPublishers) {
			System.out.println("\nThe publishers are: ");
			List<String> sortedPubs = new ArrayList<String>(publishers.keySet());
			Collections.sort(sortedPubs);
			for (String pub : sortedPubs) {
				System.out.println(pub);
			}
		}

	}

	private boolean isWord(String token) {
		// is in dictionary
		if (words.contains(token))
			return true;
		
		// is a number
		try {
			Double.valueOf(token);
			return true;
		} catch (NumberFormatException e) {
			// do nothing; token failed number test
		}
		
		// is special char
		if (token.equals("a") || token.equals("b") ||
				token.equals("i") || token.equals("ii") ||
				token.equals("iii") || token.equals("u.s.") ||
				token.equals("no.") || token.equals("v1") ||
				token.equals("v2"))
			return true;
		
		return false;
	}

	private void loadWords() {
		try {
			BufferedReader input =  new BufferedReader(new FileReader(DataUtils.DATA_PATH + "dic-0294.txt"));
			String word = null;
			while ((word = input.readLine()) != null) {
				words.add(word);
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String processSection(String section) {
		// Do nothing with this.
		
//		if (parts.length == 3) {
//			if (!parts[1].startsWith("."))
//				parts[1] = "." + parts[1];
//			if (parts[1].contains("/"))
//				parts[1] = parts[1].split("/")[0];
//			
//			section = parts[0] + parts[1].replace(" ", "") + DataUtils.PARTS_SEP + parts[2];
//		}
		return section;
	}

	private String processTitle(String title) {
		title = title.toLowerCase();
		
		// URL encoding and other spacing issues
		title = title.replaceAll(":", ": ");
		title = title.replaceAll("\\(", " (");
		title = title.replaceAll("-", " ");
		title = title.replaceAll(",", ", ");
		title = title.replaceAll("\\+", " and ");
		title = title.replaceAll("&amp;", " and ");
		title = title.replaceAll("&#039;", "'");
		title = title.replaceAll("w\\/", "with ");
		title = title.replaceAll("f\\/", "for ");
		title = title.replaceAll("&gt;", " ");
		title = title.replaceAll("&lt;", " ");
		
		// Common 'first' words
		title = title.replaceAll("fund\\.", "fundamentals ");
		title = title.replaceAll("intro\\.", "introduction ");
		title = title.replaceAll("prin\\.", "principles ");
		
		// Common words
		title = title.replaceAll("anal\\.", "analysis ");
		title = title.replaceAll("appl\\.", "applied ");
		title = title.replaceAll("engr\\.", "engineering ");
		title = title.replaceAll("engin\\.", "engineering ");
		title = title.replaceAll("engineer\\.", "engineering ");
		title = title.replaceAll("sci\\.", "science ");
		
		// Less frequent 'meaningful' words
		title = title.replaceAll("acct\\.", "accounting ");
		title = title.replaceAll("admin\\.", "administration ");
		title = title.replaceAll("adv\\.", "advanced ");
		title = title.replaceAll("amer\\.", "american ");
		title = title.replaceAll("anthol\\.", "anthology ");
		title = title.replaceAll("appr\\.", "approach ");
		title = title.replaceAll("bas\\.", "basis ");
		title = title.replaceAll("bio\\.", "biology ");
		title = title.replaceAll("bound\\.", "boundary ");
		title = title.replaceAll("cent\\.", "century ");
		title = title.replaceAll("chem\\.", "chemical ");
		title = title.replaceAll("circ\\.", "circuits ");
		title = title.replaceAll("civ\\.", "civilization ");
		title = title.replaceAll("civiliz\\.", "civilization ");
		title = title.replaceAll("crit\\.", "criticism ");
		title = title.replaceAll("classrm\\.", "classroom ");
		title = title.replaceAll("comp\\.", "composition ");
		title = title.replaceAll("contemp\\.", "contemporary ");
		title = title.replaceAll("democ\\.", "democracy ");
		title = title.replaceAll("deriv\\.", "derivatives ");
		title = title.replaceAll("determinat\\.", "determination ");
		title = title.replaceAll("dev\\.", "development ");
		title = title.replaceAll("dict\\.", "dictionary ");
		title = title.replaceAll("diff\\.", "differential ");
		title = title.replaceAll("dist\\.", "distribution ");
		title = title.replaceAll("dynam\\.", "dynamics ");
		title = title.replaceAll("econ\\.", "economic ");
		title = title.replaceAll("electron\\.", "electronics ");
		title = title.replaceAll("elect\\.", "electronic ");
		title = title.replaceAll("elem\\.", "elementary ");
		title = title.replaceAll("employ\\.", "employment ");
		title = title.replaceAll("eng\\.", "english ");
		title = title.replaceAll("environ\\.", "environmental ");
		title = title.replaceAll("extraord\\.", "extraordinary ");
		title = title.replaceAll("exer\\.", "exercises ");
		title = title.replaceAll("explor\\.", "exploring ");
		title = title.replaceAll("fran\\.", "france ");
		title = title.replaceAll("geotech\\.", "geotechnical ");
		title = title.replaceAll("gram\\.", "gramatica "); // span
		title = title.replaceAll("govt\\.", "government ");
		title = title.replaceAll("hist\\.", "history ");
		title = title.replaceAll("hlth\\.", "health ");
		title = title.replaceAll("illustrat\\.", "illustrating ");
		title = title.replaceAll("invest\\.", "investment ");
		title = title.replaceAll("interm\\.", "intermed ");
		title = title.replaceAll("intg\\.", "integrated ");
		title = title.replaceAll("lang\\.", "language ");
		title = title.replaceAll("lit\\.", "literature ");
		title = title.replaceAll("lect\\.", "lectures ");
		title = title.replaceAll("math\\.", "mathematical ");
		title = title.replaceAll("mech\\.", "mechanical ");
		title = title.replaceAll("mechan\\.", "mechanics ");
		title = title.replaceAll("mgmt\\.", "management ");
		title = title.replaceAll("mtrls\\.", "materials ");
		title = title.replaceAll("mod\\.", "modern ");
		title = title.replaceAll("narr\\.", "narration ");
		title = title.replaceAll("niv\\.", "niveau "); // french
		title = title.replaceAll("opt\\.", "optimal ");
		title = title.replaceAll("other\\.", "other ");
		title = title.replaceAll("orig\\.", "original ");
		title = title.replaceAll("path\\.", "pathologic ");
		title = title.replaceAll("phys\\.", "physical ");
		title = title.replaceAll("plan\\.", "planning ");
		title = title.replaceAll("poly\\.", "polymers ");
		title = title.replaceAll("pop\\.", "popular ");
		title = title.replaceAll("pow\\.", "power ");
		title = title.replaceAll("prog\\.", "progressive ");
		title = title.replaceAll("progress\\.", "progressive ");
		title = title.replace("prob.concepts", "probability concepts"); // special case
		title = title.replaceAll("prob\\.", "problems ");
		title = title.replaceAll("prod\\.", "production ");
		title = title.replaceAll("proc\\.", "processes ");
		title = title.replaceAll("psych\\.", "psychology ");
		title = title.replaceAll("read\\.", "reading ");
		title = title.replaceAll("react\\.", "reaction ");
		title = title.replaceAll("renormal\\.", "renormalization ");
		title = title.replaceAll("represent\\.", "representation ");
		title = title.replaceAll("revol\\.", "revolution ");
		title = title.replaceAll("sel\\.", "selection ");
		title = title.replaceAll("simp\\.", "simple ");
		title = title.replaceAll("stat\\.", "statistics ");
		title = title.replaceAll("strat\\.", "strategies ");
		title = title.replaceAll("syst\\.", "systems ");
		title = title.replaceAll("thermodyn\\.", "thermodynamics ");
		title = title.replaceAll("trad\\.", "traditional ");
		title = title.replaceAll("thry\\.", "theory ");
		title = title.replaceAll("trans\\.", "transitions ");
		title = title.replaceAll("treas\\.", "treasury ");
		title = title.replaceAll("unabrgd\\.", "unabridged ");
		title = title.replaceAll("var\\.", "variables ");
		title = title.replaceAll("vibrat\\.", "vibrations ");
		title = title.replaceAll("west\\.", "western ");
		title = title.replaceAll("writ\\.", "writing ");
		
		// Post-title words
		title = title.replaceAll("bklt\\.", "booklet ");
		title = title.replaceAll("gde\\.", "guide ");
		title = title.replaceAll("lab\\.", "laboratory ");
		title = title.replaceAll("lev\\.", "level ");
		title = title.replaceAll("man\\.", "manual ");
		title = title.replaceAll("s\\.m\\.", "solution manual ");
		title = title.replaceAll("sol\\.", "solution ");
		title = title.replaceAll("soln\\.", "solution ");
		title = title.replaceAll("std\\.", "study ");
		title = title.replaceAll("stud\\.", "student ");
		title = title.replaceAll("wkbk\\.", "workbook ");
		title = title.replaceAll("wkbk", "workbook ");
		title = title.replaceAll("wrkbk", "workbook ");
		
		// More post-title words (smaller)
		title = title.replaceAll("ed\\.", "edition ");
		title = title.replaceAll("pkg\\.", "package ");
		title = title.replaceAll("pt\\.", "part ");
		title = title.replaceAll("rev\\.", "revised ");
		title = title.replaceAll("updt\\.", "updated ");
		title = title.replaceAll("upd\\.", "updated ");
		title = title.replaceAll("v\\.", "version ");
		title = title.replaceAll("vol\\.", "volume ");
		
		// problematic ones (collision)
		title = title.replaceAll("am\\.", "american ");
		title = title.replaceAll("est\\.", "estate ");
		title = title.replaceAll("bk\\.", "book ");
		
		// Spacing
		title = title.replaceAll("\\.{2,3}", "");
		title = title.replaceAll("\\s\\s+", " ");
		title = title.replaceAll("\\s,", ",");
		title = title.replaceAll("\\s:", ":");
//		title = title.replaceAll("[a-z]\\.", "");
		title = title.replaceAll("dr\\.", "dr ");
		title = title.replaceAll("mr\\.", "mr ");
		title = title.replaceAll("mrs\\.", "mrs ");
		
		// remove punctuation marks
		title = title.replaceAll("\\(.*\\)", "");
		title = title.replaceAll(":", "");
		title = title.replaceAll(",", "");
		title = title.replaceAll("/", " ");
		title = title.replaceAll("!", "");
		title = title.replaceAll("\\?", "");
		
		title = title.trim();
		
		titles.add(title);
		
		if (title.contains("."))
			System.err.println(title);
		
		return title.trim();
	}

	private String processAuthor(String author) {
		author = author.toLowerCase();
		
		author = author.replaceAll("&#039;", "'");
		author = author.replaceAll("-", " ");
		return author;
	}
	
	private String processEdition(String edition) {
		return edition.toLowerCase();
	}

	private String processPublisher(String publisher, Material mat) {
		publisher = publisher.toLowerCase();
		
		// to gather unique publishers (one sample per publisher)
		if (!publishers.containsKey(publisher))
			publishers.put(publisher, mat);
		
		if (publisher.equals("a k peters")) publisher = "a k peters";
//		else if (publisher.equals("am math")) publisher = "";
//		else if (publisher.equals("am s micro")) publisher = "";
		else if (publisher.equals("artech")) publisher = "artech"; // house publishers
		else if (publisher.equals("asce")) publisher = "society civil engineers";
		else if (publisher.equals("ashgate")) publisher = "ashgate"; // publishing
		else if (publisher.equals("athena sci")) publisher = "athena scientific";
		else if (publisher.equals("b+n pub")) publisher = "barnes noble";
//		else if (publisher.equals("baker+tay")) publisher = "";
		else if (publisher.equals("barron")) publisher = "barron"; // (barron's)
//		else if (publisher.equals("broadview")) publisher = "";
		else if (publisher.equals("cal-prince")) publisher = "university california"; // princeton too
		else if (publisher.equals("camb")) publisher = "cambridge";
//		else if (publisher.equals("carolina a")) publisher = "";
		else if (publisher.equals("castalia")) publisher = "castalia";
		else if (publisher.equals("catedra")) publisher = "catedra";
		else if (publisher.equals("cengage l")) publisher = "cengage";
//		else if (publisher.equals("cheng+tsui")) publisher = "";
		else if (publisher.equals("continuum")) publisher = "continuum";
//		else if (publisher.equals("cosimo")) publisher = "";
		else if (publisher.equals("cq")) publisher = "cq press";
		else if (publisher.equals("cup serv")) publisher = "cornell university"; // press
		else if (publisher.equals("cupr")) publisher = "center urban policy research";
		else if (publisher.equals("dark horse")) publisher = "dark horse";
//		else if (publisher.equals("dover")) publisher = "";
//		else if (publisher.equals("drama play")) publisher = "";
		else if (publisher.equals("dramatic")) publisher = "dramatic"; // publishing company
		else if (publisher.equals("duke u pr")) publisher = "duke university press";
		else if (publisher.equals("dynamic i")) publisher = "dynamic ideas";
		else if (publisher.equals("east-west")) publisher = "east-west"; // center
//		else if (publisher.equals("elsevier")) publisher = "";
//		else if (publisher.equals("emerald")) publisher = "";
//		else if (publisher.equals("european b")) publisher = "";
		else if (publisher.equals("f beedle")) publisher = "franklin beedle"; // and associates
//		else if (publisher.equals("for pub")) publisher = "";
		else if (publisher.equals("graphic pr")) publisher = "graphics"; // press
		else if (publisher.equals("greenwd")) publisher = "greenwood"; // press
		else if (publisher.equals("hachette b")) publisher = "hachette";
//		else if (publisher.equals("hackett")) publisher = "";
		else if (publisher.equals("harp pub")) publisher = "harper"; // publications
//		else if (publisher.equals("hbc trade")) publisher = "";
		else if (publisher.equals("heinemann")) publisher = "heinemann";
//		else if (publisher.equals("hm")) publisher = "";
		else if (publisher.equals("hopkins f")) publisher = "johns hopkins";
//		else if (publisher.equals("ingram pub")) publisher = "";
//		else if (publisher.equals("inner trad")) publisher = "";
//		else if (publisher.equals("interlink")) publisher = "";
//		else if (publisher.equals("intl pub")) publisher = "";
//		else if (publisher.equals("intl pub m")) publisher = "";
//		else if (publisher.equals("ipg")) publisher = "";
		else if (publisher.equals("k-j bathe")) publisher = "klaus bathe"; // klaus-jurgen
//		else if (publisher.equals("leonard h")) publisher = "";
		else if (publisher.equals("lexisnexis")) publisher = "lexisnexis";
		else if (publisher.equals("lipp/w+w")) publisher = "lippincott williams wilkins";
//		else if (publisher.equals("longleaf")) publisher = "";
		else if (publisher.equals("mcg")) publisher = "mcgraw hill";
		else if (publisher.equals("mcgill q")) publisher = "mcgill queens"; // university press
//		else if (publisher.equals("mps")) publisher = "";
		else if (publisher.equals("munshiram")) publisher = "munshiram";
//		else if (publisher.equals("nathan")) publisher = "";
		else if (publisher.equals("northcoast")) publisher = "northcoast"; // publishers
		else if (publisher.equals("norton")) publisher = "norton"; // w. w. norton & company
		else if (publisher.equals("nyu")) publisher = "nyu press";
		else if (publisher.equals("odyssey p")) publisher = "odyssey"; // publications
		else if (publisher.equals("oneworld p")) publisher = "oneworld publications";
		else if (publisher.equals("oxf")) publisher = "oxford university"; // press
//		else if (publisher.equals("pearson")) publisher = "";
//		else if (publisher.equals("pearson c")) publisher = "";
		else if (publisher.equals("peng usa")) publisher = "penguin"; // books
		else if (publisher.equals("penn st")) publisher = "pennsylvania state university"; // press
//		else if (publisher.equals("perseus d")) publisher = "";
//		else if (publisher.equals("ph school")) publisher = "";
//		else if (publisher.equals("prestwick")) publisher = "";
		else if (publisher.equals("prometheus")) publisher = "prometheus"; // books
//		else if (publisher.equals("random")) publisher = "";
		else if (publisher.equals("roberts+co")) publisher = "roberts"; // and company publishers
		else if (publisher.equals("rodopi")) publisher = "rodopi"; // editions
		else if (publisher.equals("s+s")) publisher = "simon schuster";
		else if (publisher.equals("sage")) publisher = "sage"; // publications
		else if (publisher.equals("sar press")) publisher = "sar press";
//		else if (publisher.equals("schoenhof")) publisher = "";
		else if (publisher.equals("sharpe")) publisher = "sharpe";
		else if (publisher.equals("siam")) publisher = "society industrial applied mathematics";
		else if (publisher.equals("silicon pr")) publisher = "silicon press";
		else if (publisher.equals("sinauer")) publisher = "sinauer associates";
//		else if (publisher.equals("sky pub")) publisher = "";
		else if (publisher.equals("springer")) publisher = "springer";
//		else if (publisher.equals("surfside")) publisher = "";
		else if (publisher.equals("taylor")) publisher = "taylor"; // and francis
//		else if (publisher.equals("taylor uk")) publisher = "";
		else if (publisher.equals("transact")) publisher = "transaction"; // publishers
		else if (publisher.equals("triliteral")) publisher = "the mit press";
//		else if (publisher.equals("turning t")) publisher = "";
//		else if (publisher.equals("u new eng")) publisher = "";
		else if (publisher.equals("u of ga")) publisher = "university georgia"; // press
		else if (publisher.equals("u of hi pr")) publisher = "university hawaii"; // press
		else if (publisher.equals("u pr of ms")) publisher = "university press mississippi";
		else if (publisher.equals("ucp")) publisher = "university chicago"; // press
		else if (publisher.equals("univ sci")) publisher = "university science"; // books
		else if (publisher.equals("utp dist")) publisher = "utp";
//		else if (publisher.equals("vhps")) publisher = "";
//		else if (publisher.equals("vic lang")) publisher = "";
//		else if (publisher.equals("waveland")) publisher = "";
		else if (publisher.equals("wbez")) publisher = "wbez";
//		else if (publisher.equals("wellesley")) publisher = "";
		else if (publisher.equals("wiley")) publisher = "wiley";
		else if (publisher.equals("wiley pod")) publisher = "wiley";
		else if (publisher.equals("willan pub")) publisher = "willan"; // publishing
		else if (publisher.equals("world bank")) publisher = "world bank";
		else if (publisher.equals("world sci")) publisher = "world scientific"; // publishing company
		else if (publisher.equals("zizi press")) publisher = "zizi press";
		
		return publisher;
	}

	private String processNecessity(String necessity) {
		necessity = necessity.toLowerCase();
		if (necessity.contains("required"))
			necessity = "required";
		else if (necessity.contains("recommended"))
			necessity = "recommended";
		else {
			// packagecomponent - what does that mean?
			System.err.println("Warning! Unknown necessity: " + necessity);
		}
		
		return necessity.toLowerCase();
	}
	
	private void writeMaterialsToFile(String course, List<Material> materials) {
		String filename = mainPath + DataUtils.MAT_PROCESSED_PATH + course + ".dat";
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

}
