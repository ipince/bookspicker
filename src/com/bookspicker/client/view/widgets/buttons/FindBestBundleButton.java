package com.bookspicker.client.view.widgets.buttons;

import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.SearchPage;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;

public class FindBestBundleButton extends FocusPanel{
	Image normalImg = new Image(Resources.INSTANCE.findBestBundleButton());
	Image disabledImg = new Image(Resources.INSTANCE.findBestBundleButtonDisabled());
	
	boolean enabled;
	
	public FindBestBundleButton() {
		super();
		//NOTE: Does not also have bpButton style applied.
		this.setStylePrimaryName(Resources.INSTANCE.style().mainBundleButton());
		addClickHandler(SearchPage.findBestBundleHandler);
		setEnabled(false);
	}
	
	public void setEnabled(boolean enable) {
		if (enable) {
			this.getElement().getStyle().setCursor(Cursor.POINTER);
			this.setWidget(normalImg);
		} else {
			this.getElement().getStyle().clearCursor();
			this.getElement().getStyle().setCursor(Cursor.DEFAULT);
			this.setWidget(disabledImg);
		}
		enabled = enable;
	}

	public boolean isEnabled() {
		return enabled;
	}

}
