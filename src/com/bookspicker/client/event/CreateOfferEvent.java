package com.bookspicker.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class CreateOfferEvent extends GwtEvent<CreateOfferEventHandler> {
	
	  public static Type<CreateOfferEventHandler> TYPE = new Type<CreateOfferEventHandler>();
	  
	  @Override
	  public Type<CreateOfferEventHandler> getAssociatedType() {
	    return TYPE;
	  }

	  @Override
	  protected void dispatch(CreateOfferEventHandler handler) {
		  handler.onCreateOffer(this);
	  }

}
