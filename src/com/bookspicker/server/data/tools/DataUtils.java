package com.bookspicker.server.data.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

import com.bookspicker.shared.Book;
import com.bookspicker.shared.School;
import com.bookspicker.shared.Term;

public class DataUtils {

    public static final String DIR_SEP = System.getProperty("file.separator");

    public static final String DATA_PATH =
            System.getProperty("user.dir") +
            DIR_SEP + "data" + DIR_SEP;

    /**
     * Path to folder where raw class data is stored
     */
    public static final String CLASS_RAW_PATH =
            DATA_PATH + "class-raw" + DIR_SEP;

    public static final String CLASS_PROCESSED_PATH =
            DATA_PATH + "class-processed" + DIR_SEP;

    /**
     * Name of folder where raw material data is stored
     */
    public static final String MAT_RAW_PATH = "materials-raw" + DIR_SEP;

    public static final String MAT_PROCESSED_PATH = "materials-processed" + DIR_SEP;

    public static final String MAT_RESOLVED_PATH = "materials-resolved" + DIR_SEP;

    /**
     * This is prepended to all files when reading/writing, so
     * that versions of data can be kept easily. It is advisable
     * to add the data of the fetch/scrape here, so that you know
     * which data is old and which is new.
     */
    public static final String FILE_DATE_PREFIX = "2014-09-01";

    public static final String SEP = "|";
    public static final String PARTS_SEP = ">";

