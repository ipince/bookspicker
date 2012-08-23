package com.bookspicker.shared;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.bookspicker.client.HistoryToken;
import com.google.gwt.core.client.GWT;

public class LocalOffer extends Offer {
	
	private static final int DEFAULT_EXPIRATION_DAYS = 5;
	
	public enum Strategy {
		AGGRESSIVE, CONSERVATIVE;
		
		/**
		 * @return multiplier, in percentage points
		 */
		public int getBaseMultiplier() {
			switch (this) {
			case AGGRESSIVE: return 90;
			case CONSERVATIVE: return 100;
			}
			return 100;
		}
		
		public String getDisplayName() {
			switch (this) {
			case AGGRESSIVE: return "Quickie Sale";
			case CONSERVATIVE: return "Money Lover";
			}
			return null;
		}
		
		public String getDescription() {
			switch (this) {
			case AGGRESSIVE: return "Sets the price to be highly competitive within the market, making your book sell fast.";
			case CONSERVATIVE: return "Sets the price competitively but not as much. It may take longer to sell but you'll get more cash.";
			}
			return null;
		}
	}
	
	// Offer details
	private Long id;
	private User owner; // required
	private Book book; // required
	private School school; // required
	private String classCode; // optional
	private Condition bookCondition; // required
	private Location location; // optional
	private Integer fixedPrice; // required unless autoPricing=true
	private boolean autoPricing = true; // required
	private Strategy strategy; // required if autoPricing=true
	private Integer lowerBoundPrice; // optional
	private String comments; // optional
	
	// Meta-data (field access only)
	private Date creationDate; // required
	
	private Boolean active; // required (must be false if sold=true)
	private Date lastPostingDate; // required
	
	private Boolean sold; // required
	@SuppressWarnings("unused")
	public Boolean soldOnce; // required
	@SuppressWarnings("unused")
	private Long buyerId; // optional if sold=true
	private String buyerEmail; // required iff sold=true
	private Integer sellingPrice; // required iff sold=true
	@SuppressWarnings("unused")
	public Date timeSold; // required iff sold=true
	
	public Long timeOnMarketFixed; // required
	public Long timeOnMarketAggressive; // required
	public Long timeOnMarketConservative; // required
	public Integer numTimesShown; // required
	private Integer numTimesNotShown; // required
	private Boolean deleted = false; // required
	
	// Transient stuff (not persisted)
	private int currentAutoPrice;
	private boolean autoPriceCalculated = false; // starts off as false
	
	// Constructor
	public LocalOffer() { // for serialization
		setDefaults();
	}
	
	public LocalOffer(User owner, Book book,
			Condition bookCondition, boolean autoPricing) {
		this();
		this.owner = owner;
		this.book = book;
		this.bookCondition = bookCondition;
		this.autoPricing = autoPricing;
		resetMetadata();
	}
	
	private void setDefaults() {
		setStoreName(StoreName.LOCAL);
		setSellerName("MIT Student");
		resetMetadata();
	}
	
	public void resetMetadata() {
		Date now = new Date();
		creationDate = now;
		active = true;
		lastPostingDate = now;
		sold = false;
		soldOnce = false;
		timeOnMarketFixed = 0L;
		timeOnMarketAggressive = 0L;
		timeOnMarketConservative = 0L;
		numTimesShown = 0;
		numTimesNotShown = 0;
	}
	
