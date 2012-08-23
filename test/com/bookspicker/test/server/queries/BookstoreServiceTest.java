package com.bookspicker.test.server.queries;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.bookspicker.server.queries.AbeQuery;
import com.bookspicker.server.queries.AlibrisQuery;
import com.bookspicker.server.queries.AmazonQuery;
import com.bookspicker.server.queries.EbayQuery;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.Bundle;
import com.bookspicker.shared.Offer;
import com.bookspicker.shared.Bundle.Condition;

import junit.framework.TestCase;

/**
 * Test our queries from the different online sites.
 * From Amazon it test for getting All book info + prices
 * From all the other sites it tests for getting prices
 * On all sites:
 * Tests run on single / multiple valid / invalid ISBNs (+ on amazon only books parameters)
 * Tests for both ISBN10 and EAN (ISBN13)
 * @author Jonathan
 */
public class BookstoreServiceTest extends TestCase {
  
    private String validIsbn1_10;
    private String validIsbn1_13;
    private String validIsbn2_10;
    private String validIsbn2_13;
    private String invalidIsbn;
    
    private Book validBook1;
    private Book validBook2;
    
    private Bundle bundle;
    
    private AmazonQuery amazonQuery;
    private EbayQuery ebayQuery;
    private AlibrisQuery alibrisQuery;
    private AbeQuery abeQuery;
    private Book invalidBook;
    public void setUp() {      
      amazonQuery = new AmazonQuery();
      ebayQuery = new EbayQuery();
      alibrisQuery = new AlibrisQuery();
      abeQuery = new AbeQuery();
      
      // Intro to Algo.
      validIsbn1_10 = "0262033844";
      validIsbn1_13 = "9780262033848";
      // Foundations of analog and digital
      validIsbn2_10 = "1558607358";
      validIsbn2_13 = "9781558607354";
      
      //Invalid isbn for tests 
      //TODO(Jonathan) numbers that don't fit the ISBN structure shouldn't be passed to the server at all - this should be tested on the client side.
      invalidIsbn = "1999999333";
      String[] authorsArray1 = {"Thomas H. Cormen", "Charles E. Leiserson", "Ronald L. Rivest", 
          "Clifford Stein"};
      String[] authorsArray2 = {"Anant Agarwal", "Jeffrey Lang"};
      
      validBook1 = new Book("Introduction to Algorithms, Third Edition", authorsArray1, 
          validIsbn1_10, validIsbn1_13, 8700, 
          "http://ecx.images-amazon.com/images/I/41hJ7gLDOmL._SL160_.jpg", 3, "The MIT Press");

      validBook2 = new Book("Foundations of Analog and Digital Electronic Circuits " +
      		"(The Morgan Kaufmann Series in Computer Architecture and Design)", authorsArray2,
      		validIsbn2_10, validIsbn2_13, 9995, 
      		"http://ecx.images-amazon.com/images/I/51npQ9dzARL._SL160_.jpg", 1, "Morgan Kaufmann");
      
      invalidBook =  new Book("FML - It is 5am and I am coding", authorsArray1, invalidIsbn, "1234567890123", 
          999999999, "", 1, "Moshiko LLC");
      
      
      bundle = new Bundle();
      bundle.addBook(validBook1);
      bundle.setCondition(Condition.NEW);
      //validNewOffer1 = new Offer
    
    
    }
  
    @Test
    public void testAmazonSoloBookInfoISBN10() {
      List<String> bookList = new ArrayList<String>();
      bookList.add(validIsbn1_10);
      Book book =  amazonQuery.getBooksInfoByIsbn(bookList).get(0);
      assertEquals(validBook1, amazonQuery.getBooksInfoByIsbn(bookList).get(0));
    }
    
    @Test
    public void testAmazonSoloBookInfoISBN13() {
      List<String> bookList = new ArrayList<String>();
      bookList.add(validIsbn2_13);
      assertEquals(validBook2, amazonQuery.getBooksInfoByIsbn(bookList).get(0));
    }
    
    @Test
    public void testAmazonMultiBookInfo() {
      List<String> bookList = new ArrayList<String>();
      bookList.add(validIsbn1_13);
      bookList.add(validIsbn2_10);
      List<Book> bookListResult = new ArrayList<Book>();
      bookListResult.add(validBook1);
      bookListResult.add(validBook2);
      assertEquals(bookListResult, amazonQuery.getBooksInfoByIsbn(bookList));
    }
    
    @Test
    public void testAmazonSoloInvalidBookInfo() {
      List<String> bookList = new ArrayList<String>();
      bookList.add(invalidIsbn);
      assertEquals(null, amazonQuery.getBooksInfoByIsbn(bookList).get(0));
    }
        
    @Test
    public void testAmazonMultiInvalidBookInfo() {
      List<String> bookList = new ArrayList<String>();
      bookList.add(validIsbn1_13);
      bookList.add(invalidIsbn);
      bookList.add(validIsbn2_13);
      List<Book> bookListResult = new ArrayList<Book>();
      bookListResult.add(validBook1);
      bookListResult.add(null);
      bookListResult.add(validBook2);
      assertEquals(bookListResult, amazonQuery.getBooksInfoByIsbn(bookList));
    }
    
    @Test 
    public void testAmazonValidQuerySearch() {
     assertEquals(validBook1, amazonQuery.getBooksInfoByQuery(validBook1.getTitle()).get(0));
    }
    
    @Test 
    public void testAmazonInvalidQuerySerach() {
     assertEquals(new ArrayList<Book>(), amazonQuery.getBooksInfoByQuery("dsjbcsdhcbdsjhcbdsjhbcdsjhbc"));
    }
    
    //TODO(Jonathan) - test null for all the fields
    @Test 
    public void testAmazonOffers() {
     amazonQuery.getBooksOffers(bundle);
     Offer offer = bundle.getBookOffers(validBook1).get(0);
     assertTrue(testForValidOffer(offer));
    }
    
    @Test 
    public void testEbayOffers() {
      ebayQuery.getBooksOffers(bundle);
      Offer offer = bundle.getBookOffers(validBook1).get(0);
      assertTrue(testForValidOffer(offer));
    }
    
    @Test 
    public void testAlibrisOffers() {
      alibrisQuery.getBooksOffers(bundle);
      Offer offer = bundle.getBookOffers(validBook1).get(0);
      assertTrue(testForValidOffer(offer));
    }
    
    
    @Test 
    public void testAbeOffers() {
      abeQuery.getBooksOffers(bundle);
      Offer offer = bundle.getBookOffers(validBook1).get(0);
      assertTrue(testForValidOffer(offer));
    }
    
    @Test
    public void testAlibrisSoloInvalidBookInfo() {
      Bundle bundle = new Bundle();
      bundle.addBook(invalidBook);      
      alibrisQuery.getBooksOffers(bundle);
      assertTrue(bundle.getOffersSortedByPrice(invalidBook).isEmpty());
    }
    
    @Test
    public void testAbeSoloInvalidBookInfo() {
      Bundle bundle = new Bundle();
      bundle.addBook(invalidBook);      
      abeQuery.getBooksOffers(bundle);
      assertTrue(bundle.getOffersSortedByPrice(invalidBook).isEmpty());
    }
   
    private boolean testForValidOffer(Offer offer) {
      if (offer.getCondition() == null || offer.getPrice() == -1  || offer.getSellerName() == null 
          || offer.getShipping() == -1 || offer.getUrl() == null) {
        return false;
      }
    return true;
    }    
    
    
 }