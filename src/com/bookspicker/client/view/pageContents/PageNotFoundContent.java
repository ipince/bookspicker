package com.bookspicker.client.view.pageContents;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class PageNotFoundContent extends Composite {

	private static PageNotFoundContentUiBinder uiBinder = GWT
			.create(PageNotFoundContentUiBinder.class);

	interface PageNotFoundContentUiBinder extends
			UiBinder<Widget, PageNotFoundContent> {
	}

	@UiField
	Button button;

	public PageNotFoundContent() {
		initWidget(uiBinder.createAndBindUi(this));		
	}

}
