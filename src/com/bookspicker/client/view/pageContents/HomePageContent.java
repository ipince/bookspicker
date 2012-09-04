package com.bookspicker.client.view.pageContents;

import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.Resources.Style;
import com.bookspicker.client.view.SearchPage;
import com.bookspicker.client.view.widgets.BpOracle;
import com.bookspicker.client.view.widgets.BpSuggestBox;
import com.bookspicker.client.view.widgets.SuggestionTextBox;
import com.bookspicker.shared.BpOracleSuggestion;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;

public class HomePageContent extends Composite {

	private static HomePageContentUiBinder uiBinder = GWT
			.create(HomePageContentUiBinder.class);

	interface HomePageContentUiBinder extends UiBinder<Widget, HomePageContent> {

	}

	private final Style STYLE = Resources.INSTANCE.style();

	@UiField
	SimplePanel searchBoxWrapper;

	private Grid searchBox = new Grid(1, 3);
	private static SuggestionTextBox searchInput;
	private final static String classSuggestionString = "Search by course, title, keyword, or ISBN...";
	private final static String genericSuggestionString = "Search by title, keyword, or ISBN...";
	private BpSuggestBox autoCompleteInput;
	public Image loadingIcon;

	public HomePageContent() {
		initWidget(uiBinder.createAndBindUi(this));
		searchInput = new SuggestionTextBox(classSuggestionString);
		searchInput.setVisibleLength(40);
		
		autoCompleteInput = new BpSuggestBox(BpOracle.getInstance(), searchInput);
		autoCompleteInput.addSelectionHandler(new SelectionHandler<Suggestion>() {

            @Override
            public void onSelection(
                    SelectionEvent<Suggestion> event) {
                if (event.getSelectedItem() instanceof BpOracleSuggestion) {
                    BpOracleSuggestion bpSuggestion = (BpOracleSuggestion) event.getSelectedItem();
                    autoCompleteInput.setSavedQuery(bpSuggestion.getQueryString());
                }
            }
        });
		
		autoCompleteInput.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                autoCompleteInput.cleanSavedQuery();
            }
        });

		
		
		searchInput.reset();
		autoCompleteInput.setLimit(8);
		autoCompleteInput.setWidth("30em");
		autoCompleteInput.setHeight("25px");
		autoCompleteInput.setAutoSelectEnabled(true);
		
		SimpleSearchHandler searchHandler = new SimpleSearchHandler();

		searchInput.addKeyPressHandler(searchHandler);
		searchInput.addKeyDownHandler(searchHandler);
		autoCompleteInput.addSelectionHandler(searchHandler);
		
		searchBox.setWidget(0, 0, autoCompleteInput);
		searchBox.setStylePrimaryName(STYLE.searchBox());

		Image searchButton = new Image(Resources.INSTANCE.searchButton());
		searchButton.setStylePrimaryName(STYLE.bpButton());
		searchButton.addClickHandler(searchHandler);
		searchBox.setWidget(0, 1, searchButton);

		searchBoxWrapper.setStylePrimaryName(STYLE.homeSearchBoxWrapper());
		searchBoxWrapper.setWidget(searchBox);
		searchBoxWrapper.getElement().getStyle().clearBorderWidth();
		searchBoxWrapper.getElement().getStyle().clearBorderColor();
		searchBoxWrapper.getElement().getStyle().clearBorderStyle();
	}
	
	static public void setGeneric(boolean isGeneric){
		if(isGeneric){
			searchInput.setSuggestionText(genericSuggestionString);
		}
		else{
			searchInput.setSuggestionText(classSuggestionString);
		}
	}
	
	private class SimpleSearchHandler implements ClickHandler, KeyPressHandler, KeyDownHandler, SelectionHandler<Suggestion> {
		
		@Override
		public void onClick(ClickEvent event) {
			sendQuery(autoCompleteInput.getText());
		}

		@Override
		public void onKeyPress(KeyPressEvent event) {
			if (event.getCharCode() == KeyCodes.KEY_ENTER) {
				DeferredCommand.addCommand(new Command() {
					@Override
					public void execute() {
						sendQuery(autoCompleteInput.getText());
					}
				});
			}
		}
		
		@Override
		public void onKeyDown(KeyDownEvent event) {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
				if (((BpSuggestBox)autoCompleteInput).isSuggestionListShowing())
					((BpSuggestBox)autoCompleteInput).hideSuggestionList();
			}
		}
		
		@Override
		public void onSelection(SelectionEvent<Suggestion> event) {
			sendQuery(autoCompleteInput.getText());
		}

		private void sendQuery(String query) {
			History.newItem(SearchPage.buildToken(query, null, null, autoCompleteInput.getDisplayedQuery()));
		}
	}

}
