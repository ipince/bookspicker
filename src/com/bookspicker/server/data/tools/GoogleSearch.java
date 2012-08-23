package com.bookspicker.server.data.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

public class GoogleSearch {

	private static String SEARCH_URL = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";

	/**
	 * Returns the estimated result count from running a Google
	 * search for the provided query.
	 * 
	 * @param query the String to search for on Google
	 * @return i where i is the estimated result count, or -1
	 * if the search fails
	 */
	public static int getSearchResultCount(String query) {
		int resultCount = -1;
		
		try {
			String urlStr = SEARCH_URL + URLEncoder.encode(query, "UTF-8");
			URL url = new URL(urlStr);
			InputStream input = url.openConnection().getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			String str;
			StringBuilder sb = new StringBuilder();
			while ((str = reader.readLine()) != null) {
				sb.append(str);
			}
			
			JSONObject json = new JSONObject(sb.toString());
			resultCount = Integer.valueOf(json.getJSONObject("responseData")
					.getJSONObject("cursor")
					.get("estimatedResultCount").toString());
			
		} catch (MalformedURLException e) {
			// TODO: log exceptions?
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return resultCount;
	}

}
