package com.bookspicker.server.data;

import java.util.ArrayList;
import java.util.List;

import com.bookspicker.shared.Book;
import com.bookspicker.shared.Friendship;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.Location;
import com.bookspicker.shared.User;
import com.bookspicker.shared.LocalOffer.Condition;
import com.bookspicker.shared.LocalOffer.Strategy;

public class Sandbox {
	
	public static void main(String[] args) {
		
		FriendshipManager manager = FriendshipManager.getManager();
		
		// 1's friends
		manager.createAndSaveFriendship("1", "2");
		manager.createAndSaveFriendship("1", "3");
		manager.createAndSaveFriendship("1", "4");
		manager.createAndSaveFriendship("1", "5");
		
		// 5's friends
		manager.createAndSaveFriendship("5", "1");
		
		// 6's friends (became 1's friend after 1 joined BP)
		manager.createAndSaveFriendship("6", "1");
		
		List<Friendship> friendships = manager.getFriendshipsInvolving("1");
		for (Friendship f : friendships)
			System.out.println(f.toString());
	}

	/**
	 * Generates some 'fake' data (a User and some LocalOffers)
	 * and stores it in the DB. Used for testing purposes.
	 * 
	 * If you modify this, make sure that other classes that
	 * make use of it (e.g., UserServiceImpl) still work as
	 * intended.
	 */
	public static User addSomeData() {
//		System.out.println("Adding a user with one offer to DB");
		User user = generateAndSaveUsers();
		List<LocalOffer> offers = generateAndSaveOffers(user);
		user.setOffers(offers);
		return user;
	}
	
	public static List<LocalOffer> generateAndSaveOffers(User user) {
		List<LocalOffer> offers = new ArrayList<LocalOffer>();
		
		List<Book> books = generateBooks(false);
		
		LocalOffer offer = new LocalOffer(user, books.get(0), Condition.ACCEPTABLE, true);
		offer.setClassCode("6.006");
		offer.setStrategy(Strategy.AGGRESSIVE);
		offer.setLowerBoundPrice(2000);
		offer.setLocation(Location.BAKER);
		offer.setComments("Book is in pretty good condition");
		offers.add(LocalOfferManager.getManager().save(offer));
		
		offer = new LocalOffer(user, books.get(1), Condition.VERY_GOOD, false);
		offer.setClassCode("18.03");
		offer.setFixedPrice(1999);
		offer.setLocation(Location.STUDENT_CENTER);
		offer.setComments("Not too useful for the class");
		offer.deactivate();
		offers.add(LocalOfferManager.getManager().save(offer));
		
		offer = new LocalOffer(user, books.get(2), Condition.LIKE_NEW, true);
		offer.setClassCode("18.06");
		offer.setLocation(Location.NEW_HOUSE);
		offer.buy("buyer@example.com", 1700);
		offers.add(LocalOfferManager.getManager().save(offer));
		
		return offers;
	}
	
	public static List<Book> generateBooks(boolean save) {
		List<Book> books = new ArrayList<Book>();
		
		// Intro to algs
		Book book = new Book("Introduction to Algorithms, Third Edition", new String[]{"Cormen", "Leiserson", "Rivest", "Stein"}, "0262033844", "9780262033844", 8700, null, 2, "Ass");
		if (save)
			BookManager.getManager().saveBook(book);
		books.add(book);
		
		// DiffEq
		book = new Book("Elementary Differential Equations With Boundary Value Problems", new String[]{"C. H. Edwards", "David E. Penney"}, "013253410X", "978013253410X", 9067, null, -1, "Asss");
		if (save)
			BookManager.getManager().saveBook(book);
		books.add(book);
		
		// Linear Algebra
		book = new Book("Introduction to Linear Algebra", new String[]{"Gilbert Strang"}, "0980232716", "9780980232716", 8750, null, 4, "Ass");
		if (save)
			BookManager.getManager().saveBook(book);
		books.add(book);
		
		return books;
	}
	
	/**
	 * Generates and persists 2 users, John Doe and Johnny Quest.
	 * @return
	 */
	public static User generateAndSaveUsers() {
		User user = new User("123", "Johnny Quest");
		user.setFbEmail("johnny@example.com");
		user.setMitEmail("jquest@mit.edu");
		UserManager.getManager().saveUser(user);
		
		user = new User("123", "John Doe");
		user.setFbEmail("test@example.com");
		user.setMitEmail("jdoe@mit.edu");
		UserManager.getManager().saveUser(user);
		
		return user;
	}
	
	public static void printOffers(List<LocalOffer> offers) {
		for (LocalOffer offer : offers)
			System.out.println("Book isbn: " + offer.getBook().getIsbn());
	}

}
