package com.bookspicker.client.view.widgets.buttons;

import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.SearchPage;
import com.bookspicker.client.view.BundleBookView;
import com.bookspicker.client.view.widgets.OfferTablePanel;
import com.bookspicker.shared.Offer;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;

public class SelectOfferButton extends FocusPanel{

	private Offer offer;
	private OfferTablePanel panel;

	HandlerRegistration normalClickReg;
	
	BundleBookView associatedView;

	public SelectOfferButton(Offer offer, OfferTablePanel panel) {
		super();		
		this.addStyleName(Resources.INSTANCE.style().bpButton());

		this.offer = offer;
		this.panel = panel;
		
		this.setWidget(new Image(Resources.INSTANCE.selectOfferButton()));
		normalClickReg = this.addClickHandler(SearchPage.selectHandler);
	}

	public Offer getOffer() {
		return offer;
	}

	public OfferTablePanel getPanel() {
		return panel;
	}
	
	
}
