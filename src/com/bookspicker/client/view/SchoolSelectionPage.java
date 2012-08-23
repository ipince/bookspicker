package com.bookspicker.client.view;

import com.bookspicker.client.BooksPicker;
import com.bookspicker.client.view.Resources.Style;
import com.bookspicker.client.view.widgets.SuggestionTextBox;
import com.bookspicker.shared.School;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class SchoolSelectionPage extends Composite {

	private static SchoolSelectionPageUiBinder uiBinder = GWT
			.create(SchoolSelectionPageUiBinder.class);

	private final Style STYLE = Resources.INSTANCE.style();

	@UiField
	SimplePanel searchBoxWrapper;

	private Grid searchBox = new Grid(1, 3);
	private SuggestionTextBox searchInput;
	
	interface SchoolSelectionPageUiBinder extends
			UiBinder<Widget, SchoolSelectionPage> {
	}

	public SchoolSelectionPage() {
		initWidget(uiBinder.createAndBindUi(this));
		searchInput = new SuggestionTextBox("Search by title, keyword or ISBN...");
		searchInput.setVisibleLength(40);		
		
		searchInput.reset();

		searchInput.addKeyPressHandler(new KeyPressHandler() {
			
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getCharCode() == KeyCodes.KEY_ENTER) {
					DeferredCommand.addCommand(new Command() {
						@Override
						public void execute() {
							Cookies.setCookie("school", School.NONE.getName());
							BooksPicker.genericSearchSelected();
							History.newItem(SearchPage.buildToken(searchInput.getText(), null, null, searchInput.getText()));
						}
					});
				}
			}
		});
		
		searchBox.setWidget(0, 0, searchInput);
		searchBox.setStylePrimaryName(STYLE.selectSchoolSearchBox());

		Image searchButton = new Image(Resources.INSTANCE.searchButtonBlack());
		searchButton.setStylePrimaryName(STYLE.bpButton());
		searchButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				Cookies.setCookie("school", School.NONE.getName());
				History.newItem(SearchPage.buildToken(searchInput.getText(), null, null, searchInput.getText()));
			}
		});
		searchBox.setWidget(0, 1, searchButton);

		//searchBoxWrapper.setStylePrimaryName(STYLE.homeSearchBoxWrapper());
		searchBoxWrapper.setWidget(searchBox);
		searchBoxWrapper.getElement().getStyle().clearBorderWidth();
		searchBoxWrapper.getElement().getStyle().clearBorderColor();
		searchBoxWrapper.getElement().getStyle().clearBorderStyle();
	}

	public void setPreSelectionSearchText(String string) {
		searchInput.setText(string);
	}

}
