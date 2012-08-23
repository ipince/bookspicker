package com.bookspicker.server.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.bookspicker.Log4JInitServlet;
import com.bookspicker.client.HistoryToken;
import com.bookspicker.client.presenter.LoginHandler;
import com.bookspicker.server.data.FriendshipManager;
import com.bookspicker.server.data.UserManager;
import com.bookspicker.server.queries.FacebookGraphApi;
import com.bookspicker.server.social.SocialGraph;
import com.bookspicker.shared.Friendship;
import com.bookspicker.shared.User;

@SuppressWarnings("serial")
public class FacebookAuthServlet extends HttpServlet {

	private static final String THIS_URL = "http://" + (LoginHandler.LIVE_TEST ? LoginHandler.TEST_DOMAIN : LoginHandler.DOMAIN) + "/bookspicker/fb_auth?origin=";
	private static final String TEST_URL = "http://" + (LoginHandler.LIVE_TEST ? LoginHandler.TEST_DOMAIN : LoginHandler.DOMAIN) + "/abookspicker/fb_auth?origin=";
//	private static final String REDIRECT_STR = "/redirect?href=";
	private static final String REDIRECT_STR = "/";
	private Logger logger = Log4JInitServlet.logger;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		/*
		 * Facebook will redirect to this servlet when the user grants
		 * the application access. The request will contain a 'code' param
		 * that can be used to obtain an access_token for further requests.
		 * To request the access_token, we must include the given 'code', as
		 * well as the original 'redirect_uri' used for the auth request,
		 * which is this one (but it also contains an 'origin' param that
		 * we use to navigate back to the original requesting page). For example,
		 * the 'redirect_uri' might like like:
		 * http://www.bookspicker.com/bookspicker/fb_auth?origin=#buy=1234567890
		 */
		String code = req.getParameter("code");
		String originalOrigin = req.getParameter("origin");
		String errorReason = req.getParameter("error_reason");
		
		logger.info("FacebookAuthServlet - Origin was: " + originalOrigin);
		String redirectOrigin = originalOrigin;
		if (redirectOrigin == null || redirectOrigin.isEmpty()) {
			redirectOrigin = HistoryToken.HOME;
		} else if (!redirectOrigin.contains("#")) {
			redirectOrigin = "#" + redirectOrigin;
		}
		redirectOrigin = redirectOrigin.replace(LoginHandler.EQ_REPLACEMENT, "=");
		redirectOrigin = redirectOrigin.replace(LoginHandler.Q_REPLACEMENT, "?");
		
		if (errorReason != null) {
			// Something bad happened
			logger.warn("FacebookAuthServlet - User failed to authenticate: " + errorReason);
			resp.sendRedirect(REDIRECT_STR + redirectOrigin);
			return;
		}
		
		if (code != null && originalOrigin != null) {
			// Need to get token
			String thisUrl = THIS_URL;
			if (LoginHandler.FORCE_FB_TEST_CALL)
				thisUrl = TEST_URL;
			String redirectUrl = thisUrl + originalOrigin;
			
			String response = null;
			try {
				String address = buildAccessTokenRequest(code, redirectUrl);
				logger.info("FacebookAuthServlet - Calling: " + address);
				URL url = new URL(address);
				BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
				response = reader.readLine();
			} catch (IOException e) {
				logger.error("FacebookAuthServlet - Could not get response from Facebook, redirecting to " + redirectOrigin);
				resp.sendRedirect(REDIRECT_STR + redirectOrigin);
				return;
			}
			
			logger.info("Response from facebook is: " + response);
			String[] values = response.split("&");
			if (values.length == 2) {
				String accessToken = values[0].split("=")[1]; // TODO: this is nasty
				String expiration = values[1].split("=")[1];
				System.out.println("Access token is: " + accessToken);
				System.out.println("Expires: " + expiration);
				
				// Set up the session for the logged in user
				setUpSession(req, resp, accessToken);
				
				// Send user back to original page
				resp.sendRedirect(REDIRECT_STR + redirectOrigin);
			} else {
				// TODO: something isn't right
				logger.warn("Wrong number of elements in facebook's response");
				resp.sendRedirect(REDIRECT_STR + redirectOrigin);
			}
		} else {
			// TODO: No code -> error or what?
			logger.warn("Warning: there was either no code or no origin in the url!");
			resp.sendRedirect(REDIRECT_STR + redirectOrigin);
		}
	}

	/**
	 * Sets up the session for this User.
	 * 
	 * Looks for the User object in our database, updates it with Facebook's
	 * data if necessary (TODO), creates an HttpSession object and adds
	 * the User object to it. We also include the facebook accessToken in
	 * case we need to send facebook further requests, though it shouldn't
	 * really be necessary.
	 * 
	 * Thus, a user is 'logged in' if and only if there exists a User object
	 * in its HttpSession. To log a user out, simply remove the User object
	 * from his session (see UserServiceImpl).
	 */
	private void setUpSession(HttpServletRequest req, HttpServletResponse resp, String accessToken) {
		
		HttpSession session = req.getSession();
		
		try {
			JSONObject json = FacebookGraphApi.getPublicInformation(accessToken);
			if (json == null) {
				// TODO Facebook didn't respond with details - what to do?
			}
			
			// Public information MUST contain name and fib (among other stuff)
			String name = json.getString("name");
			final String fib = json.getString("id");
			
			// Since we requested extended permissions, should also have email
			String email = json.getString("email");
			
			// Get friends
			final List<String> friends = FacebookGraphApi.getFriendList(accessToken);
			
			System.out.println("name is: " + name);
			System.out.println("fib is: " + fib);
			System.out.println("email is: " + email);
			System.out.println("number of friends is: " + friends.size());
			
			// Get user with this fib
			User user = UserManager.getManager().getUserWithFib(fib);
			
			// If not there, create and save the user
			if (user == null) {
				user = new User(fib, name);
				user.setFbEmail(email.toLowerCase());
				if (email != null && email.toLowerCase().endsWith("mit.edu"))
					user.setMitEmail(email.toLowerCase());
				user.setFriends(friends);
				
				user = UserManager.getManager().saveUser(user);
				
				// Save friends to the database and add to graph
				HelperThreads.execute(new Runnable() {
					@Override
					public void run() {
						FriendshipManager friendManager = FriendshipManager.getManager();
						for (String friendId : friends) {
							// TODO: update friendships once in a while
							Friendship friendship = friendManager.createAndSaveFriendship(fib, friendId);
							SocialGraph.addFriend(friendship);
						}
					}
				});
			}
			
			// Sub-par way to determine whether login was successful or not (TODO: fix)
			if (name != null && fib != null) {
				UserServiceImpl.setUser(user, req, resp);
				session.setAttribute("fbAccessToken", accessToken); // just in case
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String buildAccessTokenRequest(String code, String redirectUrl) {
		return FacebookGraphApi.FB_AUTH_URL + "?client_id=" + FacebookGraphApi.FB_APP_ID +
				"&redirect_uri=" + redirectUrl +
				"&client_secret=" + FacebookGraphApi.FB_APP_SECRET +
				"&code=" + code;
	}
	
}
