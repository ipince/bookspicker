package com.bookspicker.client.view.pageContents;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class FaqPageContent extends Composite {

	private static FaqPageContentUiBinder uiBinder = GWT
			.create(FaqPageContentUiBinder.class);

	interface FaqPageContentUiBinder extends UiBinder<Widget, FaqPageContent> {
	}

	public FaqPageContent() {
		initWidget(uiBinder.createAndBindUi(this));		
	}

}
