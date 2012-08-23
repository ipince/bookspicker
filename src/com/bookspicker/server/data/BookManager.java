package com.bookspicker.server.data;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.bookspicker.shared.Book;

/**
 * Manages interactions between the Book class and the database.
 * 
 * @author Rodrigo Ipince
 *
 */
public class BookManager {
	
	private static final BookManager MANAGER = new BookManager();
	
	private BookManager() {}
	
	public static BookManager getManager() {
		return MANAGER;
	}

	public List listBooks() {
    	Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        List result = session.createQuery("from Book").list();
        session.getTransaction().commit();
        return result;
	}
	
	public Book getBookByIsbn(String isbn) {
    	Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        Query q;
        if (isbn.length() == 10) {
        	q = session.createQuery("from Book where isbn='" + isbn + "'");
        } else if (isbn.length() == 13) {
        	q = session.createQuery("from Book where ean='" + isbn + "'");
        } else {
        	// Invalid isbn
        	return null;
        }
        List book = q.list();
        session.getTransaction().commit();
        session.close();
        
        if (!book.isEmpty()) {
        	assert book.size() == 1; // TODO: enforce
        	
        	return (Book) book.get(0);
        }
		
		return null;
	}
	
	public Book saveBook(Book book) {
		Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.save(book);
        session.getTransaction().commit();
        session.close();
        return book;
	}

	// TODO: remove
	public Book createAndStoreBook(String isbn, String title) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setAuthorList(new String[]{"turdo", "other"});
        book.setListPrice(1999);
        session.save(book);

        session.getTransaction().commit();
        
        return book;
    }
	
}
