package com.bookspicker.client.event;

import com.bookspicker.shared.Book;
import com.bookspicker.shared.Offer;

public class Analytics {

	public static native void trackEvent(String category, String action, String label) /*-{
		if($wnd._gaq) {
	    	$wnd._gaq.push(['_trackEvent', category, action, label]);
		}
	}-*/;
	
	public static native void trackEvent(String category, String action, String label, int intArg) /*-{
		if($wnd._gaq) {
	    	$wnd._gaq.push(['_trackEvent', category, action, label, intArg]);
		}
	}-*/;
	
	public static native void trackPageview(String url) /*-{
		if($wnd._gaq) {
	    	$wnd._gaq.push(['_trackPageview', url]);
		}
	}-*/;
	
	public static void trackSearchFromURL(String tokenString) {
		Analytics.trackEvent("search", "searchFromURL", tokenString);
	}
	
	public static void trackSearchFromSearchPage(String query) {
		Analytics.trackEvent("search", "searchFromBox", query);
	}
	
	public static void trackBuyAction(Offer offer, Book book) {
		Analytics.trackEvent("buy", "buyOffer", offer.toString() + " ISBN: " + book.getIsbn());
	}
	
	public static void trackShowOffersButton(Book book) {
		Analytics.trackEvent("manageOffers", "showOffers", "Book: "+book.getIsbn());
		
	}

	public static void trackExpandOffersButton(int size, Book book) {
		Analytics.trackEvent("manageOffers", "expandOffers", "Offers Size: "+size+" Book: "+book.getIsbn());
		
	}

	public static void trackCollapsedOffersButton(int size, Book book) {
		Analytics.trackEvent("manageOffers", "collapseOffers", "Offers Size: "+size+" Book: "+book.getIsbn());
	}
}
