package com.bookspicker.server.bundling;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.bookspicker.server.data.StatManager;
import com.bookspicker.server.services.HelperThreads;
import com.bookspicker.server.social.SocialGraph;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.Bundle;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.Location;
import com.bookspicker.shared.Offer;
import com.bookspicker.shared.Stat;
import com.bookspicker.shared.User;

public class Optimizer {
	
	public static void optimize(Bundle bundle, Filter filter, User buyer) {
		optimize(bundle, filter, buyer, "", false);
	}

	public static void optimize(Bundle bundle, Filter filter,
			final User buyer, final String ip, final boolean persist) {

		// TODO(rodrigo): what if two offers are exactly
		// the same price?
		
		if (filter == null || !filter.isMeaningful()) {
			
			// no filters, simply do naive optimization
			List<Offer> offers;
			for (Book book : bundle.getBooks()) {
				
				// Sort the offers
				offers = bundle.getBookOffers(book);
				Collections.sort(offers, new Comparator<Offer>() {
					@Override
					public int compare(Offer offer1, Offer offer2) {
						if (offer1.compareTo(offer2) != 0) {
							return offer1.compareTo(offer2);
						} else if (buyer != null) {
							if (offer1 instanceof LocalOffer &&
									offer2 instanceof LocalOffer) {
								LocalOffer lo1 = (LocalOffer) offer1;
								LocalOffer lo2 = (LocalOffer) offer2;
								
								// Location stuff
								int locComparison = 0;
								if (buyer.getLocation() != null) {
									if (lo1.getLocation() != null) {
										if (lo2.getLocation() != null) {
											int buyerToOffer1 = Location.getDistance(buyer.getLocation(), lo1.getLocation());
											int buyerToOffer2 = Location.getDistance(buyer.getLocation(), lo2.getLocation());
											if (buyerToOffer1 != buyerToOffer2) { // we have a winner!
												locComparison = buyerToOffer1 - buyerToOffer2;
											}
										} else {
											locComparison = -1; // Offer 1 wins
										}
									} else {
										if (lo2.getLocation() != null) {
											locComparison = 1; // Offer 2 wins
										}
									}
								}
								if (locComparison != 0) {
									if (persist) {
										saveStat(Stat.newUsedLocationStat(lo1.getId().toString() + "," + lo2.getId(), buyer.getId().toString(), ip));
									}
									return locComparison;
								}
								
								// If we're here, it means location didn't
								// resolve ordering -> check social distance
								double buyerToSeller1 = SocialGraph.getSocialDistance(buyer.getFib(), lo1.getOwner().getFib());
								double buyerToSeller2 = SocialGraph.getSocialDistance(buyer.getFib(), lo2.getOwner().getFib());
								
								int socialComparison = 0;
								if (buyerToSeller1 != buyerToSeller2) {
									if (buyerToSeller1 > buyerToSeller2)
										socialComparison = 1;
									else
										socialComparison = -1;
								}
								if (socialComparison != 0) {
									if (persist) {
										saveStat(Stat.newUsedSocialStat(lo1.getId().toString() + "," + lo2.getId(), buyer.getId().toString(), ip));
									}
									return socialComparison;
								}
								
								// Social distance didn't solve it either
								// -> use creation date
								if (persist) {
									saveStat(Stat.newUsedCreationDateStat(lo1.getId().toString() + "," + lo2.getId(), buyer.getId().toString(), ip));
								}
								return lo1.getCreationDate().compareTo(lo2.getCreationDate());
							}
						}
						return 0;
					}
				});
				
				// Pick the selected one
				Offer tempBestOffer = null;
				for (Offer offer : bundle.getBookOffers(book)) {
					if (tempBestOffer == null) {
						tempBestOffer = offer;
					} else if (tempBestOffer.getTotalPrice() > offer.getTotalPrice() ||
							offer.isChosenOffer()) {
						tempBestOffer = offer;
					}
				}
				if (tempBestOffer == null) {
					// TODO (rodrigo): log error!!
				}
				bundle.setSelectedOffer(book, tempBestOffer);
			}
		} else {

			// no filters, simply do naive optimization
			for (Book book : bundle.getBooks()) {
				Offer tempBestOffer = null;
				
				for (Offer offer : bundle.getBookOffers(book)) {
					if (tempBestOffer == null && !filter.getDisabledStores().contains(offer.getStoreName())) {
						tempBestOffer = offer;
					} else if (!filter.getDisabledStores().contains(offer.getStoreName()) &&
							tempBestOffer.getTotalPrice() > offer.getTotalPrice()) {
						tempBestOffer = offer;
					}
				}
				if (tempBestOffer == null) {
					// TODO (rodrigo): log error!!
				}
				bundle.setSelectedOffer(book, tempBestOffer);
			}
		}
	}
	
	private static void saveStat(final Stat stat) {
		HelperThreads.execute(new Runnable() {
			@Override
			public void run() {
				StatManager.getManager().save(stat);
			}
		});
	}
}
