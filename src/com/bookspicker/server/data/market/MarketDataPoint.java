package com.bookspicker.server.data.market;

import java.util.Date;

import com.bookspicker.shared.Offer;

/**
 * Simple bean that contains basic information about a book
 * offer. Used to save to the database the competing offers when
 * a purchase is made, so that the data can be used later to
 * analyze what students prefer to buy.
 * 
 * @author Rodrigo Ipince
 *
 */
public class MarketDataPoint {
	
	private Long id;
	private Long buyClickStatId;
	public String store;
	public Integer price;
	public String bookCondition;
	private Date date;
	
	private MarketDataPoint() {} // For Hibernate
	
	public MarketDataPoint(Long buyClickStatId, Offer offer, Date date) {
		this.buyClickStatId = buyClickStatId;
		this.store = offer.getStoreName().getName();
		this.price = offer.getTotalPrice();
		this.bookCondition = offer.getCondition();
		this.date = date;
	}

}
