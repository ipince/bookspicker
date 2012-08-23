package com.bookspicker.test.shared;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.bookspicker.server.data.LocalOfferManager;
import com.bookspicker.server.data.Sandbox;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.User;
import com.bookspicker.shared.LocalOffer.Condition;
import com.bookspicker.shared.LocalOffer.Strategy;

public class LocalOfferTest extends TestCase {
	
	private Book book1;
	private Book book2;
	private Book book3;
	private Book book1Copy;
	
	private User user;
	
	private LocalOffer fixedPriceOffer;
	private LocalOffer conservativeOffer;
	private LocalOffer aggressiveOffer;
	
	private static final long dateError = 500; // half a second
	
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
		
		user = new User("123", "John Doe");
		
		// Create some local offers
		fixedPriceOffer = new LocalOffer(user, book1, Condition.GOOD, false);
		fixedPriceOffer.setFixedPrice(1000);
		conservativeOffer = new LocalOffer(user, book2, Condition.VERY_GOOD, true);
		conservativeOffer.setStrategy(Strategy.CONSERVATIVE);
		aggressiveOffer = new LocalOffer(user, book3, Condition.ACCEPTABLE, true);
		aggressiveOffer.setStrategy(Strategy.AGGRESSIVE);
	}
	
//	@Test
//	public void testDatabasePersistence() {
//		// Add some data
//		User user = Sandbox.generateAndSaveUsers();
//		List<LocalOffer> offers = Sandbox.generateAndSaveOffers(user);
//		
//		// Retrieve the offers by using the books in them (with ids)
//		Book book;
//		List<LocalOffer> newOffers;
//		for (LocalOffer offer : offers) {
//			book = offer.getBook();
//			System.out.println("Book id is: " + book.getId());
//			newOffers = LocalOfferManager.getManager().getLocalOffersFor(book);
//			System.out.println("Offers for it are: ");
//			for (LocalOffer newOffer : newOffers) {
//				System.out.println("\t" + newOffer.toString());
//			}
//		}
//		
//		// Retrieve the offers by using the books in them (with ids)
//		List<Book> books = Sandbox.generateBooks(false);
//		for (Book bookNoId : books) {
//			System.out.println("Book ISBN is: " + bookNoId.getIsbn());
//			newOffers = LocalOfferManager.getManager().getLocalOffersFor(bookNoId);
//			System.out.println("Offers for it are: ");
//			for (LocalOffer newOffer : newOffers) {
//				System.out.println("\t" + newOffer.toString());
//			}
//		}
//	}
	
	@Test
	public void testCreationDate() {
		Date now = new Date();
		assertTrue("Creation date not set properly", now.getTime() - fixedPriceOffer.getCreationDate().getTime() < dateError);
		assertTrue("Creation date not set properly", now.getTime() - aggressiveOffer.getCreationDate().getTime() < dateError);
		assertTrue("Creation date not set properly", now.getTime() - conservativeOffer.getCreationDate().getTime() < dateError);
	}
	
	@Test
	public void testLastPostingDate() throws InterruptedException {
		Date now = new Date();
		// Initial posting date
		assertTrue("Last posting date date not set properly", now.getTime() - fixedPriceOffer.getLastPostingDate().getTime() < dateError);
		
		// Subsequent posting dates
		fixedPriceOffer.deactivate();
		try {
			Thread.sleep(1000);
			now = new Date();
			fixedPriceOffer.activate();
			assertTrue("Last posting date date not set properly", now.getTime() - fixedPriceOffer.getLastPostingDate().getTime() < dateError);
		} catch (InterruptedException e) {
			// do nothing
		}
		
		// Should Expire?
	}
	
	@Test
	public void testSold() {
		
		// First sale
		
		// Subsequent sales
		
	}
	
	@Test
	public void testActivate() {
		
	}
	
	@Test
	public void testDeactivate() {
		
	}
	
	@Test
	public void testTimeOnMarketFixed() {
		
	}
	
	@Test
	public void testTimeOnMarketAggressive() {
		
	}
	
	@Test
	public void testTimeOnMarketConservative() {
		
	}
	
	@Test
	public void testDeleted() {
		
	}

}
