package com.bookspicker.test.shared;

import junit.framework.TestCase;

import org.junit.Test;

import com.bookspicker.server.data.BookManager;
import com.bookspicker.shared.Book;

public class BookTest extends TestCase {
	
	@Test
	public void testEquals() {
		Book book = new Book("Test Title",
				new String[]{"First Author", "Second Author"},
				"0123456789", "9780123456789",
				-1, null, -1, "Test Publisher");
		
		Book persistedBook = BookManager.getManager().saveBook(book);
		
		assertTrue("Books were not equal!", book.equals(persistedBook));
	}

}
