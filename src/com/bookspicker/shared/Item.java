package com.bookspicker.shared;

/**
 * Used as a super-interface for Book and ClassBook, so that we can return
 * a List of Items, which the client can then display appropriately.
 */
public interface Item {
	
	public Book getBook();
	
}
