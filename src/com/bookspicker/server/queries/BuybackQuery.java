package com.bookspicker.server.queries;

import java.util.ArrayList;
import java.util.List;

import com.bookspicker.shared.Book;
import com.bookspicker.shared.BuybackOffer;
import com.bookspicker.shared.Offer;
import com.bookspicker.shared.Offer.StoreName;

public class BuybackQuery {
	
	public static List<Offer> getBuybackOffers(Book book) {
		// TODO: implement this! This is just a dummy implementation!
		
		List<Offer> offers = new ArrayList<Offer>();
		offers.add(new BuybackOffer(1000, 200, StoreName.AMAZON));
		offers.add(new BuybackOffer(800, 100, StoreName.AMAZON));
		
		return offers;
	}

}
