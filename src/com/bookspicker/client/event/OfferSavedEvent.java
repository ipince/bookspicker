package com.bookspicker.client.event;

import com.bookspicker.shared.User;
import com.google.gwt.event.shared.GwtEvent;

public class OfferSavedEvent extends GwtEvent<OfferSavedEventHandler> {
	
	  public static Type<OfferSavedEventHandler> TYPE = new Type<OfferSavedEventHandler>();
	  
	  private final User user;
	  
	  public OfferSavedEvent(User user) {
	    this.user = user;
	  }
	  
	  public User getUser() { return user; }
	  
	  @Override
	  public Type<OfferSavedEventHandler> getAssociatedType() {
	    return TYPE;
	  }

	  @Override
	  protected void dispatch(OfferSavedEventHandler handler) {
		  handler.onOfferSaved(this);
	  }

}
