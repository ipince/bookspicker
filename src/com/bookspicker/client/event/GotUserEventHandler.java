package com.bookspicker.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface GotUserEventHandler extends EventHandler {
	
	void onGotUser(GotUserEvent event);

}
