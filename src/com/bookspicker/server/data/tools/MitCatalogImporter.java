package com.bookspicker.server.data.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import com.bookspicker.server.data.BookManager;
import com.bookspicker.server.data.ClassManager;
import com.bookspicker.server.data.HibernateUtil;
import com.bookspicker.server.data.PatternUtil;
import com.bookspicker.server.data.tools.DataUtils.BookMat;
import com.bookspicker.server.data.tools.DataUtils.Material;
import com.bookspicker.server.queries.AmazonQuery;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.ClassBook.Necessity;
import com.bookspicker.shared.ClassBook.Source;
import com.bookspicker.shared.School;
import com.bookspicker.shared.SchoolClass;
import com.bookspicker.shared.Term;

public class MitCatalogImporter {

    private final boolean WRITE_TO_DB = true;

    private String dataPath;

    public static void main(String[] args) {
        MitCatalogImporter importer = new MitCatalogImporter();
        importer.importClassData();
        importer.importBookData(MitCatalogScraper.BOOK_LIST_FILE);
    }

    private void importClassData() {
        // Read names and urls from files.
        Map<String, String> classNames = DataUtils
                .readMapFromFile(MitCatalogScraper.CLASS_NAME_FILE);
        Map<String, String> classUrls = DataUtils.readMapFromFile(MitCatalogScraper.CLASS_URL_FILE);
        //        System.out.println(classes);

        ClassManager cm = ClassManager.getManager();

        // Trigger hibernate
        cm.getClassByCode(School.MIT, Term.CURRENT_TERM, "6.002");

        // Sort classes by code before adding (just because)
        List<String> classCodes = new ArrayList<String>(classNames.keySet());
        Collections.sort(classCodes);

        for (String classCode : classCodes) {
            // If is does not exist in DB, then add it.
            SchoolClass clas = cm.getClassByCode(School.MIT, Term.CURRENT_TERM, classCode);
            if (clas == null) {
                String course = classCode.substring(0, classCode.indexOf("."));
                String classPart = classCode.substring(classCode.indexOf("."));
                // If not in DB, add new class
                clas = new SchoolClass();
                clas.setSchool(School.MIT);
                clas.setTerm(Term.CURRENT_TERM);
                clas.setCourse(course);
                clas.setClas(classPart);
                clas.setSection("1");
                clas.setCode(School.MIT.getClassCode(course, classPart, "1"));
                clas.setTitle(classNames.get(classCode));
                clas.setClassInfoUrl(classUrls.get(classCode));

                System.out.println("Saving: " + clas);
                if (WRITE_TO_DB) {
                    clas = cm.save(clas);
                }
            } else {
                System.err.println("Class already existed " + classCode + ": " + clas);
            }
        }
    }

    private void importBookData(String filename) {
        setFilePrefix();
        List<Material> materials = new ArrayList<Material>();
        try {
            DataUtils.readRawData(filePrefix, filename, materials);
        } catch (IOException e) {
            e.printStackTrace();
            // Failed
            return;
        }

        // First make sure all books are available in DB and get Book-Material pairs.
        //        List<BookMat> bookmats = getBookMats(materials);

        ClassManager cm = ClassManager.getManager();
        BookManager bm = BookManager.getManager();


        // Iterate through Book/Material and add mappings to DB.
        for (Material material : materials) {
            Session session = HibernateUtil.getSessionFactory().openSession();
            // Make sure class exists in DB.
            SchoolClass clas = cm.getClassByCode(School.MIT, Term.CURRENT_TERM, material.section,
                    session);
            if (clas == null) {
                // Class is supposed to be in DB by now (class data should be imported first).
                System.err.println("Class with code " + material.section + " not in DB. Skipping");
                session.close();
                continue;
            }

            if ("null".equals(material.isbn)) { // means class requires no books
                Book dummyBook = bm.getBookByIsbn("1558607358", session);
                if (dummyBook == null) {
                    session.close();
                    System.err.println("Couldn't find dummy book to use for non-book classes");
                    System.exit(1);
                }
                clas.addBook(dummyBook, Necessity.UNKNOWN, Source.MIT, "", material.url, false);
                if (WRITE_TO_DB) {
                    cm.updateClass(clas, session);
                }
                continue;
            }

            // Make sure book exists in DB.
            Book book = bm.getBookByIsbn(material.isbn, session);
            if (book == null) {
                System.err.println("Book with isbn " + material.isbn + " not in DB (class " + material.section + "). Skipping");
                session.close();
                continue;
            }

            // Save mapping!
            clas.addBook(book, Necessity.valueOf(material.necessity), Source.MIT, "", material.url, true);
            if (WRITE_TO_DB) {
                cm.updateClass(clas, session);
            }
        }
    }

    private List<BookMat> getBookMats(List<Material> materials) {
        List<BookMat> bookmats = new ArrayList<BookMat>();
        BookManager bm = BookManager.getManager();
        Book dummyBook = bm.getBookByIsbn("1558607358");
        if (dummyBook == null) {
            System.err.println("Couldn't find dummy book to use for non-book classes");
            System.exit(1);
        }
        AmazonQuery amazon = new AmazonQuery();
        for (Material material : materials) {
            System.out.println(material);

            if ("null".equals(material.isbn)) {
                // Skip. This class has no books.
                bookmats.add(new BookMat(dummyBook, material));
                continue;
            }

            // Fetch the book in DB
            Book dbBook = bm.getBookByIsbn(material.isbn);
            if (dbBook != null) {
                // Found on DB -> save bookmat
                bookmats.add(new BookMat(dbBook, material));
            } else {
                // Not on DB -> search Amazon
                DataUtils.sleep();
                List<Book> bookResults = new ArrayList<Book>();

                if (PatternUtil.isIsbn(material.isbn)) {
                    List<String> isbnList = new ArrayList<String>();
                    isbnList.add(material.isbn);
                    bookResults = amazon.getBooksInfoByIsbn(isbnList);
                } else {
                    System.err.println("Invalid ISBN: " + material.isbn + ". Skipping");
                    continue;
                }

                if (bookResults.isEmpty()) {
                    System.err.println("No results found for " + material.isbn + ". Skipping");
                    continue;
                } else if (bookResults.size() > 1) {
                    System.err.println("Multiple results found for " + material.isbn + ". Skipping");
                    continue;
                } else {
                    // Only 1 result, as expected. Save it to DB and add bookmat
                    Book book = bookResults.get(0);
                    if (book == null) {
                        // No matching book found.
                        System.err.println("No book found matching: " + material.isbn);
                        continue;
                    }
                    if (WRITE_TO_DB) {
                        book = bm.saveBook(book);
                    }
                    bookmats.add(new BookMat(book, material));
                }
            }
        }
        return bookmats;
    }

    private static String filePrefix = null;

    public static void setFilePrefix() {
        // If filePrefix is not established, get it
        if (filePrefix == null) {
            School school = DataUtils.getSchoolFromUser();
            Term term = DataUtils.getTermFromUser();
            filePrefix = DataUtils.DATA_PATH + school.toString() + DataUtils.DIR_SEP
                    + term.toString() + DataUtils.DIR_SEP + DataUtils.FILE_DATE_PREFIX
                    + DataUtils.DIR_SEP;
        }
        if (!DataUtils.checkPathAndFilePrefix(filePrefix)) {
            System.out.println("Bad path. Please try again (or quit)");
            setFilePrefix();
        }
    }
}