	// Getters and Setters for all fields
	public void setId(Long id) {
		this.id = id;
	}
	public Long getId() {
		return id;
	}
	public void setOwner(User owner) {
		// Rodrigo: Maybe we should call owner.addLocalOffer(this),
		// but I'm not sure if it's necessary or not. If you decide
		// to make this change, make sure the constructor uses
		// this setter instead of assigning the owner directly!
		this.owner = owner;
		//owner.addLocalOffer(this);
	}
	public User getOwner() {
		return owner;
	}
	public void setBook(Book book) {
		this.book = book;
	}
	public Book getBook() {
		return book;
	}
	public void setSchool(School school) {
		this.school = school;
	}
	public School getSchool() {
		return school;
	}
	public void setClassCode(String classCode) {
		this.classCode = classCode;
	}
	public String getClassCode() {
		return classCode;
	}
	public void setBookCondition(Condition bookCondition) {
		this.bookCondition = bookCondition;
	}
	public Condition getBookCondition() {
		return bookCondition;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	public Location getLocation() {
		return location;
	}
	public void setFixedPrice(Integer fixedPrice) {
		this.fixedPrice = fixedPrice;
	}
	public Integer getFixedPrice() {
		return fixedPrice;
	}
	public void setAutoPricing(Boolean autoPricing) {
		boolean previouslyActive = active;
		deactivate();
		this.autoPricing = autoPricing;
		if (previouslyActive)
			activate();
	}
	public Boolean isAutoPricing() {
		return autoPricing;
	}
	public void setStrategy(Strategy strategy) {
		boolean previouslyActive = active;
		deactivate();
		this.strategy = strategy;
		if (previouslyActive)
			activate();
	}
	public Strategy getStrategy() {
		return strategy;
	}
	public void setLowerBoundPrice(Integer lowerBoundPrice) {
		this.lowerBoundPrice = lowerBoundPrice;
	}
	public Integer getLowerBoundPrice() {
		return lowerBoundPrice;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getComments() {
		return comments;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	public Date getLastPostingDate() {
		return lastPostingDate;
	}
	public Boolean isActive() {
		return active;
	}
	public Boolean isSold() {
		return sold;
	}
	public Integer getSellingPrice() {
		return sellingPrice;
	}
	public boolean isDeleted() {
		return deleted;
	}
	
	// Methods that need to be implemented for Offer
	
	@Override
	public int getPrice() {
		if (autoPricing) {
			return getAutoPrice();
		} else {
			return fixedPrice;
		}
	}
	public int getAutoPrice() {
		if (autoPriceCalculated) {
			return currentAutoPrice;
		} else {
			// TODO: what to do?
			System.err.println("Warning! Tried to access autoPrice before it was calculated!");
			return -1;
		}
	}

	@Override
	public int getShipping() {
		// No shipping for local offers!
		return 0;
	}
	
	@Override
	public String getCondition() {
		if (bookCondition != null)
			return bookCondition.getDisplayName();
		else
			return "Unknown";
	}
	
	@Override
	public String getUrl() {
		StringBuilder sb = new StringBuilder();
		if (GWT.isClient()) {
			sb.append(GWT.getHostPageBaseURL());
		}
		sb = sb.append("#");
		sb = sb.append(HistoryToken.BUY_LOCAL);
		sb = sb.append(HistoryToken.PARAM_STARTER);
		sb = sb.append(HistoryToken.PARAM_OFFER_ID);
		sb = sb.append("=").append(getId());
		return sb.toString();
	}
	
	// Utility methods
	
	/**
	 * Copies over details from the given offer, but NOT
	 * any metadata details.
	 */
	public void copy(LocalOffer newOffer) {
		// Book
		setBook(newOffer.getBook());
		
		// School
		setSchool(newOffer.getSchool());
		
		// Class
		setClassCode(newOffer.getClassCode());
		
		// Condition
		setBookCondition(newOffer.getBookCondition());
		
		// Pricing
		setAutoPricing(newOffer.isAutoPricing());
		setStrategy(newOffer.getStrategy());
		setLowerBoundPrice(newOffer.getLowerBoundPrice());
		setFixedPrice(newOffer.getFixedPrice());
		
		// Location
		setLocation(newOffer.getLocation());
		
		// Comments
		setComments(newOffer.getComments());
	}

	public void calculatePrice(List<Offer> competingOffers, List<Offer> buyBackOffers) {
		
		int reference = -1;
		int highestBuyback = -1;
		
		// Make the reference be the second non-international price
		// If there aren't two, then make it the first
		Collections.sort(competingOffers);
		boolean firstSeen = false;
		for (int i = 0; i < competingOffers.size(); i++) {
			if (!competingOffers.get(i).isInternationalEdition()) {
				if (!firstSeen) { // first one
					reference = competingOffers.get(i).getTotalPrice();
					firstSeen = true;
				} else { // second!
					reference = competingOffers.get(i).getTotalPrice();
					break;
				}
			}
		}
		
		System.out.println("Calculating auto price. Reference price is " + reference + " and high buyback is " + highestBuyback);
		
		// Check that reference > 0 and highestBuyback is non-negative
		if (reference <= 0) {
			currentAutoPrice = -1;
			autoPriceCalculated = false;
			return;
		}
		if (highestBuyback < 0) {
			// No one wants to buy this book, so set it to 0
			highestBuyback = 0;
		}
		
		// Max should be higher than min. Otherwise, it means there's
		// an arbitrage opportunity -> buy book at max price and 
		// sell to stores at min price. This is extremely unlikely
		// to happen, but if it does, we should make the price be
		// max and maybe notify the seller that he's better off
		// selling his book to a store instead of locally.
//		if (max < min) {
//			currentAutoPrice = max;
//			autoPriceCalculated = true;
//			// TODO: Notify seller?
//			return;
//		}
		
		if (strategy != null && bookCondition != null) {
			int mult = strategy.getBaseMultiplier() + bookCondition.getMultiplierOffset();
			double tmp = reference * (mult / 100.0);
			// round to nearest 10 cents
			int price = (int) Math.round(tmp / 10) * 10;
			currentAutoPrice = Math.max(price, 0); // ensure > 0
		} else {
			// Not enough information to calculate the price
			currentAutoPrice = -1;
			autoPriceCalculated = false;
			return;
		}
		
		if (lowerBoundPrice != null) {
			if (currentAutoPrice < lowerBoundPrice)
				currentAutoPrice = lowerBoundPrice;
		}
		autoPriceCalculated = true;
	}

	/**
	 * Returns true if the offer has been active for longer
	 * than 5 days. If this returns true, the offer should be
	 * de-activated.
	 */
	public boolean shouldExpire() {
		return shouldExpire(DEFAULT_EXPIRATION_DAYS);
	}
	
	public boolean shouldExpire(int expirationDays) {
		Date now = new Date();
		return (now.getTime() - lastPostingDate.getTime() > (1000 * 60 * 60 * 24 * expirationDays));
	}
	
	/**
	 * Sets this offer as expired. Should really only happen
	 * when the offer is currently active. If so, the offer will
	 * become inactive and all relevant metadata will be updated.
	 */
	public void deactivate() {
		if (!deleted && active) {
			active = false;
			// Update timeOnMarket
			Date now = new Date();
			if (isAutoPricing()) {
				if (strategy == Strategy.AGGRESSIVE) {
					timeOnMarketAggressive += (now.getTime() - lastPostingDate.getTime());
				} else if (strategy == Strategy.CONSERVATIVE) {
					timeOnMarketConservative += (now.getTime() - lastPostingDate.getTime());
				}
			} else {
				timeOnMarketFixed += (now.getTime() - lastPostingDate.getTime());
			}
		}
	}
	
	/**
	 * Makes this offer live. Only does something if the offer
	 * is previously inactive.
	 */
	public void activate() {
		if (!deleted && !active) {
			active = true;
			sold = false;
			// Reset relevant metadata
			lastPostingDate = new Date();
		}
	}
	
	public void delete() {
		if (!deleted) {
			if (active)
				deactivate();
			deleted = true;
		}
	}
	
	public void increaseNumTimesShowed() {
		if (!deleted && active) {
			numTimesShown++;
		}
	}
	
	public void increaseNumTimesNotShowed() {
		if (!deleted && active) {
			numTimesNotShown++;
		}
	}
	
	public synchronized void buy(String buyerEmail, int price) {
		buy(buyerEmail, -1, price);
	}
	
	/**
	 * Offer must be unsold and active in order for it to
	 * be eligible to be sold.
	 * 
	 * @param buyerEmail must be a valid email address
	 * @param buyerId
	 * @param price
	 */
	public synchronized void buy(String buyerEmail, long buyerId, int price) {
		if (!deleted && !sold && active) {
			// First, deactivate
			deactivate();

			this.buyerEmail = buyerEmail;
			if (buyerId > 0)
				this.buyerId = buyerId;
			sellingPrice = price;

			sold = true;
			timeSold = new Date();
			soldOnce = true;
		}
	}
	
	public String getStatus() {
		if (deleted)
			return "Deleted";
		if (sold)
			return "Sold";
		if (active)
			return "Active";
		else
			return "Inactive";
	}
	
	/**
	 * Determines whether the offer has all required fields
	 * set. If strict=true, returns true iff ALL required fields
	 * are set. Else, only checks for fields that are generated
	 * by the user.
	 */
	public boolean isComplete(boolean strict) {
		boolean complete =
			book != null &&
			bookCondition != null &&
			(autoPricing || fixedPrice != null);
		if (strict) {
			complete = complete &&
				owner != null &&
				creationDate != null &&
				active != null &&
				lastPostingDate != null &&
				sold != null &&
				sellingPrice != null &&
				timeOnMarketFixed != null &&
				numTimesShown != null;
		}
		return complete;
	}
	
	private void checkRep() {
		// Required fields must be set
		// TODO
		
		// If offer is SOLD, it CANNOT be active
		if (sold && active){
			throw new RuntimeException("LocalOffer rep invariant violated!");
		}
		
		// If offer is SOLD, there MUST be a sellingPrice
		// and a buyerEmail
		if (sold) {
			if (sellingPrice == null || buyerEmail == null) {
				throw new RuntimeException("LocalOffer rep invariant violated!");
			}
		}
	}

	/**
	 * Pre-established condition levels for local book sales.
	 * The levels are modeled after Amazon, and descriptions
	 * of what each condition means can be found here:
	 * 
	 * http://www.amazon.com/gp/help/customer/display.html?nodeId=1161242
	 */
	public enum Condition {
//		NEW,
		LIKE_NEW,
		VERY_GOOD,
		GOOD,
		ACCEPTABLE;
		
		public String getDisplayName() {
			switch (this) {
//			case NEW: return "New";
			case LIKE_NEW: return "Like New";
			case VERY_GOOD: return "Very Good";
			case GOOD: return "Good";
			case ACCEPTABLE: return "Acceptable";
			}
			return "Error"; // should never happen
		}
		
		public int getMultiplierOffset() {
			switch (this) {
//			case NEW: return 5;
			case LIKE_NEW: return -1;
			case VERY_GOOD: return -2;
			case GOOD: return -4;
			case ACCEPTABLE: return -6;
			}
			return 0;
		}
		
		/**
		 * Taken verbatim from Amazon's conditions guidelines.
		 */
		public String getDescription() {
			switch (this) {
//			case NEW:
//				return "Just like it sounds. A brand-new, unused, unread " +
//						"copy in perfect condition.";
			case LIKE_NEW:
				return "An apparently unread copy in perfect condition. " +
//						"Dust cover is intact, with no nicks or tears. " +
						"Spine has no signs of creasing. Pages are clean " +
						"and are not marred by notes or folds of any kind. " +
						"Book may contain a remainder mark on an outside " +
						"edge but this should be noted in the comments.";
			case VERY_GOOD:
				return "A copy that has been read, but remains in excellent " +
						"condition. Pages are intact and are not marred by " +
						"notes or highlighting. The spine remains undamaged.";
			case GOOD:
				return "A copy that has been read, but remains in clean " +
						"condition. All pages are intact, and the cover is " +
						"intact. The " +
						"spine may show signs of wear. Pages can include " +
						"limited notes and highlighting.";
			case ACCEPTABLE:
				return "A readable copy. All pages are intact, and the " +
						"cover is intact. " +
						"Pages can include considerable notes--in pen or " +
						"highlighter--but the notes cannot obscure the text.";
			}
			return "Error"; // should never happen
		}
	}
	
}
