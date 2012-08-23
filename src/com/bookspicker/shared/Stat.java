package com.bookspicker.shared;

import java.util.Date;

/**
 * Used to gather statistics about the site's usage.
 * 
 * @author Rodrigo Ipince
 */
public class Stat {
	
	// These are the types of stats we gather. The reason they
	// are a String instead of an Enum is largely a hack. When
	// Hibernate saves Enums to the database, it saves them as
	// blobs, so it's not easily searchable. There's clearly a
	// way around it, but I (Rodrigo) haven't figured it out yet.
	public static String BOOK_SEARCH = "BOOK_SEARCH";
	public static String CLASS_SEARCH = "CLASS_SEARCH";
	public static String BUNDLE_SEARCH = "BUNDLE_SEARCH";
	public static String BUY_LINK_CLICK = "BUY_LINK_CLICK";
	public static String ANTI_ABUSE = "ANTI_ABUSE";
	public static String USED_LOCATION = "USED_LOCATION";
	public static String USED_SOCIAL = "USED_SOCIAL";
	public static String USED_CREATION_DATE = "USED_CREATION_DATE";
	public static String PERSONALIZED_NAME = "PERSONALIZED NAME";
	
	private Long id;
	private String type;
	public Date date;
	public String search; // for Search Stats only
	@SuppressWarnings("unused")
	public String isbn; // for Click Stats only
	@SuppressWarnings("unused")
	public String store; // for Click Stats only
	@SuppressWarnings("unused")
	public Integer price; // for Click Stats only
	@SuppressWarnings("unused")
	private String bookCondition; // for Click Stats only
	@SuppressWarnings("unused")
	public String localId;
	@SuppressWarnings("unused") // Hibernate has field access
	private String uid;
	@SuppressWarnings("unused") // Hibernate has field access
	private String ip;
	
	private Stat() {} // For Hibernate
	
	private Stat(String type, String query, String book,
			String store, Integer price, String condition,
			String localId, String uid, String ip) {
		this.type = type;
		this.date = new Date();
		this.search = query;
		this.isbn = book;
		this.store = store;
		this.price = price;
		this.bookCondition = condition;
		this.localId = localId;
		this.uid = uid;
		this.ip = ip;
	}
	
	public static Stat newBookSearchStat(String query, String uid, String ip) {
		return new Stat(BOOK_SEARCH, query, null, null, null, null, null, uid, ip);
	}
	public static Stat newClassSearchStat(String query, String uid, String ip) {
		return new Stat(CLASS_SEARCH, query, null, null, null, null, null, uid, ip);
	}
	public static Stat newBundleStat(String isbns, String uid, String ip) {
		return new Stat(BUNDLE_SEARCH, isbns, null, null, null, null, null, uid, ip);
	}
	public static Stat newBuyLinkStat(String isbn, String store, Integer price, String condition, String localId, String uid, String ip) {
		return new Stat(BUY_LINK_CLICK, null, isbn, store, price, condition, localId, uid, ip);
	}
	public static Stat newAntiAbuseStat(String isbn, String uid, String ip) {
		return new Stat(ANTI_ABUSE, null, isbn, null, null, null, null, uid, ip);
	}
	public static Stat newUsedLocationStat(String localIds, String uid, String ip) {
		return new Stat(USED_LOCATION, null, null, null, null, null, localIds, uid, ip);
	}
	public static Stat newUsedSocialStat(String localIds, String uid, String ip) {
		return new Stat(USED_SOCIAL, null, null, null, null, null, localIds, uid, ip);
	}
	public static Stat newUsedCreationDateStat(String localIds, String uid, String ip) {
		return new Stat(USED_CREATION_DATE, null, null, null, null, null, localIds, uid, ip);
	}
	public static Stat newPersonalizedNameStat(String name, String localId, String uid, String ip) {
		return new Stat(PERSONALIZED_NAME, name, null, null, null, null, localId, uid, ip);
	}
	
	@Override
	public String toString() {
		return type + " " + date.toString() + " :" + search;
	}

	public Long getId() {
		return id;
	}

}
