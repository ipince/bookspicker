package com.bookspicker.client.view.widgets.buttons;

import com.bookspicker.client.view.Resources;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;

public class AddNewListingButton extends FocusPanel{
	Image image = new Image(Resources.INSTANCE.addNewListingButton());
	
	public AddNewListingButton() {
		super();
		this.addStyleName(Resources.INSTANCE.style().bpButton());
		
		
		this.setWidget(image);
		setTitle("Add a New Book Listing");
	}
}
