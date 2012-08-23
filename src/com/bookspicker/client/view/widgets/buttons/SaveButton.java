package com.bookspicker.client.view.widgets.buttons;

import com.bookspicker.client.view.Resources;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;

public class SaveButton extends FocusPanel{
	Image image = new Image(Resources.INSTANCE.saveButton());
	
	public SaveButton() {
		super();
		this.addStyleName(Resources.INSTANCE.style().bpButton());
		
		
		this.setWidget(image);
		setTitle("Save Offer Details");
	}
}
