package com.bookspicker.server.queries;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.bookspicker.shared.Book;
import com.bookspicker.shared.Bundle;
import com.bookspicker.shared.Offer;
import com.bookspicker.shared.OnlineOffer;
import com.bookspicker.shared.Offer.StoreName;

public class BookExQuery implements BookstoreQuery {

	private static final String BOOK_EX_SEARCH_URL = "http://apo.mit.edu/bookex/book/search?isbns=";
	private static final String BOOK_EX_BUY_URL = "http://apo.mit.edu/bookex/book/list?search=";
    private static XmlParsingTools xmlParsingTools = new XmlParsingTools();

	@Override
	public void getBooksOffers(Bundle bundle) {
		if (!bundle.isEmpty()) {
			String request = BOOK_EX_SEARCH_URL + bundle.getIsbnListOfAllBooks();
			Document response = xmlParsingTools.parseXmlToDoc(request);
			
			NodeList rootNode = response.getElementsByTagName("books");
			Element books, book;
			NodeList bookNodes;
			String isbn, condition, title, url;
			int price;
			Offer offer;
			Book bundleBook;
			
			if (rootNode.getLength() == 1) {
				books = (Element) rootNode.item(0);
				bookNodes = books.getElementsByTagName("book");
				
				for (int i = 0; i < bookNodes.getLength(); i++) {
					book = (Element) bookNodes.item(i);
					isbn = xmlParsingTools.getTextValue(book, "isbn");
					bundleBook = bundle.getBookByIsbn(isbn);
					if (bundleBook != null) {
						condition = xmlParsingTools.getTextValue(book, "condition");
//						URLEncoder.
						price = (int) Math.round(xmlParsingTools.getDoubleValue(book, "price") * 100);
						title = xmlParsingTools.getTextValue(book, "name");
						url = BOOK_EX_BUY_URL + title;
						offer = new OnlineOffer(price, 0, StoreName.BOOK_EX, "MIT Student", condition, url);
						bundle.addOffer(bundleBook, offer);
					}
				}
			}
		}
	}

}
