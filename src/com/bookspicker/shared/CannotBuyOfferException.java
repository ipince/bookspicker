package com.bookspicker.shared;

@SuppressWarnings("serial")
public class CannotBuyOfferException extends Exception {
	
	public CannotBuyOfferException() {
		super();
	}
	
	public CannotBuyOfferException(String msg) {
		super(msg);
	}

}
