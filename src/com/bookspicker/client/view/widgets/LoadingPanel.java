package com.bookspicker.client.view.widgets;

import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.Resources.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;

public class LoadingPanel extends FlowPanel {

	private final Style STYLE = Resources.INSTANCE.style();

	public LoadingPanel(String message) {
		super();
		this.add(new Image(Resources.INSTANCE.loadingIcon()));
		this.add(new HTML(message));
		setStylePrimaryName(STYLE.loadingPanel());
	}
}
