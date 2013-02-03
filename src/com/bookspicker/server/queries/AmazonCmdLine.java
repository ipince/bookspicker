package com.bookspicker.server.queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.bookspicker.shared.Book;

public class AmazonCmdLine {
	
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		AmazonQuery amazon = new AmazonQuery();
		String query;
		while ((query = scanner.next()) != null) {
			List<String> isbnList = new ArrayList<String>();
            isbnList.add(query);
			List<Book> results = amazon.getBooksInfoByIsbn(isbnList);
			if (results.isEmpty()) {
				System.out.println("No results!");
			} else {
				for (Book book : results) {
					System.out.println(book);
				}
			}
		}
	}
}
