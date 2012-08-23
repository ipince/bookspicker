package com.bookspicker.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.user.client.Window;

public class ClientExceptionHandler implements UncaughtExceptionHandler {

	@Override
	public void onUncaughtException(Throwable e) {
		
		GWT.log(e.getMessage(), null);
		// TODO: alert the user in a friendlier way
		Window.alert("An unexpected error occurred. Sorry for the " +
				"inconvenience, please try again! If the error " +
				"persists, please do let us know.");
		
		// TODO: log exception with server or something
	}

}
