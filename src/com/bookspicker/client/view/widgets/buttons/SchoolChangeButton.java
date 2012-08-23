package com.bookspicker.client.view.widgets.buttons;

import com.bookspicker.client.HistoryToken;
import com.bookspicker.client.view.Resources;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;

public class SchoolChangeButton extends FocusPanel{
	public SchoolChangeButton() {
		super();
		setWidget(new Image(Resources.INSTANCE.schoolChangeButton()));
		setTitle("Change which school's books you want us to search for.");
		setStylePrimaryName(Resources.INSTANCE.style().bpButton());
		this.getElement().getStyle().setMarginRight(5, Unit.PX);
		this.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				History.newItem(HistoryToken.SELECT_SCHOOL);
			}
		});
	}
}
