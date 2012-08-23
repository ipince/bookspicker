package com.bookspicker.client.presenter;

import com.bookspicker.client.event.BuyOfferEvent;
import com.bookspicker.client.view.widgets.buttons.BuyOfferButton;
import com.bookspicker.shared.LocalOffer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/**
 * Controls the BuyOfferView, which is presented when a
 * user wants to buy a local offer.
 * 
 * @author Rodrigo Ipince
 */
public class BuyOfferPresenter implements Presenter {
	
	public interface Display {
		BuyOfferButton getBuyButton(); // TODO: nasty dependence on Button
		boolean hasReadAgreement();
		
		void showError(String msg);
		void setNeedsLogin(boolean needsLogin);
		void setData(LocalOffer offer); // TODO: remove dependency
		
		boolean isBound();
		void setBound(boolean bound);
		Widget asWidget();
	}
	
	private HasWidgets container;
	
	private final HandlerManager eventBus;
	private final Display display; // View
	
	private LocalOffer offer; // Model
	
	public BuyOfferPresenter(HandlerManager eventBus, Display view) {
		this.eventBus = eventBus;
		display = view;
	}
	
	/**
	 * 
	 * @param offer != null
	 */
	public void setOffer(LocalOffer offer) {
		this.offer = offer;
	}

	@Override
	public void go(HasWidgets container) {
		this.container = container;
		bind();
		showOffer(offer);
	}

	private void bind() {
		if (!display.isBound()) {
			display.getBuyButton().addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (display.hasReadAgreement() && display.getBuyButton().isEnabled()) {
						// fire event
						eventBus.fireEvent(new BuyOfferEvent(offer));
					} else {
						display.showError("You must agree in order for us to put you in touch with the seller!");
					}
				}
			});
			display.setBound(true);
		}
	}

	private void showOffer(LocalOffer offer) {
		display.setData(offer);
		display.setNeedsLogin(Cookies.getCookie("uid") == null);
		//TODO: figure out how we can show already sold status without disabling the button.
//		if (offer.isSold() || !offer.isActive()) {
//			display.getBuyButton().setEnabled(false);
//		} else {
//			display.getBuyButton().setEnabled(true);
//		}
		container.clear();
		container.add(display.asWidget());
	}
	
	public void showError(String msg) {
		display.showError(msg);
	}

}
