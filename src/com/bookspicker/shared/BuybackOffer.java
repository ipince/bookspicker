package com.bookspicker.shared;

public class BuybackOffer extends Offer {
	
	private int price;
	private int shipping;
	
	public BuybackOffer(int price, int shipping, StoreName storeName) {
		this.price = price;
		this.shipping = shipping;
		setStoreName(storeName);
	}

	@Override
	public int getPrice() {
		return price;
	}

	@Override
	public int getShipping() {
		return shipping;
	}

}
