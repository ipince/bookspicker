package com.bookspicker.server.data;

import java.util.List;

import org.hibernate.classic.Session;

import com.bookspicker.shared.Stat;

public class StatManager {
	
	private static final StatManager MANAGER = new StatManager();
	
	private StatManager() {}
	
	public static StatManager getManager() {
		return MANAGER;
	}
	
	public List<Stat> getStatsOfType(String type) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		List statList = session.createQuery("from Stat where type='" + type + "'").list();
		session.getTransaction().commit();
		session.close();
		return statList;
	}
	
	public Stat save(Stat stat) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		session.save(stat);
		session.getTransaction().commit();
		session.close();
		return stat;
	}

}
