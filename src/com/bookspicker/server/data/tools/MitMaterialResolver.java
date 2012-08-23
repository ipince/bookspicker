package com.bookspicker.server.data.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bookspicker.server.data.PatternUtil;
import com.bookspicker.server.data.tools.DataUtils.BookMat;
import com.bookspicker.server.data.tools.DataUtils.Material;
import com.bookspicker.server.queries.AmazonQuery;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.School;
import com.bookspicker.shared.Term;
import com.google.gwt.dev.util.collect.HashMap;

/**
 * This is the THIRD step in a data scrape. Queries Amazon
 * to get book information based on the processed data.
 * 
 * @author Rodrigo Ipince
 *
 */
public class MitMaterialResolver {

	private static String mainPath;
	
	public static void main(String[] args) {
		
		// Choose school and term!
		School school = DataUtils.getSchoolFromUser();
		Term term = DataUtils.getTermFromUser();
		
		mainPath = DataUtils.DATA_PATH + school.toString() + DataUtils.DIR_SEP +
			term.toString() + DataUtils.DIR_SEP + 
			DataUtils.FILE_DATE_PREFIX + 
			DataUtils.DIR_SEP;
		
		if (!DataUtils.checkPathAndFilePrefix(mainPath))
			System.exit(1);
		new MitMaterialResolver().run();
	}

