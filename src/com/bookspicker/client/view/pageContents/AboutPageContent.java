package com.bookspicker.client.view.pageContents;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class AboutPageContent extends Composite {

	private static AboutPageContentUiBinder uiBinder = GWT
			.create(AboutPageContentUiBinder.class);

	interface AboutPageContentUiBinder extends
			UiBinder<Widget, AboutPageContent> {
	}

	public AboutPageContent() {
		initWidget(uiBinder.createAndBindUi(this));		
	}

}
