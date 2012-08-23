package com.bookspicker.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class EditOfferCancelledEvent extends GwtEvent<EditOfferCancelledEventHandler> {

	public static Type<EditOfferCancelledEventHandler> TYPE = new Type<EditOfferCancelledEventHandler>();
	
	@Override
	protected void dispatch(EditOfferCancelledEventHandler handler) {
		handler.onEditOfferCancelled(this);
	}

	@Override
	public Type<EditOfferCancelledEventHandler> getAssociatedType() {
		return TYPE;
	}

}
