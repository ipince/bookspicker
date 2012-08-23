package com.bookspicker.client.view;

import com.bookspicker.client.view.Resources;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.NumberUtil;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;

public class SimpleOfferView extends FlexTable {
	
	public SimpleOfferView(LocalOffer offer) {
		this.setStylePrimaryName(Resources.INSTANCE.style().editOfferDetailsTable());
		setWidget(0, 0, new Label("Status: " + offer.getStatus()));
		setWidget(1, 0, new Label("Seller: " + offer.getSellerName()));
		setWidget(2, 0, new Label("Condition: " + offer.getCondition()));
		if (offer.isSold()) {
			setWidget(3, 0, new Label("Price: " + NumberUtil.getDisplayPrice(offer.getSellingPrice())));
		} else {
			setWidget(3, 0, new Label("Price: " + (offer.getPrice() < 0 ? "N/A" : NumberUtil.getDisplayPrice(offer.getPrice()))));
		}
		setWidget(4, 0, new Label("Location: " + (offer.getLocation() == null ? "(none specified)" : offer.getLocation().getDisplayName())));
		setWidget(5, 0, new Label("Comments: " + (offer.getComments() == null ? "(none)" : offer.getComments())));
	}

}
