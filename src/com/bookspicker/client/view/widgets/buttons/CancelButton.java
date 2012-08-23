package com.bookspicker.client.view.widgets.buttons;

import com.bookspicker.client.view.Resources;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;

public class CancelButton extends FocusPanel{
Image image = new Image(Resources.INSTANCE.cancelButton());
	
	public CancelButton() {
		super();
		this.addStyleName(Resources.INSTANCE.style().bpButton());

		this.setWidget(image);
		setTitle("Cancel");
	}
}
