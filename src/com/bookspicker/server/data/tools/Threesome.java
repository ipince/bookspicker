package com.bookspicker.server.data.tools;

/**
 * Wrapper around 3 objects of known types.
 * Threesomes are immutable (and fun!).
 */
public class Threesome<A, B, C> {
	private final A first;
	private final B second;
	private final C third;
	
	public Threesome(A first, B second, C third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}
	
	public A getFirst() {
		return first;
	}
	
	public B getSecond() {
		return second;
	}
	
	public C getThird() {
		return third;
	}

}
