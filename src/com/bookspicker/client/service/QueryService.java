package com.bookspicker.client.service;

import java.util.List;
import java.util.Map;

import com.bookspicker.shared.Bundle;
import com.bookspicker.shared.Item;
import com.bookspicker.shared.School;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("query")
public interface QueryService extends RemoteService {

	Bundle getOffersForBundle(School school, Bundle oldBundle, boolean includeLocal);

	public List<Item> getBookInfo(School school, String query);
	
	public Map<String, List<Item>> search(School school, List<String> queries);

	List<String> getCurrentClasses();
}
