package com.bookspicker.server.queries;

import com.bookspicker.shared.Bundle;

/**
 * An interface for all the online stores that are queried. 
 * 
 * @author Jonathan
 */
public interface BookstoreQuery {

	/**
	 * Populates Offers for each of the books in the given Bundle.
	 *  
	 * @param bundle the Bundle to be processed.
	 */
	public void getBooksOffers(Bundle bundle);
	
}