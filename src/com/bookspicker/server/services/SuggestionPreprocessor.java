package com.bookspicker.server.services;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class SuggestionPreprocessor implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// do nothing
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		SuggestionServiceImpl.preprocessClasses();
	}

}
