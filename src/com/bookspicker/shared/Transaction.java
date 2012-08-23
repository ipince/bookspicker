package com.bookspicker.shared;

import java.util.Date;

public class Transaction {
	
	public static final int WAIT_HOURS = 6;
	public static final long WAIT_TIME = 1000 * 60 * 60 * WAIT_HOURS;
	
	private Long id;
	private Long buyerId;
	private String isbn;
	private Date time;
	
	public Transaction() {}
	
	public Transaction(Long buyerId, String isbn) {
		this.buyerId = buyerId;
		this.isbn = isbn;
		this.time = new Date();
	}
	
	public Date getTime() {
		return time;
	}

}
