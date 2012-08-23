package com.bookspicker.client.view.widgets;

import com.bookspicker.client.view.Resources;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class HelpWidget extends Image {

	private final PopupPanel popup;

	public HelpWidget(String message, final boolean constrainWidth) {
		super(Resources.INSTANCE.helpIcon());

		setTitle("What's this?");
		addStyleName(Resources.INSTANCE.style().bpButton());
		this.getElement().getStyle().setVerticalAlign(VerticalAlign.TOP);
		popup = new PopupPanel(true);
		popup.setWidget(new HTML(message));

		addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// Reposition the popup relative to the button
				Widget source = (Widget) event.getSource();
				int left = source.getAbsoluteLeft() + 10;
				int top = source.getAbsoluteTop() + 10;
				popup.setPopupPosition(left, top);

				// Nasty hack to make market conditions text readable...
				if (!constrainWidth) {
					popup.getElement().getStyle().clearWidth();
				} else {
					popup.getElement().getStyle().setWidth(30, Unit.EM);
				}
				popup.show();
			}
		});
	}

}
