package com.bookspicker.client.presenter;

import com.bookspicker.client.BooksPicker;
import com.bookspicker.client.service.UserService;
import com.bookspicker.client.service.UserServiceAsync;
import com.bookspicker.client.event.GotUserEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LogoutHandler implements ClickHandler {

	private final UserServiceAsync userService = GWT.create(UserService.class);

	@Override
	public void onClick(ClickEvent event) {
		userService.logout(callback);
	}

	private AsyncCallback<Void> callback = new AsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {
			sendEvent();
		}
		@Override
		public void onFailure(Throwable caught) {
			sendEvent();
		}

		private void sendEvent() {
			// Delete cookies: this should actually happen on
			// the server and NOT on the client. However, I
			// (rodrigo) was unable to get it to work. For some
			// unknown reason, the cookies aren't clearing when
			// cleared on the server! This is a temporary solution
			// TODO: fix it
			if (Cookies.getCookie("uid") != null) {
				GWT.log("LogoutHandler - Cookie didn't clear on the server!!", null);
			}
			Cookies.removeCookie("uid");
			
			GWT.log("LogoutHandler - Logging out, sending GotUserEvent with null user", null);
			BooksPicker.eventBus.fireEvent(new GotUserEvent(null));
		}
	};

}
