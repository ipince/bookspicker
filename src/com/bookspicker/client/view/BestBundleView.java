package com.bookspicker.client.view;

import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.Resources.Style;
import com.bookspicker.client.view.widgets.BPPanel;
import com.bookspicker.client.view.widgets.LoadingPanel;
import com.bookspicker.client.view.widgets.buttons.BuyAllOffersButton;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.Bundle;
import com.bookspicker.shared.NumberUtil;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;

public class BestBundleView extends BPPanel {
	private FlowPanel offersList = new FlowPanel();
	private FlowPanel contentPanel;
	private Grid costAndSavings;
	LoadingPanel loadingPanel = new LoadingPanel("Looking for the best prices...");
	Bundle bundle;
	
	Label totalCostLabel;
	Label savingsLabel;
	Label totalCostDescription;
	Label savingsDescription;
	
	Style style;
	
	private static final String totalCostMsg = "total cost (books + shipping)";
	private static final String savingsMsg = "savings ";

	public BestBundleView(Bundle _bundle) {
		super("Here are the best prices for your cart, buy the offers you want :-)");
		
		style = Resources.INSTANCE.style();
		
		bundle = _bundle;
		this.setStylePrimaryName(Resources.INSTANCE.style().bestBundleView());
		contentPanel = getContentPanel();
		
		offersList.setStylePrimaryName(Resources.INSTANCE.style().offersList());
		
		savingsDescription = new Label(savingsMsg);
		savingsLabel = new Label();
		
		FlowPanel savingsDiv = new FlowPanel();
		savingsDiv.add(savingsLabel);
		savingsLabel.setStylePrimaryName(style.savingsLabel());
		savingsDiv.add(savingsDescription);
		savingsDescription.setStylePrimaryName(style.description());
		savingsDiv.setStylePrimaryName(style.bestBundleHeaderDiv());
		
		FlowPanel totalCostDiv = new FlowPanel();
		totalCostLabel = new Label();
		totalCostLabel.setStylePrimaryName(style.totalCostLabel());
		totalCostDescription = new Label(totalCostMsg);
		totalCostDescription.setStylePrimaryName(style.description());
		totalCostDiv.add(totalCostDescription);		
		totalCostDiv.add(totalCostLabel);
		totalCostDiv.setStylePrimaryName(style.bestBundleHeaderDiv());
		
		BuyAllOffersButton allOffersButton = new BuyAllOffersButton();
		allOffersButton.addStyleName(style.bestBundleHeaderDiv());
		
		costAndSavings = new Grid(1, 3);
		costAndSavings.setStylePrimaryName(style.priceSummaryGrid());
		CellFormatter formatter = costAndSavings.getCellFormatter();
		
		formatter.setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
	    formatter.setAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		formatter.setAlignment(0, 2, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);
		
		formatter.setWidth(0, 0, "33%");
	    formatter.setWidth(0, 1, "33%");
		formatter.setWidth(0, 2, "33%");
		
		costAndSavings.setWidget(0, 0, savingsDiv);
		costAndSavings.setWidget(0, 1, totalCostDiv);
		costAndSavings.setWidget(0, 2, allOffersButton);
		
		contentPanel.add(costAndSavings);
		
		loadingPanel.setVisible(false);
		contentPanel.add(loadingPanel);
		
		contentPanel.add(offersList);

	}

	public void refreshCosts(Bundle newBundle){
		bundle = newBundle;
		
		int totalCost = bundle.getTotalBundlePrice();
		String totalCostFormatted = NumberUtil.getDisplayPrice(totalCost);
		totalCostLabel.setText(totalCostFormatted);
		if (bundle.isTotalListPriceUndefined() == -1 && totalCost < bundle.getTotalListPrice()){
			costAndSavings.getCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
			savingsDescription.setVisible(true);
			savingsLabel.setVisible(true);
			String savings = NumberUtil.getDisplayPrice(bundle.getTotalListPrice() - totalCost);
			savingsLabel.setText(savings);
		} else {
			costAndSavings.getCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
			savingsDescription.setVisible(false);
			savingsLabel.setVisible(false);
		}
		costAndSavings.setVisible(true);
	}
	
	
	public void refreshBundle(Bundle newBundle) {
		bundle = newBundle;

		
		//TODO: We need to figure out the selection process... here the updating after 
		//selection is being done by a bunch of different parts like the OfferTablePanel
		// and the searchPage.
		//I have a bundle argument on refreshCosts because there is no logic to only
		// update the selected books right now... so its just updating costs but not
		// the view... in a way...
		refreshCosts(bundle);
		offersList.clear();
		for (Book book : bundle.getBooks()) {
			BookOfferView offerView = new BookOfferView(book,
					bundle.getSelectedOffer(book),
					bundle.getBookOffers(book));
			offersList.add(offerView);
		}
	}
	
	public void setLoadingVisible(boolean visible) {
		loadingPanel.setVisible(visible);
	}

	public void clear() {
		offersList.clear();
		costAndSavings.setVisible(false);
	}

}
