package com.bookspicker.client.service;

import com.bookspicker.shared.User;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("user")
public interface UserService extends RemoteService {
	
	User getUser(boolean refreshOffers);
	
	User getTestUser();
	
	void logout();

}
