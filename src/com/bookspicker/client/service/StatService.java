package com.bookspicker.client.service;

import java.util.List;

import com.bookspicker.shared.Offer;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("stats")
public interface StatService extends RemoteService {
	
	public void logBuyClick(String isbn, Offer clickedOffer, List<Offer> competingOffers);

}
