package com.bookspicker.server.data.tools;

import java.util.ArrayList;
import java.util.Arrays;

import com.bookspicker.server.queries.AmazonQuery;

public class Sandbox {

	public static void main(String[] args) {
		AmazonQuery amazon = new AmazonQuery();
		System.out.println(amazon.getBooksInfoByIsbn(Arrays.asList("9780307763860", "9781558607354")));
		}
}
