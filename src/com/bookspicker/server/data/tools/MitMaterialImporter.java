package com.bookspicker.server.data.tools;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.bookspicker.server.data.BookManager;
import com.bookspicker.server.data.ClassManager;
import com.bookspicker.server.data.tools.DataUtils.BookMat;
import com.bookspicker.server.queries.AmazonQuery;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.ClassBook.Necessity;
import com.bookspicker.shared.ClassBook.Source;
import com.bookspicker.shared.School;
import com.bookspicker.shared.SchoolClass;
import com.bookspicker.shared.Term;
import com.google.gwt.dev.util.collect.HashMap;

public class MitMaterialImporter {

    private static final boolean MANUAL = false;

    private static String mainPath;
    private static School school;
    private static Term term;

    public static void main(String[] args) {
        // Choose school and term!
        school = DataUtils.getSchoolFromUser();
        term = DataUtils.getTermFromUser();

        mainPath = DataUtils.DATA_PATH + school.toString() + DataUtils.DIR_SEP +
                term.toString() + DataUtils.DIR_SEP +
                DataUtils.FILE_DATE_PREFIX +
                DataUtils.DIR_SEP;

        if (MANUAL)
            new MitMaterialImporter().importManualData();
        else
            new MitMaterialImporter().importAutoData();
    }

