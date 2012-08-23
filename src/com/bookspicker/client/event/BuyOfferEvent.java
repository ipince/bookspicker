package com.bookspicker.client.event;

import com.bookspicker.shared.LocalOffer;
import com.google.gwt.event.shared.GwtEvent;

public class BuyOfferEvent extends GwtEvent<BuyOfferEventHandler> {

	public static Type<BuyOfferEventHandler> TYPE = new Type<BuyOfferEventHandler>();
	
	private final LocalOffer offer;
	
	public BuyOfferEvent(LocalOffer offer) {
		this.offer = offer;
	}
	
	public LocalOffer getOffer() {
		return offer;
	}
	
	@Override
	protected void dispatch(BuyOfferEventHandler handler) {
		handler.onBuyOffer(this);
	}

	@Override
	public Type<BuyOfferEventHandler> getAssociatedType() {
		return TYPE;
	}

}
