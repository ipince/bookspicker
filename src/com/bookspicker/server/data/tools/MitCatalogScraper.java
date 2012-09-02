package com.bookspicker.server.data.tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

    private static final String CATALOG_TERM_STR = "2013FA";
    private static final String CATALOG_BASE = "http://student.mit.edu/catalog/";
    private static final String CATALOG_SEARCH_URL = CATALOG_BASE + "search.cgi?search=&style=verbatim&when=C&days_offered=*&start_time=*&duration=*&total_units=*";
    private static final String BOOK_URL_BASE = "http://sisapp.mit.edu/textbook/books.html?Term="
            + CATALOG_TERM_STR + "&Subject=";

    public static final String CLASS_NAME_FILE = "mit-class-names.dat";
    public static final String CLASS_URL_FILE = "mit-class-urls.dat";
    private static final String BOOK_LINKS_FILE = "mit-book-links.dat";
    public static final String BOOK_LIST_FILE = "mit-book-lists.dat";

    private final Prowser prowser = new Prowser();
    private final Tab mainTab = prowser.createTab();
    private final HtmlCleaner cleaner = new HtmlCleaner();

    private String filePrefix;

    // visible for testing
    protected final Map<String, String> classNames = new HashMap<String, String>();
    private final Map<String, String> classUrls = new HashMap<String, String>();
    private final Set<String> courseUrls = new HashSet<String>();
    // visible for testing
    protected final Map<String, String> bookLinks = new HashMap<String, String>();
    private final List<Material> materials = new ArrayList<Material>();

    public static void main(String[] args) {
        MitCatalogScraper scraper = new MitCatalogScraper();

        boolean refresh = false;

        if (refresh || !scraper.loadClassInfoFromDisk()) {
            scraper.updateClassInfoAndCourseUrls();
        }
        if (refresh || !scraper.loadBookLinksFromDisk()) {
            scraper.updateBookLinks();
        }
        scraper.fetchBookInfo();
    }

    private boolean loadClassInfoFromDisk() {
        Map<String, String> loadedClassNames = DataUtils.readMapFromFile(CLASS_NAME_FILE);
        if (loadedClassNames != null) {
            classNames.clear();
            classNames.putAll(loadedClassNames);
        } else {
            return false;
        }
        Map<String, String> loadedClassUrls = DataUtils.readMapFromFile(CLASS_URL_FILE);
        if (loadedClassUrls != null) {
            classUrls.clear();
            classUrls.putAll(loadedClassUrls);
        } else {
            return false;
        }
        return true;
    }

    private boolean loadBookLinksFromDisk() {
        Map<String, String> loadedBookLinks = DataUtils.readMapFromFile(BOOK_LINKS_FILE);
        if (loadedBookLinks != null) {
            bookLinks.clear();
            bookLinks.putAll(loadedBookLinks);
            return true;
        }
        return false;
    }

    /**
     * Fetches class names, urls, and course urls from the course catalog.
     */
    private void updateClassInfoAndCourseUrls() {
        // We get the class names by performing an advanced search on the catalog
        // for everything that is offered in the current semester:
        String results = mainTab.go(CATALOG_SEARCH_URL).getPageSource();
        classNames.clear(); classUrls.clear(); courseUrls.clear();
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
                    classNames.put(code, name);
                    classUrls.put(code, classUrl);
                    courseUrls.add(CATALOG_BASE + courseUrl);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPatherException e) {
            e.printStackTrace();
        }
        System.out.println("Got " + classNames.size() + " classes");
        System.out.println("Got " + courseUrls + " courses");
        DataUtils.writeMapToFile(classNames, CLASS_NAME_FILE);
        DataUtils.writeMapToFile(classUrls, CLASS_URL_FILE);
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

    /**
     * Updates map of classCode->bookUrl.
     * 
     * Does so by first generating a bookUrl for each class based on the
     * canonical way the book URLs are:
     * 
     * 1.285 => sisapp.mit.edu/textbook/books.html?Term=2013FA&Subject=1.285
     * 
     * For joint subjects, we need a slightly different URL that we cannot
     * create a priori, so we then scrape the courseUrls and extract all
     * bookUrls we find.
     * 
     * If we find something like:
     * sisapp.mit.edu/textbook/books.html?Term=2013FA&
     * Subject=1.285&Source=11.482
     * 
     * then the bookUrl for 1.285 would get replaced by it.
     * 
     * ====
     * 
     * Reasoning:
     * 
     * MIT offers the following URL to display book lists:
     * 
     * sisapp.mit.edu/textbook/books.html?Term=2013FA&Subject=1.285[&Source=
     * 11.482]
     * 
     * When source is NOT present, this URL fetches book data for (Term,
     * Subject). When Source IS present, it fetches data for (Term, Source), but
     * displays Subject as the actual subject.
     * 
     * This is typically used for joint classes that have different course
     * numbers but share the same books.
     * 
     * For joint classes, the course catalog for class 1.285 will link to the
     * link above. For class 11.482, it will link to:
     * 
     * sisapp.mit.edu/textbook/books.html?Term=2013FA&Subject=11.482
     * 
     * Note: URLs are Case Sensitive!!
     */
    private void updateBookLinks() {
        // 1. Generate generic bookUrls for each class.
        for (String classCode : classNames.keySet()) {
            bookLinks.put(classCode, BOOK_URL_BASE + classCode);
        }

        // 2. Fetch extra book links from course urls.
        List<String> links = getBookLinksFromCourseUrls(false);
        int replaced = 0;
        for (String link : links) {
            if (replaceLinkIfJoint(link)) {
                replaced++;
            }

        }
        System.out.println("Replaced " + replaced + " book links.");
        DataUtils.writeMapToFile(bookLinks, BOOK_LINKS_FILE);
    }

    // visible for testing
    protected boolean replaceLinkIfJoint(String link) {
        // Only care if it's "special", i.e. contains a 'Source' param
        if (link.contains("Source")) {
            // Extract class code it's supposed to be for.
            String paramStr = link.substring(link.lastIndexOf("?"));
            String params[] = paramStr.split("&");
            for (String pair : params) {
                String keyVal[] = pair.split("=");
                if ("Subject".equals(keyVal[0])) {
                    // Found subject
                    if (classNames.containsKey(keyVal[1])) {
                        bookLinks.put(keyVal[1], link); // replace link
                        return true;
                    } else {
                        System.err.println("Warning: Found unknown class in book link: " + link);
                    }
                }
            }
        }
        return false;
    }

    private List<String> getBookLinksFromCourseUrls(boolean refresh) {
        // Extract book links from the course urls
        List<String> bookLinks = new ArrayList<String>();
        for (String courseUrl : courseUrls) {
            DataUtils.sleep();
            Response response = mainTab.go(courseUrl);
            if (response.getStatus() == 200) {
                bookLinks.addAll(extractBookLinks(response));
            } else {
                System.err.println("Failed with: " + courseUrl);
                break;
            }
        }

        System.out.println("Obtained " + bookLinks.size() + " book links from course urls");
        return bookLinks;
    }

    private static final String NO_BOOK_INFO = "No text books are recorded for your request.";
    private static final String NO_BOOKS_NEEDED = "Course Has No Materials";

    /**
     * Fetches book information from book links.
     */
    private void fetchBookInfo() {
        materials.clear();
        for (Entry<String, String> entry : bookLinks.entrySet()) {
            String link = entry.getValue();
            String classCode = entry.getKey();
            System.out.println(link);
            DataUtils.sleep();
            String html = mainTab.go(link).getPageSource();
            if (html.contains(NO_BOOK_INFO)) {
                // No book info, skip.
                System.out.println("No book info");
                continue;
            } else if (html.contains(NO_BOOKS_NEEDED)) {
                materials.add(new Material(classCode, null, null, null, null, null, null, null,
                        null, link));
                System.out.println("No books needed!!");
                continue;
            }
            try {
                TagNode node = cleaner.clean(html);
                // First, determine the Required/Recommended/Unknown status
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
                                    System.out.println("Found: " + classCode + ", " + isbn + ", "
                                            + necessity);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XPatherException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Found " + materials.size() + " materials");
        // TODO(rodrigo): extract to method.
        try {
            setFilePrefix();
            String filename = BOOK_LIST_FILE;
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
            e.printStackTrace();
        } catch (XPatherException e) {
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
}
