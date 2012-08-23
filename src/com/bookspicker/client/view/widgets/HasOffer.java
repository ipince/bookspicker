package com.bookspicker.client.view.widgets;

import com.bookspicker.shared.LocalOffer;
import com.google.gwt.event.dom.client.HasClickHandlers;

public interface HasOffer extends HasClickHandlers {
	
	public LocalOffer getOffer();
	
	public boolean isEnabled();

}
