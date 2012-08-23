package com.bookspicker.client.view;

import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.Resources.Style;
import com.bookspicker.client.presenter.BuyOfferPresenter;
import com.bookspicker.client.view.widgets.buttons.BuyOfferButton;
import com.bookspicker.shared.LocalOffer;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class BuyOfferView extends Composite implements
		BuyOfferPresenter.Display {

	private static final String BUY_NOTE_1 = "Clicking \"Buy offer\" will put you and the seller in contact and will remove this listing from the search results.";
	private static final String BUY_NOTE_2 = "Only click if you actually intend on buying this book!";

	private FlowPanel contentPanel = new FlowPanel();
	private SimplePanel bookContainer = new SimplePanel();
	private SimplePanel offerContainer = new SimplePanel();

	private static final Style STYLE = Resources.INSTANCE.style();

	private HTML error = new HTML();
	private HTML mustLoginLabel;
	private CheckBox agreeCheckbox = new CheckBox(
			" I understand and do intend to buy this book!");
	private BuyOfferButton buyButton = new BuyOfferButton();
	private boolean bound = false;

	private Grid buyStuff;
	
	public BuyOfferView() {
		initWidget(contentPanel);

		FlowPanel hPanel = new FlowPanel();
		hPanel.setStylePrimaryName(STYLE.buyOfferViewContent());

		FlowPanel offerDetailsPanel = new FlowPanel();
		offerDetailsPanel.setStylePrimaryName(STYLE.buyOfferDetailsPanel());
		Label offerDetailsLabel = new Label("Offer details");
		offerDetailsLabel.setStylePrimaryName(STYLE.buyOfferViewHeading());
		offerDetailsPanel.add(offerDetailsLabel);
		offerDetailsPanel.add(offerContainer);
		hPanel.add(offerDetailsPanel);

		FlowPanel bookDetailsPanel = new FlowPanel();
		bookDetailsPanel.setStylePrimaryName(STYLE.buyOfferBookDetailsPanel());
		Label bookDetailsLabel = new Label("Book details");
		bookDetailsLabel.setStylePrimaryName(STYLE.buyOfferViewHeading());
		bookDetailsPanel.add(bookDetailsLabel);
		bookDetailsPanel.add(bookContainer);
		hPanel.add(bookDetailsPanel);

		contentPanel.add(hPanel);

		buyStuff = new Grid(4, 1);
		buyStuff.setCellSpacing(10);
		buyStuff.setStylePrimaryName(STYLE.buyofferTextAndButtonTable());
		HTML buyNoteLabel = new HTML("<b><i>"+BUY_NOTE_1 + " " + BUY_NOTE_2+"</i></b>");
		buyStuff.setWidget(0, 0, buyNoteLabel);
		buyStuff.setWidget(1, 0, agreeCheckbox);
		agreeCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if (event.getValue()) {
					buyButton.setEnabled(true);
				} else {
					buyButton.setEnabled(false);
				}
			}
		});
	
		buyStuff.setWidget(2, 0, buyButton);
		error.getElement().getStyle().setColor("red");
		buyStuff.setWidget(3, 0, error);
		offerDetailsPanel.add(buyStuff);
		
		mustLoginLabel = new HTML("You must be logged in to buy this book!");
		mustLoginLabel.setStylePrimaryName(STYLE.mustLoginLabel());
		offerDetailsPanel.add(mustLoginLabel);
	}

	@Override
	public BuyOfferButton getBuyButton() {
		return buyButton;
	}

	@Override
	public boolean hasReadAgreement() {
		return agreeCheckbox.getValue();
	}

	@Override
	public void setNeedsLogin(boolean needsLogin) {
		buyStuff.setVisible(!needsLogin);
		mustLoginLabel.setVisible(needsLogin);
	}

	@Override
	public void showError(String msg) {
		error.setText(msg);
		// error.setVisible(true);
	}

	@Override
	public void setData(LocalOffer offer) {
		BookDetailedView bookView = new BookDetailedView(offer.getBook());
		bookContainer.setWidget(bookView);
		offerContainer.setWidget(new SimpleOfferView(offer));
	}

	@Override
	public boolean isBound() {
		return bound;
	}

	@Override
	public void setBound(boolean bound) {
		this.bound = bound;
	}

	@Override
	public Widget asWidget() {
		return this;
	}

}
