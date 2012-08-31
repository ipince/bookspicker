package com.bookspicker.server.data.tools;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class MitCatalogScraperTest extends TestCase {

    private MitCatalogScraper scraper;

    @Override
    @Before
    public void setUp() {
        scraper = new MitCatalogScraper();
    }

    @Test
    public void testReplaceLinkIfJoint() {
        String jointLink = "sisapp.mit.edu/textbook/books.html?Term=2013FA&Subject=ESD.482&Source=1.00";

        scraper.classNames.put("ESD.482", "someName");
        scraper.bookLinks.put("ESD.482", "someUrl");
        scraper.replaceLinkIfJoint(jointLink);
        assertEquals(jointLink, scraper.bookLinks.get("ESD.482"));
    }
}
