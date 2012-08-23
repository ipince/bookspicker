package com.bookspicker.server.data.tools;

import java.io.IOException;
import java.util.ArrayList;

import org.htmlcleaner.XPatherException;

import com.bookspicker.server.data.tools.DataUtils.Material;
import com.bookspicker.shared.School;
import com.bookspicker.shared.Term;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

public class DebugSandbox {
	
	public static void main(String[] args) {
		String request = "http://uchicago.bncollege.com/webapp/wcs/stores/servlet/TBListView?storeId=15063&langId=-1&catalogId=10001&savedListAdded=true&clearAll=&viewName=TBWizardView&removeSectionId=&mcEnabled=N&section_1=46227306&numberOfCourseAlready=0&sectionList=newSectionNumber";
		
		Selenium selenium = new DefaultSelenium("localhost", 4444, "*firefox", "http://www.google.com");
		selenium.start();
		
		try {
			Thread.sleep(1000 * 10);
		} catch (InterruptedException e) {
			// do nothing
		}
		
		selenium.open(request);
		try {
			// wait 5 seconds for it to load (js redirect)
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// do nothing
		}
		String html = selenium.getHtmlSource();
		
		MitMaterialFetcher fetcher = new MitMaterialFetcher(School.UCHICAGO, Term.SPRING2011, null);
		try {
			fetcher.buildMaterial(request, html, "asdf", "asfd2", new ArrayList<Material>(), new ArrayList<String>());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPatherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
