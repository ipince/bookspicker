package com.bookspicker.shared;

public class CoopOffer extends Offer {
	
	private Long id;
	private String isbn;
	private int price;
	
	public CoopOffer() {
		setStoreName(StoreName.THE_COOP);
		setSellerName("The COOP");
	}
	
	public CoopOffer(String isbn, String condition, int price, String url) {
		this.isbn = isbn;
		this.price = price;
		setCondition(condition);
		setUrl(url);
		setStoreName(StoreName.THE_COOP);
		setSellerName("The COOP");
	}

	@Override
	public int getPrice() {
		return price;
	}
	@Override
	public int getShipping() {
		return 0;
	}

}
