package com.bookspicker.server.services;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.bookspicker.Log4JInitServlet;
import com.bookspicker.client.service.LocalOfferService;
import com.bookspicker.server.data.LocalOfferManager;
import com.bookspicker.server.data.StatManager;
import com.bookspicker.server.data.TransactionManager;
import com.bookspicker.server.data.UserManager;
import com.bookspicker.server.email.EmailSender;
import com.bookspicker.server.queries.BuybackQuery;
import com.bookspicker.server.social.SocialGraph;
import com.bookspicker.shared.AuthenticationException;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.Bundle;
import com.bookspicker.shared.CannotBuyOfferException;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.Pair;
import com.bookspicker.shared.Stat;
import com.bookspicker.shared.Transaction;
import com.bookspicker.shared.User;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class LocalOfferServiceImpl extends RemoteServiceServlet implements LocalOfferService {

	private static Logger logger = Log4JInitServlet.logger;
	
	@Override
	public User saveOffer(LocalOffer offer) throws AuthenticationException {
		
		// Get user
		User user = getUser(true); // require login
		
		if (offer.getId() != null) {
			// If offer has id, get owned offer and apply changes
			checkOwnership(user, offer.getId());
			updateOfferToDb(offer);
		} else {
			// It's a new offer, just add it to user
			offer.resetMetadata(); // to make it 'new'
			offer.setOwner(user);
			// Save (the offer)
			saveOfferToDb(offer);
		}
		
		// Update User's offer list and return
		user = updateUser(user);
		
		// Update User's
		if (offer.getLocation() != null) {
			if (offer.getLocation() != user.getLocation()) {
				final User userPointer = user;
				userPointer.setLocation(offer.getLocation());
				final HttpServletRequest req = getThreadLocalRequest();
				final HttpServletResponse resp = getThreadLocalResponse();
				HelperThreads.execute(new Runnable() {
					@Override
					public void run() {
						UserManager.getManager().updateUser(userPointer);
						UserServiceImpl.setUser(userPointer, req, resp);
					}
				});
			}
		}
		
		return user;
	}

	@Override
	public User postOffer(long offerId, boolean post)
			throws AuthenticationException {
		// Get user
		User user = getUser(true); // require login
		
		// Get correct offer, apply changes, save
		LocalOffer offer = checkOwnership(user, offerId);
		if (post) {
			offer.activate();
		} else {
			offer.deactivate();
		}
		updateOfferToDb(offer);
		
		user = updateUser(user);
		return user;
	}

	@Override
	public User deleteOffer(long offerId) throws AuthenticationException {
		// Get user
		User user = getUser(true); // require login
		
		// Get correct offer, delete
		LocalOffer offer = checkOwnership(user, offerId);
		deleteOffer(offer);
		
		user = updateUser(user);
		return user;
	}

	@Override
	public Pair<LocalOffer, User> getOffer(long offerId) {
		// Get user (no login required)
		User user = null;
		try {
			user = getUser(false);
		} catch (AuthenticationException e) {
			// This should never happen, but just in case
		}

		// Get offer and calculate price if necessary
		LocalOffer offer = LocalOfferManager.getManager().getLocalOfferWithId(offerId);
		if (offer != null && offer.isAutoPricing()) { // TODO: not calculate for sold offers
			Bundle dummyBundle = new Bundle();
			Book book = offer.getBook();
			dummyBundle.addBook(book);
			QueryServiceImpl.getOnlineOffers(dummyBundle);
			
			offer.calculatePrice(dummyBundle.getBookOffers(book), BuybackQuery.getBuybackOffers(book));
		}
		
		// Personalize local offer with Facebook info
		if (user != null) {
			double sd = SocialGraph.getSocialDistance(user.getFib(), offer.getOwner().getFib());
			if (sd <= 1)
				offer.setSellerName("A friend");
			else if (sd <= 2)
				offer.setSellerName("A friend's friend");
		}
		
		return new Pair<LocalOffer, User>(offer, user);
	}
	
	
	@Override
	public synchronized void buyOffer(long offerId, int price) throws AuthenticationException, CannotBuyOfferException {
		logger.debug("LocalOfferServiceImpl - Buying offer with id " + offerId + " for " + price);
		User buyer = getUser(true); // Require login
		LocalOffer offer = getOffer(offerId).getFirst(); // get it with the price and all
		if (offer != null) {
			
			// Can't buy an offer that's already been sold
			if (offer.isSold()) {
				throw new CannotBuyOfferException("Offer has already been sold. Sorry!");
			}
			// Can't buy an offer that's not active
			if (!offer.isActive()) {
				throw new CannotBuyOfferException("Offer is not active (not for 'sale')");
			}
			
			// Can't buy your own offer
			if (offer.getOwner().getId().equals(buyer.getId())) {
				throw new CannotBuyOfferException("You cannot buy your own book, silly!");
			}
			
			// Can't buy if you've already bought offers for this book
			List<Transaction> transactions = TransactionManager.getManager().getTransactions(buyer.getId(), offer.getBook().getIsbn());
			Date now = new Date();
			for (Transaction trans : transactions) {
				if (now.getTime() - trans.getTime().getTime() < Transaction.WAIT_TIME) {
					logger.info("LocalOfferServiceImpl - User attempted to buy the same book within a " + Transaction.WAIT_HOURS + " hour period");
					StatManager.getManager().save(Stat.newAntiAbuseStat(offer.getBook().getIsbn(), buyer.getId().toString(), getThreadLocalRequest().getRemoteAddr()));
					throw new CannotBuyOfferException("You already 'bought' a copy of this " +
							"book within the last " + Transaction.WAIT_HOURS + " hours. We " +
							"only allow each user to 'buy' the same book every so " +
							"often to protect our sellers from users who try to " +
							"contact multiple sellers while they only want to buy " +
							"1 book. If you feel you should be able to buy this book, " +
							"send us an email to bookspicker at mit.edu");
				}
			}
			
			// Can't buy an offer with no price
			if (offer.getPrice() < 0) {
				// Happens when there's not enough information to set an auto-price (rare event)
				throw new CannotBuyOfferException("There's no price available for this " +
						"offer, and therefore it cannot be bought.");
			}
			
			// Can't buy if the prices don't match
			if (offer.getPrice() != price) {
				throw new CannotBuyOfferException("There was a price mis-match between the " +
						"client and the server. It might be that the price has " +
						"changed from when you loaded the offer on your screen " +
						"until when you clicked the 'Buy' button. Please refresh " +
						"and try again");
			}
			
			String email = buyer.getMitEmail();
			if (email == null)
				email = buyer.getFbEmail();
			// The sale was successful, so notify participants
			String sellerEmail = offer.getOwner().getMitEmail();
			if (sellerEmail == null)
				sellerEmail = offer.getOwner().getFbEmail();
			try {
				logger.debug("LocalOfferServiceImpl - About to send email to: " + sellerEmail + ", " + offer.getOwner().getName() + ", " + email);
				double priceDbl = price * 1.0 / 100;
				NumberFormat nf = new DecimalFormat("$#,##0.00");
				EmailSender.sendIntroductionEmail(sellerEmail, offer.getOwner().getName(), email, offer.getBook().getTitle(), nf.format(priceDbl));
			} catch (Exception e) {
				logger.warn("LocalOfferServiceImpl - Warning! Unable to email buyer and seller! " + e.getMessage());
				throw new CannotBuyOfferException("We were unable to contact the seller. His/her email address must be erroneous or a connection error occurred. Sorry!");
			}
			
			// Save transaction
			TransactionManager.getManager().save(new Transaction(buyer.getId(), offer.getBook().getIsbn()));
			
			// Save to DB after everything else is successful
			offer.buy(email, buyer.getId(), price);
			updateOfferToDb(offer);
		}
	}
	
	/**
	 * Retrieves the current user.
	 * 
	 * @param needsLogin whether the caller requires the user to
	 * be logged in or not.
	 * @return the logged in user, or null if there's no user
	 * logged in and needsLogin is false
	 * @throws AuthenticationException if there's no user logged
	 * in and needsLogin is true
	 */
	private User getUser(boolean needsLogin) throws AuthenticationException {
		User user = UserServiceImpl.getUser(getThreadLocalRequest(), getThreadLocalResponse());
		if (user == null && needsLogin) {
			// not logged in
			throw new AuthenticationException("You need to be logged in to perform this action!");
		}
		return user;
	}
	
	/**
	 * @throws AuthenticationException if user doesn't own the Offer
	 * @return the LocalOffer owned by the User that matches
	 * the given offer id
	 */
	private LocalOffer checkOwnership(User user, Long id) throws AuthenticationException {
		// TODO(Rodrigo): Note that we don't get the offers by doing user.getOffers()
		// because we have no guarantee that the User will have the updated
		// list of offers. I guess we could guarantee it, but I don't want to
		// think about what that means right now. I think it would involve
		// changes to UserManager (to populate the list when the User is
		// fetched), to LocalOfferManager (to set the Offers on the User's
		// list), as well as updating the Session's User every time we make
		// a change to his offer list.
		List<LocalOffer> offers = LocalOfferManager.getManager().getLocalOffersOwnedBy(user);
		
		for (LocalOffer owned : offers) {
			if (owned.getId().equals(id))
				return owned;
		}
		
		throw new AuthenticationException("You don't own the offer you're trying to access");
	}
	
	private void saveOfferToDb(LocalOffer offer) {
		LocalOfferManager.getManager().save(offer);
	}
	
	private void updateOfferToDb(LocalOffer offer) {
		LocalOfferManager.getManager().update(offer);
	}
	
	private void deleteOffer(LocalOffer offer) {
		if (!offer.isDeleted()) {
			offer.delete();
			LocalOfferManager.getManager().update(offer);
		}
	}
	
	private User updateUser(User user) {
		List<LocalOffer> offers = LocalOfferManager.getManager().getLocalOffersOwnedBy(user);

		for (LocalOffer offer : offers) {
			if (offer != null && offer.isAutoPricing() && offer.isActive()) {
				Bundle dummyBundle = new Bundle();
				Book book = offer.getBook();
				dummyBundle.addBook(book);
				QueryServiceImpl.getOnlineOffers(dummyBundle);

				offer.calculatePrice(dummyBundle.getBookOffers(book), BuybackQuery.getBuybackOffers(book));
			}
		}
		
		user.setOffers(offers);
		return user;
	}
}
