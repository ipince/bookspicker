package com.bookspicker.client.presenter;

import com.google.gwt.user.client.ui.HasWidgets;

// Borrowed from the GWT MVP tutorial
public abstract interface Presenter {
	
	/**
	 * Instructs the Presenter to present the view.
	 * 
	 * @param container the container that contains this
	 * Presenter
	 */
	public abstract void go(final HasWidgets container);
}
