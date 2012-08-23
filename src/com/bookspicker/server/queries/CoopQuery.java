package com.bookspicker.server.queries;

import java.util.List;

import com.bookspicker.server.data.CoopOfferManager;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.Bundle;
import com.bookspicker.shared.CoopOffer;

public class CoopQuery implements BookstoreQuery {

	@Override
	public void getBooksOffers(Bundle bundle) {
		
		CoopOfferManager com = CoopOfferManager.getManager();
		
		List<CoopOffer> offers;
		for (Book book : bundle.getBooksThatNeedUpdates()) {
			offers = com.getLocalOffersFor(book);
			for (CoopOffer offer : offers) {
				bundle.addOffer(book, offer);
			}
		}
	}
}
