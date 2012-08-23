package com.bookspicker.server.data.tools;

import com.zenkey.net.prowser.Request;

public class RequestWrapper extends Threesome<String, String, Request> {
	
	public RequestWrapper(String section, String sectionId, Request req) {
		super(section, sectionId, req);
	}
	
	public String getSection() {
		return getFirst();
	}
	
	public String getSectionId() {
		return getSecond();
	}
	
	public Request getRequest() {
		return getThird();
	}

}
