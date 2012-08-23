package com.bookspicker.client.service;

import java.util.List;

import com.bookspicker.shared.School;
import com.bookspicker.shared.BpOracleSuggestion;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SuggestionServiceAsync {

	void getClassSuggestion(School school, String query, int limit,
			AsyncCallback<List<BpOracleSuggestion>> callback);

}
