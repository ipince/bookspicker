package com.bookspicker.server.data.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlcleaner.ContentToken;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.bookspicker.server.data.tools.DataUtils.Material;
import com.bookspicker.shared.ClassBook.Necessity;
import com.bookspicker.shared.School;
import com.bookspicker.shared.Term;
import com.zenkey.net.prowser.Prowser;
import com.zenkey.net.prowser.Response;
import com.zenkey.net.prowser.Tab;

public class MitCatalogScraper {

    private final Prowser prowser = new Prowser();
    private final Tab mainTab = prowser.createTab();
    private final HtmlCleaner cleaner = new HtmlCleaner();

    private String filePrefix;

    private final String CATALOG_BASE = "http://student.mit.edu/catalog/";

    protected final static Set<String> CATALOG_COURSE_URLS = new HashSet<String>();

    /**
     * @param args
     */
    public static void main(String[] args) {
        MitCatalogScraper scraper = new MitCatalogScraper();

        scraper.getClassNames();
        List<String> links = scraper.getBookLinks(false);
        //        scraper.fetchBookInfo(links);
    }

    private Map<String, String> getClassNames() {
        // We get the class names by performing an advanced search on the catalog
        // for everything that is offered in the current semester:
        String searchUrl = "http://student.mit.edu/catalog/search.cgi?search=&style=verbatim&when=C&days_offered=*&start_time=*&duration=*&total_units=*";

        String results = mainTab.go(searchUrl).getPageSource();
        Map<String, String> classes = new HashMap<String, String>();
        try {
            TagNode node = cleaner.clean(results);
            Object[] anchors = node.evaluateXPath("/body/div[2]/blockquote/dl/dt/a");
            for (Object obj : anchors) {
                if (obj instanceof TagNode) {
                    TagNode anchor = (TagNode) obj;
                    // Label is of form
                    // "<code>[, <code>] <Title> [(New)|(<Joint-Class-Code>)]"
                    String label = ((ContentToken)anchor.getChildren().get(0)).getContent();
                    String name = extractNameFromLabel(label);
                    String href = anchor.getAttributeByName("href");
                    String code = href.substring(href.lastIndexOf("#")+1);
                    String courseUrl = href.substring(0, href.lastIndexOf("#"));
                    String classUrl = CATALOG_BASE + href;
                    CATALOG_COURSE_URLS.add(CATALOG_BASE + courseUrl);
                    classes.put(code, name + DataUtils.SEP + classUrl);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPatherException e) {
            e.printStackTrace();
        }
        System.out.println(classes.size());
        System.out.println(CATALOG_COURSE_URLS);
        writeMapToFile(classes, "mit-classes.dat");
        return classes;
    }

    private String extractNameFromLabel(String label) {
        String[] parts = label.split(" ");
        String name = "";
        for (int i = 0; i < parts.length; i++) {
            // Skip the first chunk
            if (i == 0) {
                continue;
            }
            // Skip second part iff its first three chars == first three chars
            // of first part
            if (i == 1 && parts[i].length() >= 3 && parts[0].length() >= 3
                    && parts[i].startsWith(parts[0].substring(0, 3))) {
                System.err.println("Skipping up to\"" + parts[i] + "\" from \"" + label + "\"");
                continue;
            }
            name += parts[i] + " ";
        }
        return name.trim();
    }

    private List<String> getBookLinks(boolean refresh) {
        if (!refresh) {
            // Attempt to read from file
            List<String> links = readListFromFile("mit-book-links.dat");
            if (links != null) {
                return links;
            } else {
                System.err.println("Warning! Reading file failed. Hitting web service");
            }
        }

        // Extract book links from the course urls
        List<String> bookLinks = new ArrayList<String>();
        for (String courseUrl : CATALOG_COURSE_URLS) {
            DataUtils.sleep();
            Response response = mainTab.go(courseUrl);
            if (response.getStatus() == 200) {
                bookLinks.addAll(extractBookLinks(response));
            } else {
                System.err.println("Failed with: " + courseUrl);
                break;
            }
        }

        System.out.println("Obtained " + bookLinks.size() + " book links");
        writeLinksToFile(bookLinks, "mit-book-links.dat");
        return bookLinks;
    }

    private void fetchBookInfo(List<String> links) {
        List<Material> materials = new ArrayList<Material>();
        for (String link : links) {
            System.out.println(link);
            DataUtils.sleep();
            String classCode = link.substring(link.lastIndexOf('=')+1);
            String html = mainTab.go(link).getPageSource();
            try {
                TagNode node = cleaner.clean(html);
                // First, determine the Required/Recommended/Unkown status
                Object[] divs = node.evaluateXPath("//*[@id='content']"); // Get content div
                if (divs.length != 1) {
                    System.err.println("Found != 1 content divs");
                    return;
                }
                TagNode parent = (TagNode)divs[0];
                Necessity necessity = Necessity.UNKNOWN; // reset per link
                for (Object obj : parent.getChildren()) {
                    // Iterate through children.
                    if (obj instanceof TagNode) {
                        TagNode child = (TagNode) obj;
                        //                        System.out.println(child.getName());
                        if ("h2".equals(child.getName())) {
                            // Necessity
                            necessity = parseNecessity(child.getText().toString());
                        } else if ("table".equals(child.getName()) &&
                                "displayTable".equals(child.getAttributeByName("class"))) {
                            // Actual entry! (Should happen AFTER a necessity has been seen)
                            Object[] cells = child.evaluateXPath("/tbody/tr/td[4]");
                            for (Object obj2 : cells) {
                                if (obj2 instanceof TagNode) {
                                    TagNode cell = (TagNode) obj2;
                                    String isbn = cell.getText().toString();
                                    materials.add(new Material(classCode, null, null, null, null, isbn, necessity.toString(),
                                            null, null, link));
                                    //                                    System.out.println("Found: " + classCode + ", " + isbn + ", " + necessity);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (XPatherException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.out.println("Found " + materials.size() + " materials");
        try {
            String filename = "mit-book-lists.dat";
            FileWriter fstream = new FileWriter(filePrefix + filename);
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

    // Helpers

    private List<String> extractBookLinks(Response response) {
        List<String> links = new ArrayList<String>();
        List<String> unmatched = new ArrayList<String>();
        Pattern urlRegex = Pattern.compile("javascript:PopUpHelp\\('(.*)'\\);");
        try {
            TagNode node = cleaner.clean(response.getPageSource());
            Object[] anchors = node
                    .evaluateXPath("/body/div[2]/table/tbody/tr[3]/td/table/tbody/tr/td/a");
            for (Object obj : anchors) {
                if (obj instanceof TagNode) {
                    TagNode anchor = (TagNode) obj;
                    String href = anchor.getAttributeByName("href");
                    if (href != null) {
                        Matcher m = urlRegex.matcher(href);
                        if (m.find()) {
                            String link = m.group(1);
                            System.out.println(link);
                            links.add(link);
                        } else {
                            unmatched.add(href);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XPatherException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.err.println("Unmatched: " + unmatched);
        return links;
    }

    private Necessity parseNecessity(String header) {
        if (header.contains("Recommended")) {
            return Necessity.RECOMMENDED;
        } else if (header.contains("Required")) {
            return Necessity.REQUIRED;
        } else if (header.contains("Unspecified")) {
            return Necessity.UNKNOWN;
        } else {
            System.err.println("Unknown necessity seen: " + header);
        }
        return Necessity.UNKNOWN;
    }

    // I/O Methods

    private void setFilePrefix() {
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

    private List<String> readListFromFile(String filename) {
        setFilePrefix();
        List<String> list = new ArrayList<String>();
        try {
            Scanner scanner = new Scanner(new File(filePrefix + filename));
            while (scanner.hasNext()) {
                list.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }

    private void writeLinksToFile(List<String> links, String filename) {
        setFilePrefix();
        try {
            // Create a stream for writing.
            FileWriter fileWriter = new FileWriter(filePrefix + filename);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            for (String link : links) {
                writer.write(link);
                writer.newLine();
            }
            writer.flush();
            writer.close();
            System.out.println("Wrote " + links.size() + " records to " + filename);
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }

    private void writeMapToFile(Map<String, String> classes, String filename) {
        setFilePrefix();
        try {
            // Create a stream for writing.
            FileWriter fileWriter = new FileWriter(filePrefix + filename);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            for (Entry<String, String> entry : classes.entrySet()) {
                writer.write(entry.getKey());
                writer.write(DataUtils.SEP);
                writer.write(entry.getValue());
                writer.newLine();
            }
            writer.flush();
            writer.close();
            System.out.println("Wrote " + classes.size() + " records to " + filename);
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }
}
