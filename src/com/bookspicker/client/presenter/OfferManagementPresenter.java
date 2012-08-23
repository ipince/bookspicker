package com.bookspicker.client.presenter;

import java.util.List;

import com.bookspicker.client.service.LocalOfferServiceAsync;
import com.bookspicker.client.service.UserServiceAsync;
import com.bookspicker.client.event.CreateOfferEvent;
import com.bookspicker.client.event.EditOfferEvent;
import com.bookspicker.client.view.widgets.HasOffer;
import com.bookspicker.client.view.widgets.LoadingPanel;
import com.bookspicker.shared.AuthenticationException;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.User;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/**
 * Controls the OfferManagementView, which is basically the
 * main control panel of the seller page.
 * 
 * @author Rodrigo Ipince
 */
public class OfferManagementPresenter implements Presenter {
	
	public interface DataDisplay {
		HasClickHandlers getCreateOfferButton();
		
		List<HasOffer> getEditButtons();
		List<HasOffer> getPostButtons();
		List<HasOffer> getDeleteButtons();
		
		void setData(User user); // TODO: remove dependency on User
		void setBound(boolean bound);
		boolean isBound();
		Widget asWidget();
	}
	
	// Here to remove dependency of Presenter on UI elements
	public interface LoginDisplay {
		HasClickHandlers getLoginButton();
		
		void setBound(boolean bound);
		boolean isBound();
		Widget asWidget();
	}
	
	private HasWidgets container;
	
	private final UserServiceAsync userService;
	private final LocalOfferServiceAsync offerService;
	private final HandlerManager eventBus;
	private final DataDisplay dataDisplay; // View
	private final LoginDisplay loginDisplay; // (static) View
	private final LoadingPanel loading = new LoadingPanel("Getting your information");
	
	private User user; // Model
	
	public OfferManagementPresenter(UserServiceAsync userService,
			LocalOfferServiceAsync offerSerivce,
			HandlerManager eventBus, DataDisplay dataView, 
			LoginDisplay loginView) {
		this.userService = userService;
		this.offerService = offerSerivce;
		this.eventBus = eventBus;
		this.dataDisplay = dataView;
		this.loginDisplay = loginView;
	}
	
	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public void go(HasWidgets container) {
		this.container = container;
		bind();
		if (user == null) {
			fetchUser();
		} else {
			showDataDisplay(user);
		}
	}
	
	private void showDataDisplay(User user) {
		dataDisplay.setData(user);
		bindExistingOfferButtons();
		container.clear();
		container.add(dataDisplay.asWidget());
	}

	private void showLoginDisplay() {
		container.clear();
		container.add(loginDisplay.asWidget());
	}
	
	private void bind() {
		if (!dataDisplay.isBound()) {
			// binds the Display with the appropriate handlers
			dataDisplay.getCreateOfferButton().addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					// Fire the event up for someone to handle
					eventBus.fireEvent(new CreateOfferEvent());
				}
			});
			dataDisplay.setBound(true);
		}
		
		if (!loginDisplay.isBound()) {
			loginDisplay.getLoginButton().addClickHandler(new LoginHandler(userService, userCallback));
			loginDisplay.setBound(true);
		}
	}
	
	private void bindExistingOfferButtons() {
		for (final HasOffer editButton : dataDisplay.getEditButtons()) {
			editButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					LocalOffer offer = editButton.getOffer();
					if (editButton.isEnabled() && !offer.isSold()) {
						// Fire event up for someone to handle
						eventBus.fireEvent(new EditOfferEvent(editButton.getOffer()));	
					}
				}
			});
		}
		
		for (final HasOffer postButton : dataDisplay.getPostButtons()) {
			postButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (postButton.isEnabled()) {
						LocalOffer offer = postButton.getOffer();
						offerService.postOffer(offer.getId(),
								!offer.isActive(), offerCallback);
					}
				}
			});
		}
		for (final HasOffer deleteButton : dataDisplay.getDeleteButtons()) {
			deleteButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if(deleteButton.isEnabled()){
						if (Window.confirm("Are you sure you want to delete this offer?")) {
							offerService.deleteOffer(deleteButton.getOffer().getId(), offerCallback);
						}
					}
				}
			});
		}
	}

	private void fetchUser() {
		container.clear(); // added because this sometimes takes too much time!
		container.add(loading);
		userService.getUser(true, userCallback);
	}
	
	private AsyncCallback<User> userCallback = new AsyncCallback<User>() {
		@Override
		public void onSuccess(User result) {
			GWT.log("OfferManagementPresenter - Got user succesfully", null);
			if (result == null) {
				showLoginDisplay();
			} else {
				showDataDisplay(result);
			}
		}

		@Override
		public void onFailure(Throwable caught) {
			if (caught instanceof AuthenticationException) {
				GWT.log("OfferManagementPresenter - Could not get User object (user is not logged in", null);
				showLoginDisplay();
			} else {
				GWT.log("OfferManagementPresenter - Could not get User object: " + caught.getMessage(), null);
				// TODO: add remote logging or something
				showLoginDisplay();
			}
		}
	};
	
	private AsyncCallback<User> offerCallback = new AsyncCallback<User>() {
		@Override
		public void onSuccess(User user) {
			GWT.log("OfferManagementPresenter - Success calling offer service", null);
			showDataDisplay(user);
		}
		
		@Override
		public void onFailure(Throwable caught) {
			GWT.log("OfferManagementPresenter - Offer call failed", null);
			if (caught instanceof AuthenticationException) {
				GWT.log(caught.getMessage(), null);
				// TODO
				Window.alert(caught.getMessage());
			}
		}
	};

}
