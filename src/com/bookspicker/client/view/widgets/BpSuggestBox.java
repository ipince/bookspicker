package com.bookspicker.client.view.widgets;

import com.google.gwt.user.client.ui.SuggestBox;

public class BpSuggestBox extends SuggestBox {
    private String savedQuery = "";
    
    public BpSuggestBox(BpOracle oracle, SuggestionTextBox box) {
        super(oracle, box);
    }

    @Override
    public void showSuggestionList() {
        cleanSavedQuery();
        super.showSuggestionList();
      }
    
    @Override
    public String getText() {
        if (savedQuery != "") {
            return savedQuery;
        } else {
            return super.getText();
        }
    }
    
    public String getDisplayedQuery() {
        return super.getText();
    }
    
    public String getSavedQuery() {
        return savedQuery;
    }

    public void setSavedQuery(String savedQuery) {
        this.savedQuery = savedQuery;
    }
    public void cleanSavedQuery() {
        savedQuery = "";
    }
}
