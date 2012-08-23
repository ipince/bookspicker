package com.bookspicker.client.service;

import com.bookspicker.shared.User;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface UserServiceAsync {

	void getUser(boolean refreshOffers, AsyncCallback<User> callback);
	
	void getTestUser(AsyncCallback<User> callback);

	void logout(AsyncCallback<Void> callback);

}
