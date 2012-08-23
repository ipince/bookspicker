package com.bookspicker.server.data;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.classic.Session;

import com.bookspicker.shared.Book;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.School;
import com.bookspicker.shared.User;

public class LocalOfferManager {
	
	private static final LocalOfferManager MANAGER = new LocalOfferManager();
	
	private LocalOfferManager() {}
	
	public static LocalOfferManager getManager() {
		return MANAGER;
	}
	
	public List<LocalOffer> getAllLocalOffers() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		List<LocalOffer> offers = session.createQuery("from LocalOffer").list();
		session.getTransaction().commit();
		session.close();
		
		return offers;
	}
	
	/**
	 * 
	 * @param id
	 * @return null if it wasn't found on the database
	 */
	public LocalOffer getLocalOfferWithId(Long id) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		List offerList = session.createQuery("from LocalOffer where id='" + id + "'").list();
		session.getTransaction().commit();
		
		// Grab the offer from the list
		LocalOffer offer = null;
		if (!offerList.isEmpty()) {
			assert offerList.size() == 1; // TODO: enforce
			offer = (LocalOffer) offerList.get(0);
		}
		
		session.close();
		
		return offer;
	}
	
	/**
	 * 
	 * @return null if it there aren't active local offers on the db.
	 */
	public List<LocalOffer> getActiveLocalOffers() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		List offerList = session.createQuery("from LocalOffer where active=true").list();
		session.getTransaction().commit();
		session.close();
		return offerList;
	}
	
	public List<LocalOffer> getLocalOffersFor(School school, Book book) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		List<LocalOffer> candidates = session.createQuery("from LocalOffer where book.isbn='" + book.getIsbn() + "'").list();
		
		List<LocalOffer> matches = new ArrayList<LocalOffer>();
		for (LocalOffer offer : candidates) {
			if (school.equals(offer.getSchool())) {
				matches.add(offer);
			}
		}
		
		session.getTransaction().commit();
		session.close();
		return matches;
	}
	
	public List<LocalOffer> getLocalOffersOwnedBy(User user) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		List offerList = session.createQuery("from LocalOffer where owner='" + user.getId() + "'").list();
		session.getTransaction().commit();
		session.close();
		return offerList;
	}
	
	public List<LocalOffer> getActiveLocalOffersForClass(String classCode) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		List offerList = session.createQuery("from LocalOffer where active='1' and classCode='" + classCode + "'").list();
		session.getTransaction().commit();
		session.close();
		return offerList;
	}
	
	public LocalOffer save(LocalOffer offer) {
		// Make sure book is persisted
		if (offer.getBook().getId() == null) {
			Book pBook = BookManager.getManager().getBookByIsbn(offer.getBook().getIsbn());
			if (pBook == null) // save new book
				BookManager.getManager().saveBook(offer.getBook());
			else
				offer.setBook(pBook);
		}
		
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		session.save(offer);
		session.getTransaction().commit();
		session.close();
		return offer;
	}

	public LocalOffer update(LocalOffer offer) {
		// Make sure book is persisted
		if (offer.getBook().getId() == null) {
			Book pBook = BookManager.getManager().getBookByIsbn(offer.getBook().getIsbn());
			if (pBook == null) // save new book
				BookManager.getManager().saveBook(offer.getBook());
		}
		
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		session.update(offer);
		session.getTransaction().commit();
		session.close();
		return offer;
	}

	public void delete(LocalOffer offer) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		session.delete(offer);
		session.getTransaction().commit();
		session.close();
	}

}
