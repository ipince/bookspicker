package com.bookspicker.server.data;

import java.util.List;

import org.hibernate.Session;

import com.bookspicker.shared.Friendship;

/**
 * Manages interactions between the Friendship class and the
 * database.
 * 
 * @author Rodrigo Ipince
 */
public class FriendshipManager {
	
	private static final FriendshipManager MANAGER = new FriendshipManager();
	
	private FriendshipManager() {} // hide constructor
	
	public static FriendshipManager getManager() {
		return MANAGER;
	}
	
	@SuppressWarnings("unchecked")
	public List<Friendship> getAllFriendships() {
    	Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        List<Friendship> result = session.createQuery("from Friendship").list();
        session.getTransaction().commit();
        return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<Friendship> getFriendshipsInvolving(String fib) {
    	Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        // Note that Friendship relationships are a bijection. Thus,
        // if A-B is in our data, and B is a registered user, then
        // B-A should be in our data too. But that's not necessarily
        // true. Suppose Alice registers when she hadn't met Bob, then
        // they became friends and then Bob registered. In that case,
        // we only have one side of the bijection. Thus, we should check
        // for Frienships where the given fib is either the primary
        // or the secondary subject. Some of them will be redundant, but
        // its up to the Friendship accessor to deal with that.
        List<Friendship> result = session.createQuery("from Friendship where primary='" + fib + "' or secondary='" + fib + "'").list();
        session.getTransaction().commit();
        session.close();
        return result;
	}
	
	public Friendship createAndSaveFriendship(String primary, String secondary) {
		
		// TODO: enforce uniqueness constraints
		Friendship f = new Friendship(primary, secondary);
		
		Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.save(f);
        session.getTransaction().commit();
        session.close();
        
        return f;
	}

}
