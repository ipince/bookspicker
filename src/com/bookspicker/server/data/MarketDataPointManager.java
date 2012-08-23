package com.bookspicker.server.data;

import java.util.List;

import org.hibernate.classic.Session;

import com.bookspicker.server.data.market.MarketDataPoint;

public class MarketDataPointManager {
	
	private static final MarketDataPointManager MANAGER = new MarketDataPointManager();

	private MarketDataPointManager() {}

	public static MarketDataPointManager getManager() {
		return MANAGER;
	}
	
	public List<MarketDataPoint> getDataPointsWithBuyId(String id) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		List<MarketDataPoint> marketData = session.createQuery("from MarketDataPoint where buyClickStatId='" + id + "'").list();
		session.getTransaction().commit();
		session.close();
		
		return marketData;
	}
	
	public MarketDataPoint save(MarketDataPoint mdp) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();
		session.save(mdp);
		session.getTransaction().commit();
		session.close();
		return mdp;
	}

}
