package com.bookspicker.client.view;

import com.bookspicker.client.event.BuyOfferEvent;
import com.bookspicker.client.event.BuyOfferEventHandler;
import com.bookspicker.client.presenter.BuyOfferPresenter;
import com.bookspicker.client.service.LocalOfferServiceAsync;
import com.bookspicker.client.view.BuyOfferView;
import com.bookspicker.client.view.HasHeader;
import com.bookspicker.client.view.HeaderView;
import com.bookspicker.client.view.Page;
import com.bookspicker.client.view.widgets.BPPanel;
import com.bookspicker.shared.AuthenticationException;
import com.bookspicker.shared.CannotBuyOfferException;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.Pair;
import com.bookspicker.shared.User;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class BuyOfferPage extends Composite implements HasHeader {
	
	private static final String SUCCESS_MSG = "<br/ >Congratulations! <br /><br />We have contacted " +
			"the seller and CC'd you in the email. <br />Just coordinate the payment and pickup of the book with the seller " +
			"and enjoy the book. <br /><br />Thank you for using BooksPicker!";
	
	private static BuyOfferPageUiBinder uiBinder = GWT.create(BuyOfferPageUiBinder.class);
	
	interface BuyOfferPageUiBinder extends UiBinder<Widget, BuyOfferPage> {}
	
	@UiField
	SimplePanel centralViewWrapper;
	@UiField
	static SimplePanel headerViewWrapper;
	private static final HeaderView headerView = HeaderView.getHeaderView();
	
	private static FlowPanel contentContainer;
	
	private final LocalOfferServiceAsync offerService;
	
	private BuyOfferPresenter buyOfferPresenter;
	
	public BuyOfferPage(final LocalOfferServiceAsync offerService, HandlerManager eventBus) {
		this.offerService = offerService;
		initWidget(uiBinder.createAndBindUi(this));
		
		headerViewWrapper.setWidget(headerView);
		
		centralViewWrapper.setStylePrimaryName(Resources.INSTANCE.style().sellerPageWrapper());
		
		BPPanel mainPanel = new BPPanel("Buy Offer Whose Seller is Local");
		contentContainer = mainPanel.getContentPanel();
		centralViewWrapper.setWidget(mainPanel);
		
		buyOfferPresenter = new BuyOfferPresenter(eventBus, new BuyOfferView());
		
		eventBus.addHandler(BuyOfferEvent.TYPE, new BuyOfferEventHandler() {
			@Override
			public void onBuyOffer(BuyOfferEvent event) {
				offerService.buyOffer(event.getOffer().getId(), event.getOffer().getPrice(), new AsyncCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						showSuccess();
					}
					@Override
					public void onFailure(Throwable caught) {
						if (caught instanceof AuthenticationException) {
							buyOfferPresenter.showError("Unable to buy offer. " + caught.getMessage());
						} else if (caught instanceof CannotBuyOfferException) {
							buyOfferPresenter.showError("Unable to buy offer. " + caught.getMessage());
						} else {
							buyOfferPresenter.showError("Oops! Something bad happened and the transaction didn't come through. Please refresh and try again!");
						}
					}
				});
			}
		});
	}
	
	public void setState(String param) {
		if (param != null && param.matches("\\d{1,8}")) {
			fetchOffer(Integer.valueOf(param));
		} else {
			showError("The offer id you requested " + (param != null ? ("(" + param + ")") : "") + " is invalid. The id must be an integer with at most 8 digits.");
		}
	}
	
	private void fetchOffer(final int offerId) {
		offerService.getOffer(offerId, new AsyncCallback<Pair<LocalOffer, User>>() {
			@Override
			public void onSuccess(Pair<LocalOffer, User> result) {
				// TODO: figure out what to do about user logged in
				if (result == null || result.getFirst() == null) {
					showError("An offer with id " + offerId + " doesn't exist.");
				} else {
					buyOfferPresenter.setOffer(result.getFirst());
					buyOfferPresenter.go(contentContainer);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				showError("An error ocurred trying to find the requested offer. Please try again later.");
			}
		});
	}
	
	private void showError(String error) {
		contentContainer.clear();
		contentContainer.add(new Label(error));
	}
	
	private void showSuccess() {
		contentContainer.clear();
		HTML successMsgHTML = new HTML(SUCCESS_MSG);
		successMsgHTML.getElement().getStyle().setFontSize(11, Unit.PT);
		successMsgHTML.getElement().getStyle().setPaddingLeft(10, Unit.PX);
		contentContainer.add(successMsgHTML);
	}

	@Override
	public void updateLoginContainer() {
		headerView.updateLoginContainer();
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		headerViewWrapper.setWidget(headerView);
		headerView.setSelectedMenuItem(Page.SEARCH);
	}
}