	private void run() {

		// Holds Materials that weren't resolved
		List<String> notFounds = new ArrayList<String>();
		List<String> foundMultiple = new ArrayList<String>();
		int uniqueNotFoundCount = 0;
		
		// Holds bookInfoFromMaterial->Book mappings serving as 'cache'
		Map<String, ArrayList<Book>> bookMappings = new HashMap<String, ArrayList<Book>>();
		Map<String, ArrayList<String>> noteMappings = new HashMap<String, ArrayList<String>>();
		
		List<Material> materials = new ArrayList<Material>();
		List<String> sections = new ArrayList<String>();
		List<Book> books = new ArrayList<Book>();
		List<BookMat> bookmats = new ArrayList<BookMat>();
		List<String> notes = new ArrayList<String>();
		
		List<Book> bookResults = new ArrayList<Book>();
		ArrayList<Book> newBooks = new ArrayList<Book>();
		ArrayList<String> newNotes = new ArrayList<String>();
		
		AmazonQuery amazon = new AmazonQuery();
		
		int numBooks;
		String note;
		
		// If true, we only consider the first result from Amazon
		boolean pickFirstResult = false;
		
		File dir = new File(mainPath + DataUtils.MAT_PROCESSED_PATH);
		// Filter files by name
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		    	// TODO remove this!
		    	return name.endsWith("EECS.dat");
		    }
		};
		String[] children = dir.list(filter);
		if (children == null) {
			System.err.println("No files found in directory");
			return;
		}

		String filename, course;
		for (int i = 0; i < children.length; i++) {
			filename = children[i];
			course = filename.substring(0, filename.indexOf(".dat"));

			System.out.println("Resolving course " + course);

			// empty lists
			materials.clear();
			sections.clear();
			books.clear();
			notes.clear();

			// load processed data
			try {
				DataUtils.readProcessedData(mainPath, filename, materials);
			} catch (IOException e) {
				// skip
				System.err.println("Error processing materials for course " + course);
				continue;
			}

			// try to resolve each material
			for (Material mat : materials) {
				
				// if we've queried it before, load it from 'cache' and continue
				if (bookMappings.containsKey(mat.getInfoString())) {
					
					if (bookMappings.get(mat.getInfoString()).size() == 0) {
						// means we didn't find it before, so we won't find it now
						notFounds.add(mat.toString());
					} else {
						for (Book book : bookMappings.get(mat.getInfoString())) {
							// copy over what we found before
							books.add(book);
							bookmats.add(new BookMat(book, mat));
						}
					}
					
					notes.addAll(noteMappings.get(mat.getInfoString()));
					for (int j = 0; j < bookMappings.get(mat.getInfoString()).size(); j++)
						sections.add(mat.section);
					
					continue; // skip queries
				}
				
				// reset lists
				newBooks = new ArrayList<Book>();
				newNotes = new ArrayList<String>();
				
				// clear new booklist and send query
				bookResults.clear();
				DataUtils.sleep();
				
				if (PatternUtil.isIsbn(mat.isbn)) {
					List<String> isbnList = new ArrayList<String>();
					isbnList.add(mat.isbn);
					bookResults = amazon.getBooksInfoByIsbn(isbnList);
				} else {
					bookResults = amazon.getBookInfoByData(mat.title, mat.author, "");
				}
				note = mat.toString();

				// see if we got any worthwhile data
				numBooks = 0;
				for (Book book : bookResults) {
//					if (book.getIsbn() == null)
//						continue;
					if (book == null)
						continue;
					
					newBooks.add(book);
					bookmats.add(new BookMat(book, mat));
					numBooks++;
					
					if (pickFirstResult)
						break;
				}
				
				// didn't get anything again, add to relevant lists
				if (numBooks == 0) {
					
//					System.err.println("Warning! No books found for: " + mat.toString());
					notFounds.add(mat.toString());
					uniqueNotFoundCount++;
				} else if (numBooks == 1) {
					sections.add(mat.section);
					newNotes.add(note + " -- 1 BOOK MATCHED");
				} else {
					// TODO: add an error or something
					foundMultiple.add("Found multiples:");
					for (int j = 0; j < numBooks; j++) {
						sections.add(mat.section);
						newNotes.add(note + " -- Match " + (j+1) + " of " + numBooks);
						foundMultiple.add(mat.toString());
					}
				}
				
				// save results on 'cache'
				bookMappings.put(mat.getInfoString(), newBooks);
				noteMappings.put(mat.getInfoString(), newNotes);
				
				// put new data on per-course data lists
				books.addAll(newBooks);
				notes.addAll(newNotes);
			}

			// write data to file
			writeBooksToFile(course, sections, books, notes);

		}
		
		writeStatsToFile("stats.dat", notFounds, uniqueNotFoundCount, foundMultiple);
		writeBookMatsToFile("books.dat", bookmats);
	}

	private int totalCount = 0;
	private void writeBooksToFile(String course, List<String> sections, 
			List<Book> books, List<String> notes) {
		
		String filename = mainPath + DataUtils.MAT_RESOLVED_PATH + course + ".dat";
		System.out.println("Writing data to " + filename);
		try {
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			int count = 0;
			for (int i = 0; i < sections.size(); i++) {
				out.write(sections.get(i));
				out.write(DataUtils.SEP);
				out.write(books.get(i).toRowString(DataUtils.SEP));
				if (notes != null) {
					out.write(DataUtils.SEP);
					out.write(notes.get(i));
				}
				out.newLine();
				count++;
			}
			out.close();
			System.out.println("Wrote " + count + " records to " + filename);
			totalCount += count;
		} catch (IOException e) {
			System.err.println("Error writing file: " + e.getMessage());
		}
	}
	
	private void writeStatsToFile(String filename, List<String> notFounds,
			int uniqueNotFoundCount, List<String> foundMultiple) {
		filename = mainPath + DataUtils.MAT_RESOLVED_PATH + filename;
		System.out.println("Writing data to " + filename);
		try {
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			
			
			out.write("\nWrote " + totalCount + " records in total");
			out.newLine();
			
			out.write("\nNot found (" + uniqueNotFoundCount + " unique, " + notFounds.size() + " total):\n");
			out.newLine();
			for (String nf : notFounds) {
				out.write(nf);
				out.newLine();
			}
			
			out.write("\nFound multiple:\n");
			for (String fm : foundMultiple) {
				out.write(fm);
				out.newLine();
			}
			
			out.close();
			System.out.println("Done");
		} catch (IOException e) {
			System.err.println("Error writing file: " + e.getMessage());
		}
	}
	
	private void writeBookMatsToFile(String filename, List<BookMat> bookmats) {
		try {
			//  Create a stream for writing.
			FileOutputStream fos = new FileOutputStream(mainPath + DataUtils.MAT_RESOLVED_PATH + filename);

			//  Next, create an object that can write to that file.
			ObjectOutputStream out = new ObjectOutputStream(fos);

			//  Save each object
			int count = 0;
			for (BookMat bookmat : bookmats) {
				out.writeObject(bookmat);
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
