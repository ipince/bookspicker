package com.bookspicker.server.data;

import java.util.List;

import org.hibernate.classic.Session;

import com.bookspicker.shared.Transaction;

public class TransactionManager {
	
	private static final TransactionManager MANAGER = new TransactionManager();
	
	private TransactionManager() {}
	
	public static TransactionManager getManager() {
		return MANAGER;
	}
	
	public List<Transaction> getTransactions(Long buyerId, String isbn) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		List statList = session.createQuery("from Transaction where buyerId='" + buyerId.toString() + "' and isbn='" + isbn + "'").list();
		session.getTransaction().commit();
		session.close();
		return statList;
	}
	
	public Transaction save(Transaction trans) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		session.save(trans);
		session.getTransaction().commit();
		session.close();
		return trans;
	}

}
