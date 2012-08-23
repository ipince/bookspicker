package com.bookspicker.client.event;

import com.bookspicker.shared.LocalOffer;
import com.google.gwt.event.shared.GwtEvent;

public class EditOfferEvent extends GwtEvent<EditOfferEventHandler> {
	
	public static Type<EditOfferEventHandler> TYPE = new Type<EditOfferEventHandler>();

	private final LocalOffer offer;
	
	public EditOfferEvent(LocalOffer offer) {
		this.offer = offer;
	}
	
	public LocalOffer getOffer() {
		return offer;
	}

	@Override
	protected void dispatch(EditOfferEventHandler handler) {
		handler.onEditOffer(this);
	}

	@Override
	public Type<EditOfferEventHandler> getAssociatedType() {
		return TYPE;
	}

}
