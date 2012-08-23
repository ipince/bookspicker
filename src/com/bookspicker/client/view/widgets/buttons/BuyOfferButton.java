package com.bookspicker.client.view.widgets.buttons;

import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.SearchPage;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.Offer;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;

public class BuyOfferButton extends FocusPanel{
	private Offer offer;
	private Book book;
	boolean enabled;
	Image disabledImage = new Image(Resources.INSTANCE.buyOfferButtonDisabled());
	Image enabledImage = new Image(Resources.INSTANCE.buyOfferButton());
	
	public BuyOfferButton(Book book, Offer _offer) {
		super();		
		this.addStyleName(Resources.INSTANCE.style().bpButton());
		this.book = book;
		offer = _offer;
		enabled = true;
		this.setWidget(new Image(Resources.INSTANCE.buyOfferButton()));
		this.addClickHandler(SearchPage.buyHandler);
	}
	
	public BuyOfferButton(){
		super();		
		this.addStyleName(Resources.INSTANCE.style().bpButton());
		offer = null;
		setEnabled(false);
	}
	
	public void setEnabled(boolean _enabled){
		if(_enabled){
			this.getElement().getStyle().setCursor(Cursor.POINTER);
			this.setWidget(enabledImage);
			enabled = true;
		}
		else{
			this.getElement().getStyle().setCursor(Cursor.DEFAULT);
			this.setWidget(disabledImage);
			enabled = false;
		}
	}
	
	public boolean isEnabled(){
		return enabled;
	}
	
	public void setOffer(Offer _offer){
		offer = _offer;
	}
	
	public Offer getOffer() {
		return offer;
	}
	
	public Book getBook() {
		return book;
	}

}
