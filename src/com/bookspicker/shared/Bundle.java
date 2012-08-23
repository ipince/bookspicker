package com.bookspicker.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Contains the list of all the books that the user is searching for with the
 * list of all the offers and the best set of offers.
 * 
 * @author Jonathan Goldberg, Rodrigo Ipince
 */
public class Bundle implements IsSerializable {

	public enum Condition {
		NEW("New"), USED("Used"), ALL("All");

		String name;

		Condition(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	Map<Book, List<Offer>> offers; // all offers for each book
	Map<Book, List<Offer>> sortedOffers;
	Map<Book, Offer> selectedOffers; // selected offers
	Map<Book, Boolean> bookNeedUpdates; // books that need their offers updated
	long oldestOfferTimeStamp = -1;
	Condition condition = Condition.ALL;
	
	/**
	 * The sum of all the list prices of the books in this Bundle,
	 * in pennies. Set to -1 when the Bundle is empty.
	 */
	int totalListPrice = -1;
	
	/**
	 * The sum, in pennies
	 */
	int totalBundlePrice = -1;

	/**
	 * 0 means that it has not been set.
	 * -1 means false.
	 * 1 means true.
	 */
	int totalListPriceUndefined = 0;
	
	public Bundle() {
		offers = new HashMap<Book, List<Offer>>();
		selectedOffers = new HashMap<Book, Offer>();
		bookNeedUpdates = new HashMap<Book, Boolean>();
	}

	// === Add, remove, and get books ===

	public synchronized void addBook(Book book) {
		if (!contains(book)) {
			// New book -> empty offer list and needs update
			offers.put(book, new ArrayList<Offer>());
			bookNeedUpdates.put(book, true);
		}
		updatePrices();

		checkRep();
	}

	public void removeBook(Book book) {
		offers.remove(book);
		selectedOffers.remove(book);
		bookNeedUpdates.remove(book);
		updatePrices();
	}

	/**
	 * Returns the Books currently in this Bundle.
	 */
	public Set<Book> getBooks() {
		return offers.keySet();
	}
	
	public void clear() {
		offers.clear();
		selectedOffers.clear();
		bookNeedUpdates.clear();
		oldestOfferTimeStamp = -1;
		updatePrices();
	}

	/**
	 * Returns a Book in this Bundle whose ISBN matches the input ISBN, or null
	 * if no book matches.
	 */
	public Book getBookByIsbn(String isbn) {
		for (Book book : getBooks()) {
			if (book.getIsbn().equals(isbn)) {
				return book;
			}
		}
		return null;
	}

	// === Add and get offers ===

	/**
	 * REPLACES current set of offers with provided ones.
	 * 
	 * Used to set offers in bulk by BookstoreQuerys.
	 */
	public void setOffers(Book book, List<Offer> offers) {
		for (Offer offer : offers) {
		    addOffer(book, offer);
		}
	    bookNeedUpdates.put(book, false);
	}

	public synchronized void addOffer(Book book, Offer offer) {
		if (!contains(book)) {
			addBook(book);
		}
		List<Offer> bookOfferListing = offers.get(book);
		if (!bookOfferListing.contains(offer)) {
		    bookOfferListing.add(offer);
		}
	}
	
	public void clearOffers() {
		for (Book book : getBooks())
			clearOffers(book);
	}
	
	public void clearOffers(Book book) {
		if (contains(book))
			offers.get(book).clear();
		// TODO(Rodrigo): this is probably not a good solution when we already
		// have selected offers... need to think about that! For now, it doesn't
		// really matter since 'selecting' an offer makes no difference or sense
		// for the user.
	}

	public List<Offer> getBookOffers(Book book) {
		return offers.get(book);
	}

	// === Set and get selected offers ===

	public void setSelectedOffer(Book book, Offer offer) {
		if (!contains(book))
			addBook(book);
		if (offer != null && !offers.get(book).contains(offer))
			offers.get(book).add(offer);
		if (selectedOffers.containsKey(book)) {
			totalBundlePrice -= selectedOffers.get(book).getTotalPrice();
		}
		if (offer != null) {
			if (totalBundlePrice == -1) {
				totalBundlePrice = offer.getTotalPrice();
			} else {
				totalBundlePrice += offer.getTotalPrice();
			}
			selectedOffers.put(book, offer);
		}
	}

	/**
	 * Returns the selected offer for the given book, if already selected. Else,
	 * returns null.
	 * 
	 * The server applies the optimization algorithm to choose the best offer
	 * given the (filter) constraints.
	 */
	public Offer getSelectedOffer(Book book) {
		return selectedOffers.get(book);
	}

	// === Set and get needsUpdate info ===

	/**
	 * Marks all the books whether they need to update their offers.
	 */
	public void setAllNeedsUpdate(boolean needsUpdate) {
		for (Book book : getBooks()) {
			setNeedsUpdate(book, needsUpdate);
		}
	}

	public void setNeedsUpdate(Book book, boolean needsUpdate) {
		if (!contains(book))
			addBook(book);
		bookNeedUpdates.put(book, needsUpdate);
	}
	
	/**
	 * Returns true if the Book needs its offers to be updated for some reason.
	 * For example, the offers might be too old.
	 */
	public boolean needsUpdate(Book book) {
		// TODO(Rodrigo): add logic for old offers
		if (bookNeedUpdates.containsKey(book)) { 
			return bookNeedUpdates.get(book);
		} else {
			return true; // TODO(rodrigo): should we return true or false?
		}
	}

	/**
	 * Returns only the books whose offers need to be updated.
	 */
	public Set<Book> getBooksThatNeedUpdates() {
		Set<Book> books = new HashSet<Book>();
		for (Book book : getBooks()) {
			if (bookNeedUpdates.get(book))
				books.add(book);
		}
		return books;
	}

/**
 * 	Returns all the selected offers of the bundle.
 */
	public Set<Offer> getSelectedOffers() {
	    Set<Offer> offers = new HashSet<Offer>();
	    for (Book book : getBooks()) {
	        offers.add(getSelectedOffer(book));
	    }
	    return offers;
	}
	
	// === Convenience methods ===

	public Offer getCheapestOffer(Book book) {
		Offer cheapestOffer = null;
		if (contains(book)) {
			for (Offer offer : offers.get(book))
				if (cheapestOffer == null || offer.compareTo(cheapestOffer) < 0)
					cheapestOffer = offer;
		}
		return cheapestOffer;
	}

	public List<Offer> getOffersSortedByPrice(Book book) {
		List<Offer> sorted = offers.get(book);
		if (sorted != null) {
			Collections.sort(sorted);
		}
		return sorted;
	}

	public List<Offer> getOffersSortedByStore(Book book) {
		// TODO(rodrigo)
		return new ArrayList<Offer>();
	}

	// === Other simple methods ===

	/**
	 * Returns a list of all the books' ISBNs separated by comma.
	 */
	public String getIsbnListOfAllBooks() {
	    return getIsbnList(getBooks());
	    
	}
	
	public String getIsbnOfBooksThatNeedToBeUpdated() {
	    return getIsbnList(getBooksThatNeedUpdates());
	}
	
	private String getIsbnList(Set<Book> books) {    
		StringBuilder sb = new StringBuilder();
		for (Book book : books) {
			sb.append(book.getIsbn());
			sb.append(",");
		}
		return sb.toString();
	}

	public long getOldestOfferTimeStamp() {
		return oldestOfferTimeStamp;
	}

	public void setOldestOfferTimeStamp(long oldestOfferTimeStamp) {
		this.oldestOfferTimeStamp = oldestOfferTimeStamp;
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	private void updatePrices() {
		if (isEmpty()) {
			totalListPrice = -1;
			totalBundlePrice = -1;
			totalListPriceUndefined = 0;
		} else {
			totalListPrice = 0;
			totalBundlePrice = 0;
			totalListPriceUndefined = 0;
			for (Book book : getBooks()) {
				// TODO(rodrigo): add note or something if a book doesn't
				// have an available list price!
				if(totalListPriceUndefined!=1)
					totalListPriceUndefined = book.getListPrice()<0 ? 1 : -1;
				totalListPrice += book.getListPrice();
				if (selectedOffers.get(book) != null)
					totalBundlePrice += selectedOffers.get(book).getTotalPrice();
			}
		}
	}

	private boolean contains(Book book) {
		return getBooks().contains(book);
	}
	
	public boolean containsBook(String isbn) {
		for (Book book : getBooks()) {
			if (book.getIsbn().equals(isbn))
				return true;
		}
		return false;
	}

	public boolean isEmpty() {
		return getBooks().isEmpty();
	}

	public int getTotalListPrice() {
		return Math.max(totalListPrice, 0);
	}

	public int getTotalBundlePrice() {
		return Math.max(totalBundlePrice, 0);
	}
	
	public int isTotalListPriceUndefined(){
		return totalListPriceUndefined;
	}

	private void checkRep() {
		// All maps should have the same Books

		// TODO(rodrigo): implement and use this
	}

}