package com.bookspicker.server.data;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.classic.Session;

import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.User;

public class UserManager {
	
	private static final UserManager MANAGER = new UserManager();
	
	private UserManager() {}
	
	public static UserManager getManager() {
		return MANAGER;
	}
	
	public List<User> getAllUsers() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		List<User> users = session.createQuery("from User").list();
		session.getTransaction().commit();
		session.close();
		
		return users;
	}
	
	public User getUserWithUid(String id) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		List userList = session.createQuery("from User where id='" + id + "'").list();
		session.getTransaction().commit();
		
		// Grab the user from the list
		User user = null;
		if (!userList.isEmpty()) {
			assert userList.size() == 1; // TODO: enforce
			user = (User) userList.get(0);
		}
		
		// TODO(rodrigo): this seems unnecessary, delete?
		if (user != null) {
			// Since PersistentList is not serializable by
			// GWT, replace by a regular ArrayList
			List<LocalOffer> offers = new ArrayList<LocalOffer>();
			for (LocalOffer offer : user.getLocalOffers())
				offers.add(offer);
			user.setOffers(offers);
		}
		
		session.close();
		
		return user;
	}
	
	public User getUserWithFib(String fib) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		List userList = session.createQuery("from User where fib='" + fib + "'").list();
		session.getTransaction().commit();
		
		// Grab the user from the list
		User user = null;
		if (!userList.isEmpty()) {
			assert userList.size() == 1; // TODO: enforce
			user = (User) userList.get(0);
		}
		
		// TODO(rodrigo): this seems unnecessary, delete?
		if (user != null) {
			// Since PersistentList is not serializable by
			// GWT, replace by a regular ArrayList
			List<LocalOffer> offers = new ArrayList<LocalOffer>();
			for (LocalOffer offer : user.getLocalOffers())
				offers.add(offer);
			user.setOffers(offers);
		}
		
		session.close();
		
		return user;
	}
	
	public User saveUser(User user) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		session.save(user);
		session.getTransaction().commit();
		session.close();
		return user;
	}
	
	public User updateUser(User user) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		session.update(user);
		session.getTransaction().commit();
		session.close();
		return user;
	}

}
