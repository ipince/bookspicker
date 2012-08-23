package com.bookspicker.server.data.tools;

import java.util.concurrent.BlockingQueue;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import com.zenkey.net.prowser.Request;

/**
 * Fetches a URL provided to it through the input queue and
 * writes the response to the output queue. These queues are
 * shared across threads.
 *
 */
public class SeleniumAsyncFetcher implements Runnable {

	private Selenium selenium;
	private final BlockingQueue<RequestWrapper> input;
	private final BlockingQueue<Pair<RequestWrapper, String>> output;
	
	public SeleniumAsyncFetcher(BlockingQueue<RequestWrapper> input,
			BlockingQueue<Pair<RequestWrapper, String>> output) {
		this.input = input;
		this.output = output;
		resetSelenium();
	}
	
	private void resetSelenium() {
		if (selenium != null)
			selenium.stop();
		selenium = new DefaultSelenium("localhost", 4444, "*firefox", "http://www.google.com");
		selenium.start();
		try {
			Thread.sleep(1000 * 10); // wait 10 secs for it to load
		} catch (InterruptedException e) {
			// do nothing
		}
	}
	
	@Override
	public void run() {
		RequestWrapper in;
		while (true) {
			try {
				DataUtils.sleep(); // wait a lil random time
				in = input.take();
				String response = fetch(in.getRequest());
				output.put(new Pair<RequestWrapper, String>(in, response));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String fetch(Request req) {
		boolean success = false;
		String response = null;
		int timesFailed = 0;
		while (!success) {
			try {
				System.out.println("Fetching: " + req.getUri().toString());
				selenium.open(req.getUri().toString());
				try {
					// wait 5 seconds for it to load (js redirect)
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// do nothing
				}
				response = selenium.getHtmlSource();
				if (!selenium.isTextPresent("Problem loading page") &&
					response != null && !response.isEmpty())
					success = true;
			} catch (SeleniumException se) {
				// nothing
			}
			
			if (!success) {
				// failed...
				if (timesFailed >= 5) {
					// restart selenium
					System.err.println("Too many failures. Restarting Selenium session");
					resetSelenium();
					timesFailed = 0;
				} else { // wait 30 seconds and try again
					timesFailed++;
					System.err.println("Error fetching: " + req.getUri().toString() + ". Waiting 30 seconds and trying again");
					try {
						Thread.sleep(1000 * 30);
					} catch (InterruptedException ie) {
						// do nothing
					}
				}
			}
		}
		return response;
	}
}
