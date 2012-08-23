package com.bookspicker.server.queries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Serves as a wrapper to perform Facebook Graph API calls. Learn more about the
 * Facebook API here:
 * 
 * http://developers.facebook.com/docs/api
 * 
 * @author Rodrigo Ipince
 */
public class FacebookGraphApi {

	public static final String FB_APP_ID = "100580116660807";
	public static final String FB_APP_SECRET = "159795e574ccf188941326e355a55ff1";
	public static final String FB_OPEN_GRAPH_URL = "https://graph.facebook.com/";
	public static final String FB_AUTH_URL = "https://graph.facebook.com/oauth/access_token";

	public static JSONObject getPublicInformation(String accessToken) {
		return fetchJson(FB_OPEN_GRAPH_URL + "me?access_token=" + accessToken);
	}

	public static List<String> getFriendList(String accessToken) {
		List<String> friends = new ArrayList<String>();

		JSONObject json = fetchJson(FB_OPEN_GRAPH_URL + "me/friends?access_token=" + accessToken);
		if (json != null) {
			try {
				JSONArray friendArray = json.getJSONArray("data");
				for (int i = 0; i < friendArray.length(); i++) {
					friends.add(friendArray.getJSONObject(i).getString("id"));
				}
				System.out.println("Parsed " + friends.size() + " friends");
				return friends;
			} catch (JSONException e) {
				System.out.println("Error parsing json object: " + json.toString());
			}
		}
		
		// Failed for some reason
		return null;
	}

	private static JSONObject fetchJson(String stringUrl) {
		try {
			URL url = new URL(stringUrl);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuilder jsonStr = new StringBuilder();
			String tmp;
			while ((tmp = reader.readLine()) != null) {
				jsonStr.append(tmp);
			}
			System.out.println("Json is: " + jsonStr.toString());
			JSONObject json = new JSONObject(jsonStr.toString());
			return json;
		} catch (IOException ioe) {
			System.out.println("Error fetching url: " + stringUrl);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null; // if failed
	}

}