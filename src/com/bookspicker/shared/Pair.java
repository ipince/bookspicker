package com.bookspicker.shared;

import java.io.Serializable;

/**
 * Simple bean that contains two objects.
 * 
 * @author Rodrigo Ipince
 */
@SuppressWarnings("serial")
public class Pair<T, V> implements Serializable {
	
	private T first;
	private V second;
	
	public Pair() {} // For GWT
	
	public Pair(T first, V second) {
		this.first = first;
		this.second = second;
	}
	
	public T getFirst() {
		return first;
	}
	
	public V getSecond() {
		return second;
	}

}
