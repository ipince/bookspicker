package com.bookspicker.client.service;

import com.bookspicker.shared.AuthenticationException;
import com.bookspicker.shared.CannotBuyOfferException;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.Pair;
import com.bookspicker.shared.User;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("local_offer")
public interface LocalOfferService extends RemoteService {
	
	User saveOffer(LocalOffer offer) throws AuthenticationException;
	
	User postOffer(long offerId, boolean post) throws AuthenticationException;
	User deleteOffer(long offerId) throws AuthenticationException;
	
	Pair<LocalOffer, User> getOffer(long offerId);
	void buyOffer(long offerId, int price) throws AuthenticationException, CannotBuyOfferException;
}
