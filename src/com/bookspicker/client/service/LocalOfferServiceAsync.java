package com.bookspicker.client.service;

import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.Pair;
import com.bookspicker.shared.User;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LocalOfferServiceAsync {

	void saveOffer(LocalOffer offer, AsyncCallback<User> callback);

	void postOffer(long offerId, boolean post, AsyncCallback<User> callback);
	void deleteOffer(long offerId, AsyncCallback<User> callback);

	void getOffer(long offerId, AsyncCallback<Pair<LocalOffer, User>> callback);
	void buyOffer(long offerId, int price, AsyncCallback<Void> callback);

}
