package com.bookspicker.server.services;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.bookspicker.Log4JInitServlet;
import com.bookspicker.client.service.UserService;
import com.bookspicker.server.data.LocalOfferManager;
import com.bookspicker.server.data.Sandbox;
import com.bookspicker.server.data.UserManager;
import com.bookspicker.server.queries.BuybackQuery;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.Bundle;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.User;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class UserServiceImpl  extends RemoteServiceServlet implements UserService {

	private static Logger logger = Log4JInitServlet.logger;
	
	@Override
	public User getUser(boolean refreshOffers) {
		
		logger.debug("UserServiceImpl - Getting user");
		User user = getUser(getThreadLocalRequest(), getThreadLocalResponse());
		if (user != null)
			logger.info("UserServiceImpl - Got User " + user.getName() + ", who has " + user.getFriends().size() + " friends and " + user.getOffers().size() + " offers");
		else
			logger.info("UserServiceImpl - No User found");
		
		if (refreshOffers && user != null) {
			List<LocalOffer> offers = LocalOfferManager.getManager().getLocalOffersOwnedBy(user);
			
			// Calculate the offer's price, if necessary
			for (LocalOffer offer : offers) {
				if (offer.isAutoPricing()) {
					Bundle dummyBundle = new Bundle();
					Book book = offer.getBook();
					dummyBundle.addBook(book);
					QueryServiceImpl.getOnlineOffers(dummyBundle);

					offer.calculatePrice(dummyBundle.getBookOffers(book), BuybackQuery.getBuybackOffers(book));
				}
			}
			
			user.setOffers(offers);
			setUser(user, getThreadLocalRequest(), getThreadLocalResponse());
		}
		return user;
	}
	
	@Override
	public User getTestUser() {
		String remoteHost = getThreadLocalRequest().getRemoteHost();
		logger.info("UserServiceImpl - Getting test user; remote host is: " + remoteHost);

		User user = null;
	//	if ("127.0.0.1".equals(remoteHost)) {
			user = getUser(getThreadLocalRequest(), getThreadLocalResponse());
			if (user == null) {
				user = Sandbox.addSomeData();
				setUser(user, getThreadLocalRequest(), getThreadLocalResponse());
		//	}
		}

		return user;
	}

	@Override
	public void logout() {
		logger.debug("UserServiceImpl - Logging out user");
		
		// Delete cookie (doesn't work for some reason!!)
		Cookie[] cookies = getThreadLocalRequest().getCookies();
		Cookie cook;
		for (int i = 0; i < cookies.length; i++) {
			cook = cookies[i];
			if ("uid".equals(cook.getName())) {
				logger.debug("UserServiceImpl - Deleting cookie");
				cook.setMaxAge(0);
				getThreadLocalResponse().addCookie(cook);
			}
		}
		
		// Delete session stuff
		try {
			getThreadLocalRequest().getSession().invalidate();
		} catch (IllegalStateException e) {
			// do nothing - means session has already been invalidated
		}
	}
	
	/**
	 * @param req Request holding the current HttpSession
	 * @return the currently logged in User, or null if the client
	 * is not logged in
	 */
	public static User getUser(HttpServletRequest req, HttpServletResponse resp) {
		
		User user = null;
		
		// Try to get user from Session
		Object userObj = req.getSession().getAttribute("user");
		if (userObj instanceof User) { // should always be the case
			user = (User) userObj;
		}

		// If not in session, check cookie! If user is in cookie,
		// then add him to the session as well. But only if the
		// signature cookie is also present and they match!
		if (user == null) {
			// Check cookie and assign
			Cookie[] cookies = req.getCookies();
			String uid = null;
			String sig = null;
			if (cookies != null) {
				for (int i = 0; i < cookies.length; i++) {
					if ("uid".equals(cookies[i].getName())) {
						uid = cookies[i].getValue();
					} else if ("sig".equals(cookies[i].getName())) {
						sig = cookies[i].getValue();
					}
				}


				if (uid != null && sig != null) {
					logger.info("UserServiceImpl - No User in session, but found uid " + uid + " in cookie");
					if (SigningUtil.checkSignature(uid, sig)) {
						user = UserManager.getManager().getUserWithUid(uid);
						logger.info("UserServiceImpl - Signature matched! Returning User (if found)");

						// Should we refresh cookie if user is not null?
						// Yes, to account for the rare case
						// in which the user deletes the "uid" cookie but is
						// still logged in (didn't delete the JSESSIONID cookie).
						// That way, the header will refresh appropriately and
						// show a Logout link.

						// Note, however, that we need to update the header
						// explicitly on the client, which isn't happening
						// right now... TODO: implement this (on the client)
						if (user != null)
							setUser(user, req, resp);
					} else {
						logger.info("UserServiceImpl - Signature didn't match, so ignoring");
					}
				}
			}
		}
		
		return user;
	}
	
	public static void setUser(User user, HttpServletRequest req, HttpServletResponse resp) {
		logger.debug("UserServiceImpl - Setting user: " + user.getId() + " with name " + user.getFullName());
		
		// Set user in Session
		req.getSession().setAttribute("user", user);
		
		// Set cookie
		setCookie(user, resp);
	}
	
	private static void setCookie(User user, HttpServletResponse resp) {
		// Set uid cookie
		Cookie userCookie = new Cookie("uid", user.getId().toString());
		userCookie.setMaxAge(60*60*24*7); // 7 days (in seconds)
		userCookie.setPath("/");
		resp.addCookie(userCookie);
		
		// Set signature!
		String sig = SigningUtil.sign(user.getId().toString());
		Cookie signature = new Cookie("sig", sig);
		signature.setMaxAge(60*60*24*7); // 7 days (in seconds)
		signature.setPath("/");
		resp.addCookie(signature);
	}

}
