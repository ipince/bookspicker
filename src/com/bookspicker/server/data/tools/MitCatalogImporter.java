package com.bookspicker.server.data.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.bookspicker.server.data.BookManager;
import com.bookspicker.server.data.ClassManager;
import com.bookspicker.server.data.PatternUtil;
import com.bookspicker.server.data.tools.DataUtils.BookMat;
import com.bookspicker.server.data.tools.DataUtils.Material;
import com.bookspicker.server.queries.AmazonQuery;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.School;
import com.bookspicker.shared.SchoolClass;
import com.bookspicker.shared.Term;
import com.bookspicker.shared.ClassBook.Necessity;
import com.bookspicker.shared.ClassBook.Source;

public class MitCatalogImporter {
    
    private final boolean WRITE_TO_DB = false;
    
    private String dataPath;
    
    public static void main(String[] args) {
        MitCatalogImporter importer = new MitCatalogImporter();
//        importer.importClassData("mit-classes.dat");
        
        importer.importBookData("mit-book-lists.dat");
    }

    private void importClassData(String filename) {
        // Read codes and titles from file.
        Map<String, String> classes = readClasses(filename);
//        System.out.println(classes);
        
        ClassManager cm = ClassManager.getManager();
        
        // Trigger hibernate
        cm.getClassByCode(School.MIT, Term.CURRENT_TERM, "6.002");
        
        for (String classCode : classes.keySet()) {
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
                
                System.out.println("Saving: " + clas);
                if (WRITE_TO_DB) {
                    clas = cm.save(clas);
                }
            }
        }
    }
    
    private void importBookData(String filename) {
        setFilePrefix();
        List<Material> materials = new ArrayList<Material>();
        try {
            DataUtils.readRawData(dataPath, filename, materials);
        } catch (IOException e) {
            e.printStackTrace();
            // Failed
            return;
        }
        
        // First make sure all books are available in DB and get Book-Material pairs.
        List<BookMat> bookmats = getBookMats(materials);
        
        // Iterate through Book/Material and add mappings to DB.
        ClassManager cm = ClassManager.getManager();
        BookManager bm = BookManager.getManager();
        for (BookMat bookmat : bookmats) {
            // Make sure class exists in DB.
            SchoolClass clas = cm.getClassByCode(School.MIT, Term.CURRENT_TERM, bookmat.section);
            if (clas == null) {
                // Class is supposed to be in DB by now (class data should be imported first).
                System.err.println("Class with code " + bookmat.section + " not in DB. Skipping");
                continue;
            }
            
            // Make sure book exists in DB.
            Book book = bm.getBookByIsbn(bookmat.book.getIsbn());
            if (book == null) {
                System.err.println("Book with isbn " + bookmat.book.getIsbn() + " not in DB (class " + bookmat.section + "). Skipping");
                continue;
            }
                
            // Save mapping!
            clas.addBook(book, Necessity.valueOf(bookmat.necessity), Source.MIT);
            if (WRITE_TO_DB) {
                cm.updateClass(clas);
            }
        }
    }

    private List<BookMat> getBookMats(List<Material> materials) {
        List<BookMat> bookmats = new ArrayList<BookMat>();
        BookManager bm = BookManager.getManager();
        AmazonQuery amazon = new AmazonQuery();
        for (Material material : materials) {
            System.out.println(material);
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
                    if (WRITE_TO_DB) {
                        book = bm.saveBook(book);
                    }
                    bookmats.add(new BookMat(book, material));
                }
            }
        }
        return bookmats;
    }

    private Map<String, String> readClasses(String filename) {
        setFilePrefix();
        Map<String, String> classes = new HashMap<String, String>();
        try {
            Scanner scanner = new Scanner(new File(dataPath + DataUtils.MAT_RAW_PATH + filename));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String[] parts = line.split("\\" + DataUtils.SEP);
                if (parts.length == 2) {
                    String code = parts[0];
                    String label = parts[1].substring(parts[1].indexOf(" ")).trim();
                    classes.put(code, label);
                } else {
                    System.err.println("Bad line: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return classes;
    }
    
    private void setFilePrefix() {
        // If filePrefix is not established, get it
        if (dataPath == null) {
            School school = DataUtils.getSchoolFromUser();
            Term term = DataUtils.getTermFromUser();
            dataPath = DataUtils.DATA_PATH + school.toString() + DataUtils.DIR_SEP +
                    term.toString() + DataUtils.DIR_SEP + DataUtils.FILE_DATE_PREFIX + 
                    DataUtils.DIR_SEP;
        }
        if (!DataUtils.checkPathAndFilePrefix(dataPath)) {
            System.out.println("Bad path. Please try again (or quit)");
            setFilePrefix();
        }
    }

}
