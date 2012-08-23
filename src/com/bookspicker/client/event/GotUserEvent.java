package com.bookspicker.client.event;

import com.bookspicker.shared.User;
import com.google.gwt.event.shared.GwtEvent;

public class GotUserEvent extends GwtEvent<GotUserEventHandler> {
	
	public static Type<GotUserEventHandler> TYPE = new Type<GotUserEventHandler>();

	private final User user;
	
	public GotUserEvent(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}
	
	@Override
	protected void dispatch(GotUserEventHandler handler) {
		handler.onGotUser(this);
	}

	@Override
	public Type<GotUserEventHandler> getAssociatedType() {
		return TYPE;
	}

}
