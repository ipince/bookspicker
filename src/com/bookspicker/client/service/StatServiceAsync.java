package com.bookspicker.client.service;

import java.util.List;

import com.bookspicker.shared.Offer;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface StatServiceAsync {

	void logBuyClick(String isbn, Offer clickedOffer,
			List<Offer> competingOffers, AsyncCallback<Void> callback);

}
