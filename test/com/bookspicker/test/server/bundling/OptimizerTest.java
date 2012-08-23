package com.bookspicker.test.server.bundling;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.bookspicker.server.bundling.Filter;
import com.bookspicker.server.bundling.Optimizer;
import com.bookspicker.server.social.SocialGraph;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.Bundle;
import com.bookspicker.shared.Friendship;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.Location;
import com.bookspicker.shared.Offer;
import com.bookspicker.shared.OnlineOffer;
import com.bookspicker.shared.User;
import com.bookspicker.shared.LocalOffer.Condition;
import com.bookspicker.shared.Offer.StoreName;

public class OptimizerTest extends TestCase {
	
	private Bundle bundle;
	private Book book1;
	private Book book2;
	private Book book3;
	
	private Offer offer1;
	private Offer offer2;
	private Offer offer3;
	
	private LocalOffer lo1;
	private LocalOffer lo2;
	private LocalOffer lo3;
	
	private User buyer;
	private User seller1;
	private User seller2;
	
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
		
		// Create some online offers
		offer1 = new OnlineOffer(10, 2, StoreName.AMAZON, "Seller1", "Used", null);
		offer2 = new OnlineOffer(20, 3, StoreName.ALIBRIS, "Seller2", "Used", null);
		offer3 = new OnlineOffer(30, 5, StoreName.HALF, "Seller1", "Used", null);
		
		// Create users
		seller1 = new User("1", "John Doe");
		seller2 = new User("2", "Jane Doe");
		buyer = new User("3", "Johnny Quest");
		
		// Create some local offers
		lo1 = new LocalOffer(seller2, book1, Condition.GOOD, false);
		lo1.setFixedPrice(1000);
		lo2 = new LocalOffer(seller1, book1, Condition.LIKE_NEW, false);
		lo2.setFixedPrice(1000);
		lo3 = new LocalOffer(seller1, book1, Condition.GOOD, false);
		lo3.setFixedPrice(900);
		
		// Create bundle
		bundle = new Bundle();
	}
	
	@Test
	public void testNormalBundle() {
		bundle.addOffer(book1, offer1);
		bundle.addOffer(book2, offer1);
		bundle.addOffer(book2, offer2);
		bundle.addOffer(book3, offer2);
		bundle.addOffer(book3, offer3);
		
		Optimizer.optimize(bundle, null, null);
		
		assertEquals(offer1, bundle.getSelectedOffer(book1));
		assertEquals(offer1, bundle.getSelectedOffer(book2));
		assertEquals(offer2, bundle.getSelectedOffer(book3));
		
		bundle.addOffer(book3, offer1);
		
		Optimizer.optimize(bundle, null, null);
		assertEquals(offer1, bundle.getSelectedOffer(book3));
	}
	
	@Test
	public void testBundleWithUserChosenOffers() {
		bundle.addOffer(book1, offer1);
		bundle.addOffer(book2, offer1);
		bundle.addOffer(book2, offer2);
		bundle.addOffer(book3, offer2);
		bundle.addOffer(book3, offer3);
		
		offer2.setChosenOffer(true); // for book2 and book3
		
		Optimizer.optimize(bundle, null, null);
		
		assertEquals(offer1, bundle.getSelectedOffer(book1));
		assertEquals(offer2, bundle.getSelectedOffer(book2));
		assertEquals(offer2, bundle.getSelectedOffer(book3));
	}
	
	@Test
	public void testStoreFilter() {
		bundle.addOffer(book1, offer1);
		bundle.addOffer(book2, offer1);
		bundle.addOffer(book2, offer2);
		bundle.addOffer(book3, offer1);
		bundle.addOffer(book3, offer3);
		
		Filter filter = new Filter();
		filter.disableStore(StoreName.AMAZON);
		
		Optimizer.optimize(bundle, filter, null);
		
		assertNull(bundle.getSelectedOffer(book1));
		assertEquals(offer2, bundle.getSelectedOffer(book2));
		assertEquals(offer3, bundle.getSelectedOffer(book3));
	}
	
	@Test
	public void testLocalOfferLocation() {
		bundle.addOffer(book1, lo1);
		bundle.addOffer(book1, lo2);
		bundle.addOffer(book1, lo3);
		
		SocialGraph.addFriend(new Friendship("1", "3"));
		SocialGraph.addFriend(new Friendship("1", "2"));
		
		List<Offer> ordered = new ArrayList<Offer>();
		ordered.add(lo3);
		ordered.add(lo2);
		ordered.add(lo1);
		
		// User has no location
		Optimizer.optimize(bundle, null, buyer);
		
		List<Offer> offers = bundle.getBookOffers(book1);
		assertTrue(offers.get(0).equals(lo3));
		
		
		// Only offer1 has location
		buyer.setLocation(Location.SENIOR_HOUSE);
		lo1.setLocation(Location.BAKER);
		
		Optimizer.optimize(bundle, null, buyer);
		offers = bundle.getBookOffers(book1);
		assertTrue(offers.get(0).equals(lo3));
		assertTrue(offers.get(1).equals(lo1));
		assertTrue(offers.get(2).equals(lo2));
		
		// Both offers have location
		lo2.setLocation(Location.EAST_CAMPUS);
		Optimizer.optimize(bundle, null, buyer);
		offers = bundle.getBookOffers(book1);
		assertTrue(offers.equals(ordered));
	}
	
	@Test
	public void testLocalOfferSocialDistance() {
		bundle.addOffer(book1, lo1);
		bundle.addOffer(book1, lo2);
		bundle.addOffer(book1, lo3);
		
		SocialGraph.addFriend(new Friendship("1", "3"));
		SocialGraph.addFriend(new Friendship("1", "2"));
		
		List<Offer> ordered = new ArrayList<Offer>();
		ordered.add(lo3);
		ordered.add(lo2);
		ordered.add(lo1);
		
		Optimizer.optimize(bundle, null, buyer);
		List<Offer> offers = bundle.getBookOffers(book1);
		
		assertTrue(offers.equals(ordered));
	}
	
	@Test
	public void testLocalOfferDate() {
		bundle.addOffer(book1, lo1);
		bundle.addOffer(book1, lo2);
		bundle.addOffer(book1, lo3);
		
		SocialGraph.addFriend(new Friendship("1", "3"));
		SocialGraph.addFriend(new Friendship("2", "3"));
		
		List<Offer> ordered = new ArrayList<Offer>();
		ordered.add(lo3);
		ordered.add(lo1);
		ordered.add(lo2);
		
		Optimizer.optimize(bundle, null, buyer);
		List<Offer> offers = bundle.getBookOffers(book1);
		
		assertTrue(offers.equals(ordered));
	}

}
