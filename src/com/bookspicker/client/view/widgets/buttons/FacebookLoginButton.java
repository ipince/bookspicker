package com.bookspicker.client.view.widgets.buttons;

import com.bookspicker.client.view.Resources;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;

public class FacebookLoginButton extends FocusPanel {
	
	public FacebookLoginButton(ImageResource imageResource) {
		super();
		setWidget(new Image(imageResource));
		setTitle("Login with Facebook");
		setStylePrimaryName(Resources.INSTANCE.style().bpButton());
	}

}
