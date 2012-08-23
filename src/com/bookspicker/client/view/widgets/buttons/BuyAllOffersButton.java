package com.bookspicker.client.view.widgets.buttons;

import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.SearchPage;
import com.bookspicker.shared.Offer;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;

public class BuyAllOffersButton extends FocusPanel{
	Offer offer;
	
	public BuyAllOffersButton() {
		super();
		this.setStylePrimaryName(Resources.INSTANCE.style().buyAllOffersButton());
		this.addStyleName(Resources.INSTANCE.style().bpButton());
		this.setWidget(new Image(Resources.INSTANCE.buyAllOffersButton()));
		this.addClickHandler(SearchPage.buyAllSelectedOffersHandler);
	}
}

