package com.bookspicker.server.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Used as a wrapper around a thread pool of 10 threads.
 * 
 * @author Rodrigo Ipince
 */
public class HelperThreads {
	
	private final static int numThreads = 10;
	
	private final static ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
	
	public static synchronized void execute(Runnable runnable) {
		threadPool.execute(runnable);
	}

}
