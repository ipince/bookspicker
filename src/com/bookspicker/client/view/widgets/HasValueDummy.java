package com.bookspicker.client.view.widgets;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;

/**
 * Serves as a dummy wrapper around the HasValue interface.
 * 
 * @author Rodrigo Ipince
 */
public abstract class HasValueDummy<V> implements HasValue<V> {
	
	@Override
	public void setValue(V value, boolean fireEvents) {
		setValue(value);
		// no events;
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<V> handler) {
		return new HandlerRegistration() {
			@Override
			public void removeHandler() { // do nothing
			}
		};
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		// do nothing
	}

}
