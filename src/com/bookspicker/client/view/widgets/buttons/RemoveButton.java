package com.bookspicker.client.view.widgets.buttons;

import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.SearchPage;
import com.bookspicker.client.view.BundleBookView;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;

public class RemoveButton extends FocusPanel{
	HandlerRegistration normalClickReg;
	
	BundleBookView associatedView;
	
	public RemoveButton(BundleBookView bookView) {
		super();
		this.setStylePrimaryName(Resources.INSTANCE.style().removeButton());
		this.addStyleName(Resources.INSTANCE.style().bpButton());
		this.setTitle("Remove from bundle");
		associatedView = bookView;
		this.setWidget(new Image(Resources.INSTANCE.removeButton()));
		normalClickReg = this.addClickHandler(SearchPage.removeHandler);
	}
	
	public BundleBookView getAssociatedBookView(){
		return associatedView;
	}

}