    public static School getSchoolFromUser() {
        School school = null;
        try {
            String answer;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            // Get school
            School[] schools = School.values();
            System.out.print("Choose school: ");
            for (int i = 0; i < schools.length; i++)
                System.out.print((i == 0 ? "" : ", ") + schools[i].toString());
            System.out.println();
            while ((answer = reader.readLine()) != null) {
                try {
                    school = School.valueOf(answer.toUpperCase());
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.print("Choose school: ");
                    for (int i = 0; i < schools.length; i++)
                        System.out.print((i == 0 ? "" : ", ") + schools[i].toString());
                    System.out.println();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return school;
    }

    public static Term getTermFromUser() {
        Term term = null;

        try {
            String answer;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            // Get term
            Term[] terms = Term.values();
            System.out.print("Choose term: ");
            for (int i = 0; i < terms.length; i++)
                System.out.print((i == 0 ? "" : ", ") + terms[i].toString());
            System.out.println();
            while ((answer = reader.readLine()) != null) {
                try {
                    term = Term.valueOf(answer.toUpperCase());
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.print("Choose term: ");
                    for (int i = 0; i < terms.length; i++)
                        System.out.print((i == 0 ? "" : ", ") + terms[i].toString());
                    System.out.println();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return term;
    }

    /**
     * 
     * @param filePrefix the filePrefix to ask the user about
     * @return true if the check was successful, false otherwise
     */
    public static boolean checkPathAndFilePrefix(String filePrefix) {
        try {
            String answer;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Path is: " + filePrefix);
            System.out.println("Is this correct? (yes/no)");
            while ((answer = reader.readLine()) != null) {
                if (answer.equals("yes") || answer.equals("y"))
                    break;
                else if (answer.equals("no") || answer.equals("n")) {
                    System.out.println("You can set the path and the date " +
                            "portion of the file prefix in DataUtils.java");
                    return false;
                } else {
                    System.out.println("Is this correct? (yes/no)");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static void readRawData(String mainPath, String filename, List<Material> materials) throws IOException {
        loadMaterials(mainPath + MAT_RAW_PATH, filename, materials);
    }

    public static void readProcessedData(String mainPath, String filename, List<Material> materials) throws IOException {
        loadMaterials(mainPath + MAT_PROCESSED_PATH, filename, materials);
    }

    private static void loadMaterials(String path, String filename, List<Material> materials) throws IOException {
        BufferedReader input =  new BufferedReader(new FileReader(path + filename));
        String line = null;
        String[] tokens;
        while ((line = input.readLine()) != null) {
            tokens = line.split("\\" + SEP);
            if (tokens.length == 10) {
                materials.add(new Material(tokens[0],
                        tokens[1], tokens[2], tokens[3], tokens[4],
                        tokens[5], tokens[6], tokens[7], tokens[8],
                        tokens[9]));
            } else {
                System.err.println("Warning! could not parse following line: " + line);
            }
        }
        input.close();
    }

    private static Random rand = new Random();
    public static void sleep() {
        //System.out.println("Going to sleep...");
        sleep(300 + rand.nextInt(1000));
        //System.out.println("Woke up");
    }

    private static void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    // I/O methods

    private static String filePrefix = null;

    public static void setFilePrefix() {
        // If filePrefix is not established, get it
        if (filePrefix == null) {
            School school = DataUtils.getSchoolFromUser();
            Term term = DataUtils.getTermFromUser();
            filePrefix = DataUtils.DATA_PATH + school.toString() + DataUtils.DIR_SEP
                    + term.toString() + DataUtils.DIR_SEP + DataUtils.FILE_DATE_PREFIX
                    + DataUtils.DIR_SEP + DataUtils.MAT_RAW_PATH;
        }
        if (!DataUtils.checkPathAndFilePrefix(filePrefix)) {
            System.out.println("Bad path. Please try again (or quit)");
            setFilePrefix();
        }
    }

    public static void writeMapToFile(Map<String, String> classes, String filename) {
        setFilePrefix();
        try {
            // Create a stream for writing.
            FileWriter fileWriter = new FileWriter(filePrefix + filename);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            List<String> keys = new ArrayList<String>(classes.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                writer.write(key);
                writer.write(DataUtils.SEP);
                writer.write(classes.get(key));
                writer.newLine();
            }
            writer.flush();
            writer.close();
            System.out.println("Wrote " + classes.size() + " records to " + filename);
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }

    public static Map<String, String> readMapFromFile(String filename) {
        setFilePrefix();
        Map<String, String> map = new HashMap<String, String>();
        try {
            Scanner scanner = new Scanner(new File(filePrefix + filename));
            while (scanner.hasNext()) {
                String[] keyVal = scanner.nextLine().split("\\" + DataUtils.SEP);
                map.put(keyVal[0], keyVal[1]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return map;
    }


    @SuppressWarnings("serial")
    public static class ClassBean implements Serializable {
        public String classCode;
        public String title;
        public Term term;
        public String jointSubjects;
        public Date lastActivityDate;
        public Date warehouseLoadDate;

        public boolean isComplete() {
            return (classCode != null &&
                    title != null &&
                    term != null &&
                    jointSubjects != null &&
                    lastActivityDate != null &&
                    warehouseLoadDate != null);
        }

        @Override
        public String toString() {
            return "\tCode: " + classCode +
                    "\n\tTitle: " + title +
                    "\n\tTerm: " + term +
                    "\n\tJoint Subjects: " + jointSubjects +
                    "\n\tlastActivityDate: " + lastActivityDate +
                    "\n\twarehouseLoadDate: " + warehouseLoadDate;
        }
    }

    public static class Material {
        public String section;
        public String title;
        public String author;
        public String edition;
        public String publisher;
        public String isbn;
        public String necessity;
        public String newPrice;
        public String usedPrice;
        public String url;

        public Material(String section, String title, String author, String edition,
                String publisher, String isbn, String necessity, String newPrice,
                String usedPrice, String url) {
            this.section = section;
            this.title = title;
            this.author = author;
            this.edition = edition;
            this.publisher = publisher;
            this.isbn = isbn;
            this.necessity = necessity;
            this.newPrice = newPrice;
            this.usedPrice = usedPrice;
            this.url = url;
        }

        @Override
        public String toString() {
            return section + DataUtils.SEP + title + DataUtils.SEP + author +
                    DataUtils.SEP + edition + DataUtils.SEP +
                    publisher + DataUtils.SEP + isbn + DataUtils.SEP + necessity +
                    DataUtils.SEP + newPrice + DataUtils.SEP + usedPrice + DataUtils.SEP + url;
        }

        public String getInfoString() {
            return isbn + DataUtils.SEP + title + DataUtils.SEP + author + DataUtils.SEP + edition + DataUtils.SEP + publisher;
        }
    }

    public static class BookMat implements Serializable {
        private static final long serialVersionUID = 1L;

        public String section;
        public Book book;
        public String necessity;
        public String newPrice;
        public String usedPrice;
        public String url;
        public String isbn;

        public BookMat(Book book, Material material) {
            this.section = material.section;
            this.book = book;
            this.necessity = material.necessity;
            this.newPrice = material.newPrice;
            this.usedPrice = material.usedPrice;
            this.url = material.url;
            this.isbn = material.isbn;
        }

        public boolean similar(BookMat other) {
            return book.equals(other.book) && necessity.equals(other.necessity);
        }
    }

}
