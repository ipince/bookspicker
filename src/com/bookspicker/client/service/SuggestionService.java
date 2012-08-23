package com.bookspicker.client.service;

import java.util.List;

import com.bookspicker.shared.School;
import com.bookspicker.shared.BpOracleSuggestion;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("suggest")
public interface SuggestionService extends RemoteService {
	
	public List<BpOracleSuggestion> getClassSuggestion(School school, String query, int limit);

}
