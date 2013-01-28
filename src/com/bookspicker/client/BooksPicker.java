package com.bookspicker.client;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bookspicker.client.event.Analytics;
import com.bookspicker.client.event.GotUserEvent;
import com.bookspicker.client.event.GotUserEventHandler;
import com.bookspicker.client.service.LocalOfferService;
import com.bookspicker.client.service.LocalOfferServiceAsync;
import com.bookspicker.client.service.QueryService;
import com.bookspicker.client.service.QueryServiceAsync;
import com.bookspicker.client.service.UserService;
import com.bookspicker.client.service.UserServiceAsync;
import com.bookspicker.client.view.BuyOfferPage;
import com.bookspicker.client.view.HasHeader;
import com.bookspicker.client.view.HeaderView;
import com.bookspicker.client.view.InformationPage;
import com.bookspicker.client.view.Page;
import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.ResultsView;
import com.bookspicker.client.view.SchoolSelectionPage;
import com.bookspicker.client.view.SearchPage;
import com.bookspicker.client.view.SearchPageUnified;
import com.bookspicker.client.view.SellerPage;
import com.bookspicker.client.view.pageContents.HomePageContent;
import com.bookspicker.shared.School;
import com.bookspicker.shared.User;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.RootLayoutPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class BooksPicker implements EntryPoint, ValueChangeHandler<String> {

	SearchPage searchPage;
	InformationPage homePage;
	InformationPage faqPage;
	InformationPage aboutPage;
	InformationPage pageNotFoundPage;
	SellerPage sellerPage;
	BuyOfferPage buyOfferPage;
	
	/**
	 * Keeps track of what the current displayed page is. This
	 * is used to set up the header status (logged in or logged
	 * out) when a GotUserEvent is received.
	 */
	HasHeader current;
	
	private final UserServiceAsync userService = GWT.create(UserService.class);
	private final LocalOfferServiceAsync offerService = GWT.create(LocalOfferService.class);
	private final QueryServiceAsync queryService = GWT.create(QueryService.class);
	private SearchPageUnified searchPageUnified;
	private static String preSchoolSelectionToken = "";
	
	/**
	 * Making this public and static is NASTY. It was only done
	 * out of desperation to get the user stuff working fine (see
	 * LogoutHandler for example). We should think of a better
	 * solution for this. But who knows, maybe making the eventBus
	 * easily accessible to anyone (like this) may not be a terrible
	 * idea after all.
	 */
	public static HandlerManager eventBus;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		Resources.INSTANCE.style().ensureInjected();

		GWT.setUncaughtExceptionHandler(new ClientExceptionHandler());
		
		eventBus = new HandlerManager(null);
		eventBus.addHandler(GotUserEvent.TYPE, new GotUserEventHandler() {
			@Override
			public void onGotUser(GotUserEvent event) {
				User user = event.getUser();
				if (user == null) {
					GWT.log("BooksPicker - Got user: null", null);
				} else {
					GWT.log("BooksPicker - Got user: " + user.getName(), null);
				}
				// Fire current view to refresh
				History.fireCurrentHistoryState();
			}
		});
		
		String initToken = History.getToken();
		//setSchoolFromDomain(); // if available
		setSchool(School.MIT);
		updateHeaderView();

		if (initToken.isEmpty()) {
			// No specific token, so go home
			History.newItem(HistoryToken.HOME, false);
		}
//		else if (!(initToken.equals(HistoryToken.FAQ) || initToken.equals(HistoryToken.ABOUT))) {
//			History.newItem(HistoryToken.SELECT_SCHOOL, false);
//			preSchoolSelectionToken = initToken;
//		}
		
		firefox3compatibility(); // Make Firefox 3 work!

		History.addValueChangeHandler(this);
		History.fireCurrentHistoryState();
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		String token = event.getValue();
		GWT.log("BooksPicker - Handling token: " + token, null);
		
		RootLayoutPanel.get().clear();
		if (token.equals(HistoryToken.SELECT_SCHOOL)) {
			SchoolSelectionPage selectSchool = new SchoolSelectionPage();
			if (preSchoolSelectionToken.startsWith(HistoryToken.SEARCH)){
				Map<String, String> params = parseParams(preSchoolSelectionToken);
				String query = params.get(HistoryToken.DISPLAYED_QUERY);
				if(query != null)
					selectSchool.setPreSelectionSearchText(query);
			}
			current = null;
			RootLayoutPanel.get().add(selectSchool);
		} else if(token.startsWith(HistoryToken.SCHOOL_PICKED_PREFIX)){
			String school = token.substring(HistoryToken.SCHOOL_PICKED_PREFIX.length());
			setSchool(School.fromName(school));
			current = null;
			History.newItem(preSchoolSelectionToken);
			preSchoolSelectionToken = ""; // reset
		} else if ((getSchool().equals("") && !(token.equals(HistoryToken.FAQ) || token.equals(HistoryToken.ABOUT))) || (getSchool().equals(School.NONE)&& token.equals(HistoryToken.SELLER))) {
			History.newItem(HistoryToken.SELECT_SCHOOL);
			preSchoolSelectionToken = token;
//		} else if (token.equals(HistoryToken.HOME) || token.isEmpty()) {
//			if (homePage == null)
//				homePage = new InformationPage(Page.HOME);
//			homePage.updateLoginContainer();
//			if(getSchool().equals(School.NONE)){
//				HomePageContent.setGeneric(true);
//			}
//			else{
//				HomePageContent.setGeneric(false);
//			}
//			current = homePage;
//			RootLayoutPanel.get().add(homePage);
		} else if (token.startsWith(HistoryToken.SEARCH_UNIFIED) || token.equals(HistoryToken.HOME) || token.isEmpty()) {
			Map<String, String> params = parseParams(token);
			if (searchPageUnified == null)
				searchPageUnified = new SearchPageUnified();
			searchPageUnified.updateLoginContainer();
			if(getSchool().equals(School.NONE)){
				ResultsView.setGeneric(true);
			}
			else{
				ResultsView.setGeneric(false);
			}
			current = searchPageUnified;
			if (token.startsWith(HistoryToken.SEARCH_UNIFIED)) {
				Analytics.trackSearchFromURL(token);
			}
			searchPageUnified.setState(params.get(HistoryToken.PARAM_QUERY),
					params.get(HistoryToken.PARAM_BUNDLE), params.get(HistoryToken.PARAM_OFFERS), params.get(HistoryToken.DISPLAYED_QUERY));
			RootLayoutPanel.get().add(searchPageUnified);
//		} else if (token.startsWith(HistoryToken.SEARCH)) {
//			Map<String, String> params = parseParams(token);
//			if (searchPage == null)
//				searchPage = new SearchPage();
//			searchPage.updateLoginContainer();
//			if(getSchool().equals(School.NONE)){
//				ResultsView.setGeneric(true);
//			}
//			else{
//				ResultsView.setGeneric(false);
//			}
//			current = searchPage;
//			searchPage.setState(params.get(HistoryToken.PARAM_QUERY),
//					params.get(HistoryToken.PARAM_BUNDLE), params.get(HistoryToken.PARAM_OFFERS), params.get(HistoryToken.DISPLAYED_QUERY));
//			RootLayoutPanel.get().add(searchPage);
		} else if (token.equals(HistoryToken.SELLER)) {
			if (sellerPage == null)
				sellerPage = new SellerPage(userService, offerService, queryService, eventBus);
			GWT.log("BooksPicker - In #sell token", null);
			sellerPage.updateLoginContainer();
			current = sellerPage;
			sellerPage.presentOfferManagement(null);
			RootLayoutPanel.get().add(sellerPage);
		} else if (token.equals(HistoryToken.FAQ)) {
			if (faqPage == null)
				faqPage = new InformationPage(Page.FAQ);
			faqPage.updateLoginContainer();
			current = faqPage;
			RootLayoutPanel.get().add(faqPage);
		} else if (token.equals(HistoryToken.ABOUT)) {
			if (aboutPage == null)
				aboutPage = new InformationPage(Page.ABOUT);
			aboutPage.updateLoginContainer();
			current = aboutPage;
			RootLayoutPanel.get().add(aboutPage);
		} else if (token.startsWith(HistoryToken.BUY_LOCAL)) {
			if (buyOfferPage == null)
				buyOfferPage = new BuyOfferPage(offerService, eventBus);
			buyOfferPage.updateLoginContainer();
			current = buyOfferPage;
			Map<String, String> params = parseParams(token);
			buyOfferPage.setState(params.get(HistoryToken.PARAM_OFFER_ID));
			RootLayoutPanel.get().add(buyOfferPage);
		} else {
			if (pageNotFoundPage == null)
				pageNotFoundPage = new InformationPage(Page.PAGE_NOT_FOUND);
			pageNotFoundPage.updateLoginContainer();
			current = pageNotFoundPage;
			RootLayoutPanel.get().add(pageNotFoundPage);
		}
	}

	private Map<String, String> parseParams(String token) {
		Map<String, String> params = new HashMap<String, String>();
		String[] tokenParts = token.split("\\" + HistoryToken.PARAM_STARTER);
		if (tokenParts.length >= 2) {
			String[] mappings = tokenParts[1].split(HistoryToken.PARAM_DELIMETER);
			String[] values;
			for (String mapping : mappings) {
				values = mapping.split("=");
				if (values.length == 2) { // be defensive
					params.put(URL.decode(values[0]), URL.decode(values[1]));
				}
			}
		}
		return params;
	}
	
	/**
	 * Used so that the SuggestBoxPopup displays correctly in Firefox 3+
	 */
	private static native void firefox3compatibility() /*-{
	 if (!$doc.getBoxObjectFor) {
	  $doc.getBoxObjectFor = function (element) {
	   var box = element.getBoundingClientRect();
	   return { "x"     : box.left,  "y"      : box.top,
	            "width" : box.width, "height" : box.height };
	  }
	 }
	}-*/;

	/**
	 * Retrieves the schools name from the subdomain and places it in a cookie.
	 */
	private native String getSubdomain (String url) /*-{
	  var patt1=/(http:\/\/)(.*)(\.bookspicker\.com)/g;
    return (url.split(patt1)[2]);
	}-*/;
	
	private void setSchoolFromDomain() {
		String schoolStr = getSubdomain(GWT.getModuleBaseURL());
		School school = School.fromName(schoolStr);
		// Only set the school from the domain if it's actually
		// meaningful (not none)
		if (school != School.NONE) {
			setSchool(school);
		}
	}

	private void setSchool(School school) {
		Date schoolCookieExprDate = new Date(System.currentTimeMillis() + 1000*60*60*24*365); // 1 year
		Cookies.setCookie("school", school.getName(), schoolCookieExprDate);

		updateHeaderView();

		if (searchPage != null || homePage != null) {
			searchPage = null;
			homePage = null;
			Cookies.removeCookie("bundle");
			Cookies.removeCookie("searchState");
		}
	}

	private static void updateHeaderView() {
		School school = getSchool();
		switch (school) {
		case MIT:
			HeaderView.getHeaderView().setLogo(Resources.INSTANCE.mitLogo());
			break;
		case DARTMOUTH:
			HeaderView.getHeaderView().setLogo(Resources.INSTANCE.dartmouthLogo());
			break;
		case UCHICAGO:
			HeaderView.getHeaderView().setLogo(Resources.INSTANCE.uchicagoLogo());
			break;
		case NORTHWESTERN:
			HeaderView.getHeaderView().setLogo(Resources.INSTANCE.northwesternLogo());
			break;
		case NONE:
			HeaderView.getHeaderView().setLogo(Resources.INSTANCE.logo());
			break;
		}
	}
	
	public static void genericSearchSelected(){
		updateHeaderView();
		preSchoolSelectionToken = "";
	}
	
	public static School getSchool() {
		String schoolStr = Cookies.getCookie("school");
		return School.MIT;//School.fromName(schoolStr == null ? "" : schoolStr);
	}
}
