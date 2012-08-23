package com.bookspicker.client.view.widgets;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

public class ListChangeHandler implements ChangeHandler, KeyDownHandler {

	private final Runnable runnable;
	
	/**
	 * 
	 * @param runnable must be idempotent
	 */
	public ListChangeHandler(Runnable runnable) {
		this.runnable = runnable;
	}
	
	@Override
	public void onChange(ChangeEvent event) {
		handle();
	}

	@Override
	public void onKeyDown(KeyDownEvent event) {
		handle();
	}
	
	private void handle() {
		DeferredCommand.addCommand(new Command() {
			@Override
			public void execute() {
				runnable.run();
			}
		});
	}

}
