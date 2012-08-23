package com.bookspicker.server.data.tools;

/**
 * Wrapper around two objects of known types. The Pair
 * is immutable.
 */
public class Pair<A, B> {
	private final A first;
	private final B second;
	
	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}
	
	public A getFirst() {
		return first;
	}
	
	public B getSecond() {
		return second;
	}

}
