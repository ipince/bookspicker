package com.bookspicker.server.services;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bookspicker.client.HistoryToken;

@SuppressWarnings("serial")
public class RedirectServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String href = req.getParameter("href");
		System.out.println("Redirecting to: " + href);
		
		if (href == null) {
			href = HistoryToken.HOME;
		}
		
		// TODO: this is OK, but not good enough. What if the
		// redirect was ftp, for example?
		if (!href.startsWith("http")) {
			href = "#" + href;
		}
		
		// Do some stuff
		
		resp.sendRedirect(href);
	}

}
