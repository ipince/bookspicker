package com.bookspicker.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Interface for an Offer.
 */
public abstract class Offer implements IsSerializable, Comparable<Offer> { 
	
	public enum StoreName {
		LOCAL("BooksPicker", true),
		AMAZON("Amazon.com", false), 
		AMAZON_MARKETPLACE("Amazon.com (Market Place)", false),
		HALF ("Half.com", false),
		ALIBRIS("Alibris.com", false), 
		ABE_BOOKS("AbeBooks.com", false),
		BOOK_EX("APO Book Exchange", true),
		THE_COOP("The COOP", true);

		String name;
		boolean isLocal;

		StoreName(String name, boolean _isLocal) {
			this.name = name;
			this.isLocal = _isLocal;
			
		}

		public String getName() {
			return name;
		}
		
		public boolean isLocal() {
			return isLocal;
		}
	}
	
	// Fields required for all types of offers
	private StoreName storeName;
	private String sellerName;
	private String condition;
	private long timeStamp;
	private String url;
	private boolean chosenOffer = false;
	private boolean internationalEdition = false;
	
	// Methods common across all offers (hence NOT abstract)
	// Mainly simple getters for all required fields, and one
	// setter for chosenOffer
	public void setStoreName(StoreName storeName) {
		this.storeName = storeName;
	}
	public StoreName getStoreName() {
		return storeName;
	}
	public void setSellerName(String sellerName) {
		this.sellerName = sellerName;
	}
	public String getSellerName() {
		return sellerName;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	/**
	 * The book's condition. Condition cannot be null.
	 */
	public String getCondition() {
		if (condition == null)
			return "";
		return condition;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUrl() {
		return url;
	}
	public void setChosenOffer(boolean chosen) {
		this.chosenOffer = chosen;
	}
	public boolean isChosenOffer() {
		return chosenOffer;
	}
	public int getTotalPrice() {
		return getPrice() + getShipping();
	}
	public void setInternationalEdition(boolean internationalEdition) {
		this.internationalEdition = internationalEdition;
	}
	public boolean isInternationalEdition() {
		return internationalEdition;
	}
	
	/**
	 * Determines whether the book for this offer is new or used.
	 * Note that this is a very rudimentary implementation and
	 * may not be 100% accurate.
	 * 
	 * @return true iff the book is new.
	 */
	public boolean isNew() {
		String cond = getCondition().toLowerCase();
		if (cond.equals("new") ||
				cond.equals("brandnew") ||
				cond.equals("brand new"))
			return true;
		else if (cond.contains("new") && !cond.contains("like"))
			return true;
		return false;
	}
	
	// Abstract methods
	public abstract int getPrice();
	public abstract int getShipping();

	// TODO(jonathan): document this
	@Override
	public int compareTo(Offer o) {
		if(o == null){
			throw new NullPointerException();
		}
		if (!(o instanceof Offer)){
			throw new ClassCastException();
		}
		double myTotalPrice = this.getTotalPrice();
		double theirTotalPrice = o.getTotalPrice();

		if(myTotalPrice > theirTotalPrice){
			return 1;
		}
		else if(myTotalPrice < theirTotalPrice){
			return -1;
		}
		else {
			return 0;
		}

	}
	
	@Override
	public String toString() {
		return "Store: " + this.storeName + 
				" Seller: " + this.sellerName + 
				" Price: " + this.getTotalPrice();
	}

}
