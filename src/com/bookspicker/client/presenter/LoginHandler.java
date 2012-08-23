package com.bookspicker.client.presenter;

import com.bookspicker.client.HistoryToken;
import com.bookspicker.client.service.UserServiceAsync;
import com.bookspicker.shared.User;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LoginHandler implements ClickHandler {
	
	public static final boolean FORCE_FB_TEST_CALL = false;
	public static final boolean LIVE_TEST = false;
	
	/**
	 * Nasty hack because facebook doesn't allow question marks
	 * in URLs. This is just nasty.
	 */
	public static final String Q_REPLACEMENT = "ioG3on4";
	public static final String EQ_REPLACEMENT = "87gs8FDAG";
	
	private static final String FB_CLIENT_ID = "100580116660807";
	private static final String FB_PERMISSIONS = "email";
	private static final String FB_DIALOG = "page";
	
	public static final String DOMAIN = "bookspicker.com";
	public static final String TEST_DOMAIN = "173.203.92.121";
	
	private final UserServiceAsync userService;
	private final AsyncCallback<User> callback;
	
	public LoginHandler() {
		userService = null;
		callback = null;
	}
	
	public LoginHandler(UserServiceAsync userService, AsyncCallback<User> callback) {
		this.userService = userService;
		this.callback = callback;
	}
	
	@Override
	public void onClick(ClickEvent event) {
		if (GWT.isScript() || FORCE_FB_TEST_CALL) {
			GWT.log("Redirecting user to: " + getAuthUrl(), null);
			redirect(getAuthUrl());
		} else {
			// for testing only
			bypassLogin();
		}
	}
	
	private String getAuthUrl() {
		String origin = History.getToken();
		if (origin.isEmpty())
			origin = HistoryToken.HOME;
		if (origin.startsWith(HistoryToken.SEARCH))
			origin = HistoryToken.SEARCH;
		origin = origin.replace("?", Q_REPLACEMENT);
		origin = origin.replace("=", EQ_REPLACEMENT);
		String fbCallback = null;
		if (FORCE_FB_TEST_CALL)
			fbCallback = "http://" + (LIVE_TEST ? TEST_DOMAIN : DOMAIN) + "/abookspicker/fb_auth?origin=" + origin;
		else
			fbCallback = "http://" + (LIVE_TEST ? TEST_DOMAIN : DOMAIN) + "/bookspicker/fb_auth?origin=" + origin;
		
		String url = "https://graph.facebook.com/oauth/authorize?client_id=" + FB_CLIENT_ID +
			"&scope=" + FB_PERMISSIONS + 
			"&display=" + FB_DIALOG +
			"&redirect_uri=" + fbCallback;
		return url;
	}
	
	/**
	 * Doing tests with the real Facebook login enabled is
	 * extremely annoying, since Facebook requires all requests
	 * to originate from your production server.
	 * 
	 * This method allows you to bypass authentication when
	 * on development mode. You should ONLY call it while running
	 * from code that is guarded by a call to !GWT.isScript().
	 * Since GWT.isScript() returns false if and only if GWT is
	 * running in development mode, then the method will never
	 * run when in production. In fact, the GWT compiler will
	 * remove this method from the compiled javascript since it
	 * won't be reachable in production mode.
	 * 
	 * Of course, as an added security layer, the server will
	 * only bypass the login successfully if the request originated
	 * from localhost, so even if an attacker sent a request to
	 * the server to bypass the login, it would still be secure. 
	 */
	private void bypassLogin() {
		if (userService != null) {
			userService.getTestUser(callback);
		}
	}
	
	//redirect the browser to the given url
	public native void redirect(String url)/*-{
	      $wnd.location = url;
	}-*/;

}
