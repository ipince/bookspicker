package com.bookspicker.server.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.bookspicker.Log4JInitServlet;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.Bundle;
import com.bookspicker.shared.Offer;

/**
 * Singleton that is used to cache all the book offers, offers become invalid after 15 minutes.
 * @author Jonathan
 *
 */
public class CachedOffers { 
    private static final CachedOffers instance = new CachedOffers();  
    private static Logger logger = Log4JInitServlet.logger;

    private static long MAX_CACHE_TIME = 1000*60*10;  // 10 minutes
    private Map<Book, List<Offer>> cachedOffers;
    private Map<Book, Long> offerTimes;

    private CachedOffers() {
        cachedOffers = new HashMap<Book, List<Offer>>();
        offerTimes = new HashMap<Book, Long>();
    }

    public static CachedOffers getInstance() {
        return instance;
    }
    
    
    public synchronized List<Offer> getOffers(Book book) {
    	System.out.println("contains? " + offerTimes.containsKey(book));
    	System.out.println("contains? " + cachedOffers.containsKey(book));
    	System.out.println("hashcode: " + book.hashCode());
        if (offerTimes.containsKey(book) && cachedOffers.containsKey(book) &&
                System.currentTimeMillis() - offerTimes.get(book) < MAX_CACHE_TIME) {
            logger.info("CachedOffers - Cache HIT: " + book.getTitle());
            return cachedOffers.get(book);
        } else {
            logger.info("CachedOffers - Cache MISS: " + book.getTitle());
            return null;
        }
    }
        
        
    private synchronized void setOffers(Book book, List<Offer> offers) {
    	if (book == null) {
    		logger.warn("CachedOffers - Tried to add offers for null book!!");
    		return;
    	}
    	offerTimes.put(book, System.currentTimeMillis());
		cachedOffers.put(book, offers);
    }

    /**
     * Updates the offers if they differ from the cached offers.
     * It only updates the different offers in order to keep track
     * of the caching time, since after MAX_CACHE_TIME the
     * offers will not be retrieved from the cache and if they are
     * different it will update them. (we may be wasting some 
     * network calls if the same book is being queried in intervals
     * smaller than 15 minutes and the results are the same, 
     * but overall we can live with it) 
     * @param bundle
     */
    public synchronized void update(Bundle bundle) {
        for (Book book : bundle.getBooks()) {
            List<Offer> offers = bundle.getBookOffers(book);
            if (offers.equals(cachedOffers.get(book))) {
                logger.info("CachedOffers - No changes for: " + book.getTitle());
                continue;
            } else {
                logger.info("CachedOffers - Adding offers to cache for: " + book.getTitle());
                setOffers(book, new ArrayList<Offer>(offers));
            }
        }
        
        if (cachedOffers.size() > 250)
        	purgeCache();
    }

    /**
     * Removes all cache values that are over 15 mins old
     */
	private synchronized void purgeCache() {
		logger.info("CachedOffers - purging cache!");
		// See which books to remove
		Set<Book> toRemove = new HashSet<Book>();
		long now = System.currentTimeMillis();
		for (Book book : cachedOffers.keySet()) {
			if (now - offerTimes.get(book) > MAX_CACHE_TIME)
				toRemove.add(book);
		}
		
		// Remove them
		for (Book book : toRemove) {
			cachedOffers.remove(book);
			offerTimes.remove(book);
		}
		
		logger.info("CachedOffers - cache size is " + cachedOffers.size() + " after purging");
	}
}
