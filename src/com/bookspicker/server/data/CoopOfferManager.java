package com.bookspicker.server.data;

import java.util.List;

import org.hibernate.classic.Session;

import com.bookspicker.shared.Book;
import com.bookspicker.shared.CoopOffer;

public class CoopOfferManager {
	
	private static final CoopOfferManager MANAGER = new CoopOfferManager();
	
	private CoopOfferManager() {}
	
	public static CoopOfferManager getManager() {
		return MANAGER;
	}
	
	public List<CoopOffer> getLocalOffersFor(Book book) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		List offerList = session.createQuery("from CoopOffer where isbn='" + book.getIsbn() + "'").list();
		session.getTransaction().commit();
		session.close();
		return offerList;
	}
	
	public CoopOffer save(CoopOffer offer) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		session.save(offer);
		session.getTransaction().commit();
		session.close();
		return offer;
	}

}
