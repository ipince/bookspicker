package com.bookspicker.client.view;

import com.bookspicker.client.event.CreateOfferEvent;
import com.bookspicker.client.event.CreateOfferEventHandler;
import com.bookspicker.client.event.EditOfferCancelledEvent;
import com.bookspicker.client.event.EditOfferCancelledEventHandler;
import com.bookspicker.client.event.EditOfferEvent;
import com.bookspicker.client.event.EditOfferEventHandler;
import com.bookspicker.client.event.OfferSavedEvent;
import com.bookspicker.client.event.OfferSavedEventHandler;
import com.bookspicker.client.presenter.EditOfferPresenter;
import com.bookspicker.client.presenter.OfferManagementPresenter;
import com.bookspicker.client.service.LocalOfferServiceAsync;
import com.bookspicker.client.service.QueryServiceAsync;
import com.bookspicker.client.service.UserServiceAsync;
import com.bookspicker.client.view.EditOfferView;
import com.bookspicker.client.view.HasHeader;
import com.bookspicker.client.view.HeaderView;
import com.bookspicker.client.view.LoginView;
import com.bookspicker.client.view.OfferManagementView;
import com.bookspicker.client.view.Page;
import com.bookspicker.client.view.widgets.BPPanel;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.User;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Controller for all the different views under the Seller Page panel.
 * 
 * @author Rodrigo Ipince
 */
public class SellerPage extends Composite implements HasHeader {
	
	private static SellerPageUiBinder uiBinder = GWT.create(SellerPageUiBinder.class);
	
	interface SellerPageUiBinder extends UiBinder<Widget, SellerPage> {}
	
	@UiField
	SimplePanel centralViewWrapper;
	@UiField
	SimplePanel headerViewWrapper;
	private static final HeaderView headerView = HeaderView.getHeaderView();
	
	private FlowPanel contentContainer;
	
	private final UserServiceAsync userService;
	private final LocalOfferServiceAsync offerService;
	private final QueryServiceAsync queryService;
	private final HandlerManager eventBus;
	
	// Child Presenters
	private OfferManagementPresenter offerManagementPresenter;
	private EditOfferPresenter editOfferPresenter;
	
	public SellerPage(UserServiceAsync userService,
			LocalOfferServiceAsync offerService,
			QueryServiceAsync queryService,
			HandlerManager eventBus) {
		this.userService = userService;
		this.offerService = offerService;
		this.queryService = queryService;
		this.eventBus = eventBus;
		
		initWidget(uiBinder.createAndBindUi(this));
		createLayout();
		
		headerView.setSelectedMenuItem(Page.SELL);
		headerViewWrapper.setWidget(headerView);
		
		// Handle offer creation and edit
		eventBus.addHandler(CreateOfferEvent.TYPE, new CreateOfferEventHandler() {
			@Override
			public void onCreateOffer(CreateOfferEvent event) {
				presentEditOffer(null);
			}
		});
		eventBus.addHandler(EditOfferEvent.TYPE, new EditOfferEventHandler() {
			@Override
			public void onEditOffer(EditOfferEvent event) {
				presentEditOffer(event.getOffer());
			}
		});
		
		// Handle offer saving
		eventBus.addHandler(OfferSavedEvent.TYPE, new OfferSavedEventHandler() {
			@Override
			public void onOfferSaved(OfferSavedEvent event) {
				presentOfferManagement(event.getUser());
			}
		});
		
		// Handle edit offer cancelled
		eventBus.addHandler(EditOfferCancelledEvent.TYPE, new EditOfferCancelledEventHandler() {
			@Override
			public void onEditOfferCancelled(EditOfferCancelledEvent event) {
				presentOfferManagement(null);
			}
		});
		// No user, so fetch it from the server
//		userService.getUser(loginCallback);
	}
	
	private void createLayout() {
		centralViewWrapper.setStylePrimaryName(Resources.INSTANCE.style().sellerPageWrapper());
		BPPanel mainPanel = new BPPanel("Sell Your Books");
		centralViewWrapper.setWidget(mainPanel);
		contentContainer = mainPanel.getContentPanel();
	}
	
	private void presentEditOffer(LocalOffer offer) {
		// TODO: add history token?
		if (editOfferPresenter == null) { // be lazy
			editOfferPresenter = new EditOfferPresenter(offerService, queryService, eventBus, new EditOfferView());
		}
		
		editOfferPresenter.setOffer(offer);
		editOfferPresenter.go(contentContainer);
	}
	
	public void presentOfferManagement(User user) {
		// TODO: history token?
		if (offerManagementPresenter == null) {
			offerManagementPresenter = new OfferManagementPresenter(userService, offerService, eventBus, new OfferManagementView(), new LoginView(true));
		}
		offerManagementPresenter.setUser(user);
		offerManagementPresenter.go(contentContainer);
	}

	@Override
	public void updateLoginContainer() {
		headerView.updateLoginContainer();
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		headerViewWrapper.setWidget(headerView);
		headerView.setSelectedMenuItem(Page.SELL);
	}

}
