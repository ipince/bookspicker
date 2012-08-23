package com.bookspicker.client.view.widgets.buttons;

import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.widgets.HasOffer;
import com.bookspicker.shared.LocalOffer;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * Nothing more than a Button that contains a LocalOffer.
 * Used for event-handling of events such as 'Edit offer' or
 * 'Repost offer'.
 * 
 * @author Rodrigo Ipince
 */
public class OfferButton extends FocusPanel implements HasOffer {
	
	private final LocalOffer offer;
	private Image regButton;
	private Image disabledButton;
	private boolean enabled;
	
	public OfferButton(LocalOffer offer, ImageResource reg, ImageResource disabled) {
		super();
		this.addStyleName(Resources.INSTANCE.style().bpButton());
		
		regButton = new Image(reg);
		disabledButton = new Image(disabled);
		
		this.setWidget(regButton);	
		enabled = true;
		this.offer = offer;
	}

	
	public void setEnabled(boolean _enabled){
		if(_enabled){
			this.getElement().getStyle().setCursor(Cursor.POINTER);
			this.setWidget(regButton);
			enabled = true;
		}
		else{
			this.getElement().getStyle().setCursor(Cursor.DEFAULT);
			this.setWidget(disabledButton);
			enabled = false;
		}
	}
	
	@Override
	public boolean isEnabled(){
		return enabled;
	}
	
	@Override
	public LocalOffer getOffer() {
		return offer;
	}


}
