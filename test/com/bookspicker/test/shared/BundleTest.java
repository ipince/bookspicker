package com.bookspicker.test.shared;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;

import com.bookspicker.shared.Book;
import com.bookspicker.shared.Bundle;
import com.bookspicker.shared.Offer;
import com.bookspicker.shared.OnlineOffer;
import com.bookspicker.shared.Offer.StoreName;

public class BundleTest extends TestCase {
	
	private Bundle bundle;
	private Book book1;
	private Book book2;
	private Book book3;
	private Book book1Copy;
	
	private Offer offer1;
	private Offer offer2;
	private Offer offer3;
	
	@Override
	public void setUp() {
		// Create some books
		book1 = new Book("Title1",
				new String[] {"Author11", "Author12"},
				"1111111111", "9781111111111", 1, null,
				0, "Publisher1");
		book2 = new Book("Title2",
				new String[] {"Author21", "Author22"},
				"2222222222", "9782222222222", 2, null,
				0, "Publisher2");
		book3 = new Book("Title3",
				new String[] {"Author31", "Author32"},
				"3333333333", "9783333333333", 3, null,
				0, "Publisher3");
		book1Copy = new Book("Title1",
				new String[] {"Author11", "Author12"},
				"1111111111", "9781111111111", 1, null,
				0, "Publisher1");
		
		// Create some offers
		offer1 = new OnlineOffer(10, 2, StoreName.AMAZON, "Seller1", "Used", null);
		offer2 = new OnlineOffer(20, 3, StoreName.ALIBRIS, "Seller2", "Used", null);
		offer3 = new OnlineOffer(30, 5, StoreName.HALF, "Seller1", "Used", null);
		
		// Create bundle with
		bundle = new Bundle();
	}
	
	// TODO(rodrigo): add more unit tests
	
	@Test
	public void testAddBook() {
		bundle.addBook(book1);
		assertTrue("Couldn't add book", bundle.getBooks().size() == 1);
		
		bundle.addBook(book2);
		bundle.addBook(book3);
		assertTrue("Couldn't add book", bundle.getBooks().size() == 3);
		
		bundle.addBook(book1Copy);
		assertTrue("Added same book twice", bundle.getBooks().size() == 3);
	}
	
	@Test
	public void testRemoveBook() {
		bundle.removeBook(book1);
		assertTrue("Removed non-existent book", bundle.getBooks().isEmpty());
		
		bundle.addBook(book1);
		bundle.removeBook(book1);
		assertTrue("Couldn't remove book", bundle.getBooks().isEmpty());
	}
	
	@Test
	public void testGetBooks() {
		assertTrue(bundle.getBooks().isEmpty());
		
		bundle.addBook(book1);
		assertTrue("Incorrect book list size",
				bundle.getBooks().size() == 1);
		
		bundle.addBook(book2);
		assertTrue("Incorrect book list size",
				bundle.getBooks().size() == 2);
		
		bundle.addBook(book1);
		assertTrue("Incorrect book list size",
				bundle.getBooks().size() == 2);
		
		Set<Book> addedBooks = new HashSet<Book>();
		addedBooks.add(book1);
		addedBooks.add(book2);
		
		assertEquals("Bundle books and added books don't match!", 
				bundle.getBooks(), addedBooks);
	}
	
	@Test
	public void testGetBookByIsbn() {
		bundle.addBook(book1);
		bundle.addBook(book2);
		assertEquals(book1, bundle.getBookByIsbn("1111111111"));
	}
	
	@Test
	public void testAddOffer() {
		bundle.addOffer(book1, offer1);
		bundle.addOffer(book1, offer2);
		bundle.addOffer(book2, offer3);
		
		List<Offer> offers = new ArrayList<Offer>();
		offers.add(offer1);
		offers.add(offer2);
		assertEquals(offers, bundle.getBookOffers(book1));
		
		offers = new ArrayList<Offer>();
		offers.add(offer3);
		assertEquals(offers, bundle.getBookOffers(book2));
	}
	
	@Test
	public void testGetOffers() {
		assertNull(bundle.getBookOffers(null));
		assertNull(bundle.getBookOffers(book1));
		
		bundle.addOffer(book1, offer1);
		bundle.addOffer(book1, offer2);
		
		List<Offer> offers = new ArrayList<Offer>();
		offers.add(offer1);
		offers.add(offer2);
		assertEquals(offers, bundle.getBookOffers(book1));
	}
	
	@Test
	public void testGetOffersByPrice() {
		assertNull(bundle.getOffersSortedByPrice(null));
		assertNull(bundle.getOffersSortedByPrice(book1));
		
		bundle.addOffer(book1, offer2);
		bundle.addOffer(book1, offer1);
		
		List<Offer> offers = new ArrayList<Offer>();
		offers.add(offer1);
		offers.add(offer2);
		assertEquals(offers, bundle.getOffersSortedByPrice(book1));
	}
	
	@Test
	public void testSelectedOffer() {
		bundle.addOffer(book1, offer1);
		bundle.addOffer(book1, offer2);
		bundle.setSelectedOffer(book1, offer1);
		
		assertEquals("Didn't select offer", offer1, bundle.getSelectedOffer(book1));
		
		List<Offer> offers = new ArrayList<Offer>();
		offers.add(offer1);
		offers.add(offer2);
		assertEquals("Re-added offer when selecting it", offers, bundle.getBookOffers(book1));
		
		bundle.setSelectedOffer(book2, offer3);
		assertEquals("Didn't select offer for new book", offer3, bundle.getSelectedOffer(book2));
		
		bundle.setSelectedOffer(book2, offer2);
		assertEquals("Didn't replace selected offer", offer2, bundle.getSelectedOffer(book2));
	}
	
	@Test
	public void testGetCheapestOffer() {
		assertNull(bundle.getCheapestOffer(null));
		assertNull(bundle.getCheapestOffer(book1));
		
		bundle.addOffer(book1, offer2);
		bundle.addOffer(book1, offer1);
		assertEquals(offer1, bundle.getCheapestOffer(book1));
	}
	
	@Test
	public void testTotalListCost() {
		// TODO(rodrigo)		
	}
	
	@Test
	public void testTotalBundleCost() {
		// TODO(rodrigo)
	}

}
