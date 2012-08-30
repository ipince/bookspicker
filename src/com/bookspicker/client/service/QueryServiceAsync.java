package com.bookspicker.client.service;

import java.util.List;
import java.util.Map;

import com.bookspicker.shared.Book;
import com.bookspicker.shared.Bundle;
import com.bookspicker.shared.Item;
import com.bookspicker.shared.Offer;
import com.bookspicker.shared.School;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface QueryServiceAsync {

	void getOffersForBundle(School school, Bundle oldBundle, boolean includeLocal, AsyncCallback<Bundle> callback);
	
	void getOffersForBook(School school, Book book, boolean includeLocal, AsyncCallback<List<Offer>> callback);

	void getBookInfo(School school, String query, AsyncCallback<List<Item>> callback);
	
	void search(School school, List<String> queries, AsyncCallback<Map<String, List<Item>>> callback);
 
	void getCurrentClasses(AsyncCallback<List<String>> callback);
}
