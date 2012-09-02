package com.bookspicker.server.data;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static SessionFactory SESSION_FACTORY;

    private enum Config {
        PROD, TEST, WIPE;
    }

    private static final Config CONFIG = Config.WIPE;
    public static boolean testDb = false;
    public static boolean liveTest = false;
    public static boolean wipeData = false;

    private static SessionFactory buildSessionFactory() {
        try {
            switch (CONFIG) {
            case PROD: // real data (unless running locally)
                return new Configuration().configure().buildSessionFactory();
            case TEST: // test data
                return new Configuration().configure("hibernate.test.cfg.xml").buildSessionFactory();
            case WIPE: // re-generate schema
                return new Configuration().configure("hibernate.wipedata.cfg.xml").buildSessionFactory();
            default: // test
                return new Configuration().configure("hibernate.test.cfg.xml").buildSessionFactory();
            }
            /*      // Create the SessionFactory from hibernate config file
        	if (testDb) {
        		if (liveTest) {
        			return new Configuration().configure("hibernate.test.live.cfg.xml").buildSessionFactory();
        		} else {
        			if (wipeData) {
        				// Creates tables from scratch
        				return new Configuration().configure("hibernate.test.wipedata.cfg.xml").buildSessionFactory();
        			} else {
        				// Uses test database (but doesn't destroy current data)
        				return new Configuration().configure("hibernate.test.cfg.xml").buildSessionFactory();
        			}
        		}
        	} else {*/
            // regular hibernate.cfg.xml - used in production!
            //        		return new Configuration().configure().buildSessionFactory();
            //}
        } catch (Throwable ex) {
            // TODO: Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static synchronized SessionFactory getSessionFactory() {
        if (SESSION_FACTORY == null)
            SESSION_FACTORY = buildSessionFactory();
        return SESSION_FACTORY;
    }

}