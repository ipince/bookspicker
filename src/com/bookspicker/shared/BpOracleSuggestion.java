package com.bookspicker.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class BpOracleSuggestion implements Suggestion, IsSerializable, Comparable<BpOracleSuggestion>{

	private String displayString;
	private String replacementString;
	private String queryString;
	
	public BpOracleSuggestion() {}; // For GWT
	
	public BpOracleSuggestion(String replacementString, String displayString, String queryString) {
		this.displayString = displayString;
		this.replacementString = replacementString;
		this.setQueryString(queryString);
	}
	
	@Override
	public String getDisplayString() {
		return displayString;
	}

	@Override
	public String getReplacementString() {
		return replacementString;
	}

	@Override
	public int compareTo(BpOracleSuggestion o) {
//		if (replacementString.length() == o.replacementString.length()) {
			return replacementString.compareTo(o.replacementString);
//		} else {
//			return replacementString.length() - o.replacementString.length();
//		}
	}

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getQueryString() {
        return queryString;
    }

}
