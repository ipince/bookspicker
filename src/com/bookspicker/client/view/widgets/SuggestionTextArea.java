package com.bookspicker.client.view.widgets;

import com.bookspicker.client.view.Resources;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.TextArea;

public class SuggestionTextArea extends TextArea {
	
	private final String suggestion;
	private boolean suggesting; // true iff text is empty

	public SuggestionTextArea(String suggestion) {
		this.suggestion = suggestion;
		SuggestionHandler handler = new SuggestionHandler();
		addFocusHandler(handler);
		addBlurHandler(handler);
		reset();
	}

	@Override
	public String getText() {
		return suggesting ? "" : super.getText();
	}
	
	@Override
	public void setText(String text) {
		super.setText(text);
		removeStyleName(Resources.INSTANCE.style().suggestionBox());
		suggesting = false;
	}
	
	@Override
	public void setValue(String value) {
		super.setValue(value);
		if (value == null || value.isEmpty())
			reset();
	}
	
	public void reset() {
		setText(suggestion);
		addStyleName(Resources.INSTANCE.style().suggestionBox());
		suggesting = true;
	}

	private class SuggestionHandler implements
			FocusHandler, BlurHandler {

		@Override
		public void onBlur(BlurEvent event) {
			if (getText().isEmpty()) {
				reset();
			}
		}

		@Override
		public void onFocus(FocusEvent event) {
			if (suggesting) {
				setText("");
			}
		}
	}
}
