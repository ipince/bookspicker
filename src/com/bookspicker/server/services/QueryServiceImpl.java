package com.bookspicker.server.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.bookspicker.Log4JInitServlet;
import com.bookspicker.client.service.QueryService;
import com.bookspicker.server.bundling.Optimizer;
import com.bookspicker.server.data.ClassManager;
import com.bookspicker.server.data.LocalOfferManager;
import com.bookspicker.server.data.PatternUtil;
import com.bookspicker.server.data.StatManager;
import com.bookspicker.server.queries.AbeQuery;
import com.bookspicker.server.queries.AlibrisQuery;
import com.bookspicker.server.queries.AmazonQuery;
import com.bookspicker.server.queries.BookExQuery;
import com.bookspicker.server.queries.BookstoreQuery;
import com.bookspicker.server.queries.EbayQuery;
import com.bookspicker.server.queries.LocalQuery;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.Bundle;
import com.bookspicker.shared.ClassBook;
import com.bookspicker.shared.Item;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.Offer;
import com.bookspicker.shared.School;
import com.bookspicker.shared.SchoolClass;
import com.bookspicker.shared.Stat;
import com.bookspicker.shared.Term;
import com.bookspicker.shared.User;
import com.bookspicker.shared.ClassBook.Necessity;
import com.bookspicker.shared.ClassBook.Source;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class QueryServiceImpl extends RemoteServiceServlet implements QueryService {

	public static final CachedOffers CACHE = CachedOffers.getInstance();
    
    
    // TODO(rodrigo): separate bundle service from booklist service

	/**
	 * 
	 */
	private static final long serialVersionUID = -5593522948722613639L;
	private static Logger logger = Log4JInitServlet.logger;

	
	public Bundle getOffersForBundle(School school, Bundle bundle, boolean includeLocal) {
		long start = System.currentTimeMillis();
	    logger.debug("QueryServiceImpl - getting offers for bundle");
        // TODO (Rodrigo): this is non-ideal. We shouldn't refresh ALL offers
        // all the time, but in order to not do so, we need to add more logic
        // to the bundle/offer or change the way the Querys work. I think we
        // should change the way the queries work, but right now I don't have
        // time and this is not a high-priority issue. If you want to discuss
        // how we should go about this, talk to me.
        bundle.clearOffers();
        bundle.setAllNeedsUpdate(true);
        getOnlineOffers(bundle);
        
        User user = UserServiceImpl.getUser(getThreadLocalRequest(), getThreadLocalResponse());
		if (includeLocal) {
			// After optimizing for online offers, search local offers
			LocalQuery localQuery = new LocalQuery();
			localQuery.getBooksOffers(school, bundle, user, getThreadLocalRequest().getRemoteAddr());
			
			// BookEx (after local so its prices do not affect local auto-prices)
			BookExQuery bookEx = new BookExQuery();
			bookEx.getBooksOffers(bundle);
		}
		// TODO: do we need to re-optimize? analyze this! OR do we need to optimize before adding local offers?
		Optimizer.optimize(bundle, null, user, getThreadLocalRequest().getRemoteAddr(), true);

		bundle.setAllNeedsUpdate(false);
		logger.debug("QueryServiceImpl - Done getting offers; took " + (System.currentTimeMillis() - start) + "ms");
		
		// Log stat for the bundle search
		final String isbns = bundle.getIsbnListOfAllBooks();
		final String uid = user != null ? user.getId().toString() : null;
		final String ip = getThreadLocalRequest().getRemoteAddr();
		HelperThreads.execute(new Runnable() {
			@Override
			public void run() {
				StatManager.getManager().save(Stat.newBundleStat(isbns, uid, ip));
			}
		});
		
		return bundle;
	}
	
	private static void updateCache(Bundle bundle) {
	   CACHE.update(bundle);
    }

    public static void getOnlineOffers(final Bundle bundle) {
        // Get online offers
		List<BookstoreQuery> bookstoreServices = getBookstoreServices();
        final ThreadCounter queryCounter = new ThreadCounter(bookstoreServices.size());
        logger.info("QueryServiceImpl - getting offers from cache");
        retrieveOffersFromCache(bundle);
        
        final List<String> sentQueries = new ArrayList<String>();
        final List<String> receivedQueries = new ArrayList<String>();
        
        for (final BookstoreQuery service : bookstoreServices) {
        	logger.debug("QueryServiceImpl - getting offers from: " + service.toString());
        	Runnable runnable = new Runnable() {
        		@Override
        		public void run() {
        			try {
        			    sentQueries.add(service.getClass().getName());
        			    
        				service.getBooksOffers(bundle);
        			} catch (Exception e) {
        				logger.error("QueryServiceImpl - " + service.toString() + " failed getting offers: " + e.getMessage());
        			} finally {
        			    receivedQueries.add(service.getClass().getName());
        				queryCounter.decreaseCounter();
        			}
        		}
        	};
        	HelperThreads.execute(runnable);
        }
        int counter = 0; 
        while (queryCounter.getCount() != 0) {
        	try {
        		logger.debug("QueryServiceImpl - waiting for " + queryCounter.getCount() + " queries / sent: " + sentQueries.toString() + "received: " + receivedQueries.toString() );    
                Thread.sleep(500);
                if (++counter == 8) {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        updateCache(bundle);
        
        Optimizer.optimize(bundle, null, null);
	}
	
	private static void retrieveOffersFromCache(Bundle bundle) {
	    for (Book book : bundle.getBooks()) {
	        List<Offer> offers = CACHE.getOffers(book);
	        if (offers == null) {
	            continue;
	        } else {
	           bundle.setOffers(book, offers);
	        }
	    }
    }

    /**
	 * Returns a list containing all the supported BookstoreQuerys.
	 */
	private static List<BookstoreQuery> getBookstoreServices() {
		List<BookstoreQuery> bookstoreServices = new ArrayList<BookstoreQuery>();
		bookstoreServices.add(new AmazonQuery());
		bookstoreServices.add(new EbayQuery());
		bookstoreServices.add(new AlibrisQuery());
		bookstoreServices.add(new AbeQuery());
		// TODO(rodrigo): this is temporarily removed for multi-school support
		// (i didnt have time to add the B&N offers for every school :(
//		bookstoreServices.add(new CoopQuery());
		return bookstoreServices;
	}

	/**
	 * 
	 * @return null if the class is not found in our database
	 */
	public List<Item> getBookInfo(School school, String query) {
		logger.info("QueryServiceImpl - Finding items for: " + query);
		
		AmazonQuery amazonQuery = new AmazonQuery();
		ClassManager classMgr = ClassManager.getManager();
		query = query.toUpperCase();
		if (school.isClass(query)) {
			query = school.cleanClass(query);
			logger.info("QueryServiceImpl - Query matched class search. Searching for: " + query);
			
			List<Item> items = null; // this is the result
			
			// Check database
			SchoolClass clas = classMgr.getClassById(query);
			if (clas != null) {

				// TODO(Jonathan) - the worst hack in the world - Refactor ClassBook with Book as an interface.
				// TODO(rodrigo) - removed hack to add ClassBook vs. Book functionality.. fix!

//				return updateImageURL(clas, amazonQuery);
				items = new ArrayList<Item>(clas.getBooks());
				
				
				// Add 'crowdsourced' classbooks
				List<Book> copy = new ArrayList<Book>(); // list of Books, as opposed to Items
				for (Item item : items)
					copy.add(item.getBook());
				
				List<LocalOffer> offers = LocalOfferManager.getManager().getActiveLocalOffersForClass(clas.getCode());
				for (LocalOffer offer : offers) {
					if (!copy.contains(offer.getBook())) {
						items.add(new ClassBook(clas, offer.getBook(),
								Necessity.UNKNOWN, Source.LOCAL,
								"This book was added to this class by a " +
								"student selling the book! Therefore we cannot guarantee " +
								"that the book is actually needed for the class, though " +
								"it probably is (if you trust your fellow students!)"));
						copy.add(offer.getBook()); // add so we don't add again in case of repetition
					}
				}
			} // End if (if class not found, items==null)
			
			// Add stat for class search
			final String loggedValue = query;
			User user = UserServiceImpl.getUser(getThreadLocalRequest(), getThreadLocalResponse());
			final String uid = user != null ? user.getId().toString() : null;
			final String ip = getThreadLocalRequest().getRemoteAddr();
			HelperThreads.execute(new Runnable() {
				@Override
				public void run() {
					StatManager.getManager().save(Stat.newClassSearchStat(loggedValue, uid, ip));
				}
			});
			
			return items;

		} else { // Not a class query
			logger.info("QueryServiceImpl - Performing a book search: " + query);
			
			List<Item> result = new ArrayList<Item>(); // result
			
			if (PatternUtil.isIsbn(query)) {
				List<String> isbnList = new ArrayList<String>();
				isbnList.add(query);
				logger.debug("QueryServiceImpl - about to call amazon");
				result.addAll(amazonQuery.getBooksInfoByIsbn(isbnList)); 
			} else {
				logger.debug("QueryServiceImpl - about to call amazon");
				result.addAll(amazonQuery.getBooksInfoByQuery(query));
				logger.debug("return book list" + result.toString());
			}
			
			// Add stat for regular book search (isbn or keyword)
			final String loggedValue = query;
			User user = UserServiceImpl.getUser(getThreadLocalRequest(), getThreadLocalResponse());
			final String uid = user != null ? user.getId().toString() : null;
			final String ip = getThreadLocalRequest().getRemoteAddr();
			HelperThreads.execute(new Runnable() {
				@Override
				public void run() {
					StatManager.getManager().save(Stat.newBookSearchStat(loggedValue, uid, ip));
				}
			});
			
			return result;
		}
	}
	
	public Map<String, List<Item>> search(School school, List<String> queries) {
		Map<String, List<Item>> results = new LinkedHashMap<String, List<Item>>();
		for (String query : queries) {
			results.put(query, getBookInfo(school, query));
		}
		return results;
	}

	private List<Item> updateImageURL(SchoolClass clas, AmazonQuery amazonQuery) {
		List<String> isbnList = clas.getIsbnList();
		return new ArrayList<Item>(amazonQuery.getBooksInfoByIsbn(isbnList));
		/*for (Book book : books) {
      for (ClassBook classBook : clas.getBooks()) {
        if (book.getIsbn() == classBook.getBook().getIsbn()) {
          classBook.getBook().setImageUrl(book.getImageUrl());
        }
      }
    }*/
	}

	private List<String> classes;
	public List<String> getCurrentClasses() {
		if (classes != null)
			return classes;
		List<String> classes = new ArrayList<String>();
		for (SchoolClass sc : ClassManager.getManager().listClasses(Term.FALL2010)) {
			classes.add(sc.getCode());
		}
		this.classes = classes;
		return classes;
	}

}
