package com.bookspicker.shared;


/**
 * Contains information regarding the seller and the specific
 * information for this book offer.
 *  
 * @author Jonathan
 */
public class OnlineOffer extends Offer {

	//TODO (Jonathan) - Add support for Ebooks  
	private int price; // in pennies
	private int shipping; // in pennies

	// For serialization purposes.
	public OnlineOffer(){
		super();
	}

	public OnlineOffer(int price, int shipping, StoreName storeName,
			String sellerName, String condition, String url) {
		super();
		this.price = price;
		this.shipping = shipping;
		setStoreName(storeName);
		setSellerName(sellerName);
		setCondition(condition);
		setUrl(url);
	}

	public OnlineOffer(int price, int shippingPrice, StoreName storeName,
			String seller, String condition, String url, boolean international) {
		this(price, shippingPrice, storeName, seller, 
				international ? condition + " (International Edition)" : condition, url);
		setInternationalEdition(international);
	}

	public int getPrice() {
		return price;
	}

	public int getShipping() {
		return shipping;
	}
	
	@Override
	public boolean isInternationalEdition() {
		// Quick and dirty 'hack' to catch some other offers
		// we didn't catch explicitly.
		return super.isInternationalEdition() || 
			 getCondition().toLowerCase().contains("international");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getCondition() == null) ? 0 : getCondition().hashCode());
		result = prime * result + (price);
		result = prime * result
		+ ((getSellerName() == null) ? 0 : getSellerName().hashCode());
		result = prime * result + shipping;
		result = prime * result + ((getStoreName() == null) ? 0 : getStoreName().hashCode());
		result = prime * result + ((getUrl() == null) ? 0 : getUrl().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OnlineOffer other = (OnlineOffer) obj;
		if (getCondition() == null) {
			if (other.getCondition() != null)
				return false;
		} else if (!getCondition().equals(other.getCondition()))
			return false;
		if (price != other.price)
			return false;
		if (getSellerName() == null) {
			if (other.getSellerName() != null)
				return false;
		} else if (!getSellerName().equals(other.getSellerName()))
			return false;
		if (shipping != other.shipping)
			return false;
		if (getStoreName() == null) {
			if (other.getStoreName() != null)
				return false;
		} else if (!getStoreName().equals(other.getStoreName()))
			return false;
		return true;
	}

}