    private void importManualData() {
        try {
            BufferedReader input =  new BufferedReader(new FileReader(mainPath + DataUtils.MAT_RESOLVED_PATH + "manual.dat"));
            String line = null;
            String[] tokens;
            String[] parts;

            AmazonQuery amazon = new AmazonQuery();

            SchoolClass clas;
            List<Book> books;
            Book book;
            Book bookdb;
            Necessity necessity;

            ClassManager cm = ClassManager.getManager();
            BookManager bm = BookManager.getManager();

            List<String> isbnList = new ArrayList<String>();

            while ((line = input.readLine()) != null) {
                tokens = line.split("\\" + DataUtils.SEP);
                if (tokens.length == 5 || tokens.length == 6) {

                    // get class from DB
                    parts = tokens[0].split(DataUtils.PARTS_SEP);
                    clas = cm.getClassByCode(school, Term.CURRENT_TERM, parts[0]);
                    if (clas == null) {
                        // class not in DB
                        System.err.println("Warning: " + parts[0] + " not in DB");
                        continue;
                    }

                    isbnList.clear();
                    isbnList.add(tokens[1]);
                    books = amazon.getBooksInfoByIsbn(isbnList);
                    if (books.size() > 0) {
                        book = books.get(0);

                        // make sure each book is in DB
                        bookdb = bm.getBookByIsbn(book.getIsbn());
                        if (bookdb == null) {
                            System.err.println("Warning: book with isbn " + book.getIsbn() + " not in DB - saving it");
                            bookdb = bm.saveBook(book);
                        }

                        // translate necessity
                        if (tokens[2].equals("required"))
                            necessity = Necessity.REQUIRED;
                        else if (tokens[2].equals("recommended"))
                            necessity = Necessity.RECOMMENDED;
                        else
                            necessity = Necessity.UNKNOWN;

                        if (tokens.length == 6)
                            clas.addBook(bookdb, necessity, Source.MANUAL, tokens[5], null, true);
                        else
                            clas.addBook(bookdb, necessity, Source.MANUAL);
                        cm.updateClass(clas);
                    }

                } else {
                    System.err.println("Warning! could not parse following line: " + line);
                }
            }
            input.close();
        } catch (IOException e) {
            System.err.println("Failed to parse manual data");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void importAutoData() {

        // read bookmats into list
        List<BookMat> bookmats = new ArrayList<BookMat>();
        readBookMats("books.dat", bookmats);

        // class -> set of sections (e.g., "1>.00" -> "1>.00>1")
        Map<String, HashSet<String>> sections = new HashMap<String, HashSet<String>>();

        // section -> list of bookmats (e.g., "1>.00>1" -> BookMat)
        Map<String, ArrayList<BookMat>> bookLists = new HashMap<String, ArrayList<BookMat>>();

        // load data into maps
        String[] parts;
        HashSet<String> tmpSections;
        ArrayList<BookMat> tmpBookmats;
        for (BookMat bookmat : bookmats) {
            parts = bookmat.section.split(DataUtils.PARTS_SEP);
            if (parts.length != 3) {
                System.err.println("Error! Course/class/section parts of unexpected size: " + bookmat.section);
                continue;
            }

            // add section to class
            tmpSections = sections.get(parts[0] + DataUtils.PARTS_SEP + parts[1]);
            if (tmpSections == null) {
                tmpSections = new HashSet<String>();
                sections.put(parts[0] + DataUtils.PARTS_SEP + parts[1], tmpSections);
            }
            tmpSections.add(bookmat.section);

            // add bookmat to section's list
            tmpBookmats = bookLists.get(bookmat.section);
            if (tmpBookmats == null) {
                tmpBookmats = new ArrayList<BookMat>();
                bookLists.put(bookmat.section, tmpBookmats);
            }
            tmpBookmats.add(bookmat);
        }

        // get DB object managers
        ClassManager cm = ClassManager.getManager();
        BookManager bm = BookManager.getManager();

        // Trigger hibernate
        cm.getClassByCode(school, Term.CURRENT_TERM, "6.002");

        // declare loop vars
        //		List<String> classSections;
        SchoolClass clas;
        Book book;
        Necessity necessity;
        //		StringBuilder noteBuilder;

        // Go in sorted order through sections
        List<String> sectionList = new ArrayList<String>(bookLists.keySet());
        Collections.sort(sectionList);

        List<String> errors = new ArrayList<String>();
        for (String fullSection : sectionList) {
            parts = fullSection.split(DataUtils.PARTS_SEP);
            if (parts.length != 3) {
                System.err.println("Section " + fullSection + " does have 3 parts! Skipping");
                continue;
            }

            clas = cm.getClassByParts(school, term, parts[0], parts[1], parts[2]);
            if (clas == null) {
                // class not in DB => skip
                System.err.println("Warning: " + fullSection + " not in DB; Adding it!");
                errors.add("Warning: " + fullSection + " not in DB; Adding it!");
                continue;
            }

            // Go through mappings and save to db
            for (BookMat bookmat : bookLists.get(fullSection)) {

                // Check that book is in DB
                book = bm.getBookByIsbn(bookmat.book.getIsbn());
                if (book == null) {
                    System.err.println("Warning: book with isbn " + bookmat.book.getIsbn() + " not in DB - saving it");
                    errors.add("Warning: book with isbn " + bookmat.book.getIsbn() + " not in DB - saving it");
                    book = bm.saveBook(bookmat.book);
                }

                // Translate necessity
                if (bookmat.necessity.equals("required"))
                    necessity = Necessity.REQUIRED;
                else if (bookmat.necessity.equals("recommended"))
                    necessity = Necessity.RECOMMENDED;
                else
                    necessity = Necessity.UNKNOWN;

                // Save
                clas.addBook(book, necessity, Source.THE_COOP);
                cm.updateClass(clas);
            }
        }

        //		// Go in sorted order
        //		List<String> classCodes = new ArrayList<String>(sections.keySet());
        //		Collections.sort(classCodes);
        //
        //		for (String classCode : classCodes) {
        //
        //			// Get class from DB (or save new class)
        //			clas = cm.getClassByCode(classCode, Term.CURRENT_TERM);
        //			if (clas == null) {
        //				// class not in DB => skip
        //				System.err.println("Warning: " + classCode + " not in DB; Adding it!");
        //				errors.add("Warning: " + classCode + " not in DB; Adding it!");
        //				continue;
        //			}
        //
        //			System.out.println(clas.getCode() + " - " + clas.getTerm().toString());
        //
        //			// grab class's sections
        //			classSections = new ArrayList<String>(sections.get(classCode));
        //			Collections.sort(classSections);
        //
        //			if (classSections.size() == 0) {
        //				System.err.println("Warning: " + classCode + " did not have any sections");
        //				errors.add("Warning: " + classCode + " did not have any sections");
        //			} else {
        //
        //				// put all unique books/necessities from all sections in a list
        //				List<BookMat> uniqueBookMats = new ArrayList<BookMat>();
        //				boolean found;
        //				for (String section : classSections) {
        //					for (BookMat bookmat : bookLists.get(section)) {
        //						found = false;
        //						for (BookMat uniquebm : uniqueBookMats) {
        //							if (uniquebm.similar(bookmat)) {
        //								found = true;
        //								break;
        //							}
        //						}
        //						if (!found) {
        //							uniqueBookMats.add(bookmat);
        //						}
        //					}
        //				}
        //
        //				for (BookMat bookmat : uniqueBookMats) {
        //
        //					// make sure each book is in DB
        //					book = bm.getBookByIsbn(bookmat.book.getIsbn());
        //					if (book == null) {
        //						System.err.println("Warning: book with isbn " + bookmat.book.getIsbn() + " not in DB - saving it");
        //						errors.add("Warning: book with isbn " + bookmat.book.getIsbn() + " not in DB - saving it");
        //						book = bm.saveBook(bookmat.book);
        //					}
        //
        //					// see what sections it belongs to
        //					boolean[] bookInSection = new boolean[classSections.size()];
        //					for (int i = 0; i < classSections.size(); i++) {
        //						for (BookMat other : bookLists.get(classSections.get(i))) {
        //							if (bookmat.similar(other)) {
        //								bookInSection[i] = true;
        //								break;
        //							}
        //						}
        //					}
        //
        //					// does it belong to ALL of them?
        //					boolean bookInAllSections = true;
        //					for (int i = 0; i < bookInSection.length; i++) {
        //						if (!bookInSection[i]) {
        //							bookInAllSections = false;
        //							break;
        //						}
        //					}
        //
        //					// translate necessity
        //					if (bookmat.necessity.equals("required"))
        //						necessity = Necessity.REQUIRED;
        //					else if (bookmat.necessity.equals("recommended"))
        //						necessity = Necessity.RECOMMENDED;
        //					else
        //						necessity = Necessity.UNKNOWN;
        //
        //
        //					noteBuilder = new StringBuilder();
        //					if (!bookInAllSections) {
        //						// add a note mentioning each section it belongs to
        //						noteBuilder = new StringBuilder();
        //						noteBuilder.append("Only for section(s) ");
        //						boolean empty = true;
        //						for (int i = 0; i < bookInSection.length; i++) {
        //							if (bookInSection[i]) {
        //								noteBuilder.append((empty ? "" : ", ") + classSections.get(i));
        //								empty = false;
        //							}
        //						}
        //						noteBuilder.append(".");
        //					}
        //					clas.addBook(book, necessity, Source.THE_COOP, noteBuilder.toString());
        //					cm.updateClass(clas);
        //				}
        //			}
        //		}

        // Print errors
        for (String error : errors) {
            System.err.println(error);
        }
    }

    private void readBookMats(String filename, List<BookMat> bookmats) {
        try {
            //  Create a stream for reading.
            FileInputStream fis = new FileInputStream(mainPath + DataUtils.MAT_RESOLVED_PATH + filename);

            //  Next, create an object that can read from that file.
            ObjectInputStream inStream = new ObjectInputStream(fis);

            // Retrieve the Serializable object.
            BookMat bookmat;
            while ((bookmat = (BookMat) inStream.readObject()) != null) {
                bookmats.add(bookmat);
            }
        } catch (EOFException eof) {
            // file ended - ok
        } catch (IOException ioe) {
            System.err.println("Error reading books");
            ioe.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Error reading books2: " + cnfe.getMessage());
        }
    }

}
