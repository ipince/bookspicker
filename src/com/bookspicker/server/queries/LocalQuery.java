package com.bookspicker.server.queries;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.bookspicker.Log4JInitServlet;
import com.bookspicker.server.data.LocalOfferManager;
import com.bookspicker.server.data.StatManager;
import com.bookspicker.server.services.HelperThreads;
import com.bookspicker.server.social.SocialGraph;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.Bundle;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.Offer;
import com.bookspicker.shared.School;
import com.bookspicker.shared.Stat;
import com.bookspicker.shared.User;

public class LocalQuery {
	
	private static Logger logger = Log4JInitServlet.logger;

	public void getBooksOffers(School school, Bundle bundle, User buyer, String ip) {
		
		List<Offer> competingOffers;
		List<Offer> buyBackOffers;
		for (Book book : bundle.getBooks()) {
			
			// Find local offers for this book ISBN
			List<LocalOffer> offers = getLocalOffersFor(school, book);
			
			// Get its competing offers (sale)
			competingOffers = bundle.getBookOffers(book);
			
			// Get current best buy-back offer (to sell)
			buyBackOffers = BuybackQuery.getBuybackOffers(book);
			
			List<LocalOffer> toAdd = new ArrayList<LocalOffer>();
			for (final LocalOffer offer : offers) {
				
				// Skip inactive offers
				if (!offer.isActive()) {
					continue;
				}
				
				boolean willAdd = false;
				if (offer.isAutoPricing()) {
					offer.calculatePrice(competingOffers, buyBackOffers);
					if (offer.getAutoPrice() >= 0) {
						toAdd.add(offer);
//						bundle.addOffer(book, offer);
						willAdd = true;
					}
				} else { // always add fixed price offers
//					bundle.addOffer(book, offer);
					toAdd.add(offer);
					willAdd = true;
				}
				
				if (willAdd) {
					offer.increaseNumTimesShowed();
				} else {
					// Also log the fact that it wasn't shown!!
					offer.increaseNumTimesNotShowed();
				}
				
				// Log the showing
				HelperThreads.execute(new Runnable() {
					@Override
					public void run() {
						LocalOfferManager.getManager().update(offer);
					}
				});
			}
			
			// Add the local offers to the bundle
			for (LocalOffer offer : toAdd) {
				// Personalize if possible
				if (buyer != null) {
					double sd = SocialGraph.getSocialDistance(buyer.getFib(), offer.getOwner().getFib());
					boolean changeName = false;
					String name = "";
					if (sd <= 1) {
						name = "A friend";
						changeName = true;
					} else if (sd <= 2) {
						name = "A friend's friend";
						changeName = true;
					}
					
					if (changeName) {
						offer.setSellerName(name);
						final Stat stat = Stat.newPersonalizedNameStat(name, offer.getId().toString(), buyer.getId().toString(), ip);
						HelperThreads.execute(new Runnable() {
							@Override
							public void run() {
								StatManager.getManager().save(stat);
							}
						});
					}
				}
				
				// Add to bundle
				bundle.addOffer(book, offer);
			}
			
		}
	}
	
	private List<LocalOffer> getLocalOffersFor(School school, Book book) {
		return LocalOfferManager.getManager().getLocalOffersFor(school, book);
	}

//	/**
//	 * @return the lowest total price in the bundle for the given
//	 * book, or -1 if there are no offers available
//	 */
//	public static int getLowestBuyingPriceFor(Bundle bundle, Book book) {
//		if (bundle == null)
//			logger.warn("LocalQuery - Bundle is null!");
//		List<Offer> onlineOffers = bundle.getOffersSortedByPrice(book);
//		if (!onlineOffers.isEmpty())
//			return onlineOffers.get(0).getTotalPrice();
//		return -1;
//	}

}
