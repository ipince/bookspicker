package com.bookspicker.shared;

/**
 * Represents a Facebook relationship between two users
 * in our system. Friendships are immutable.
 * 
 * @author Rodrigo Ipince
 */
public class Friendship {
	
	private Long id;
	private String primary;
	private String secondary;
	
	public Friendship() {} // For Hibernate
	
	public Friendship(String primary, String secondary) {
		this.primary = primary;
		this.secondary = secondary;
	}
	
	// Getters and setters for hibernate

	public void setId(Long id) {
		this.id = id;
	}
	public Long getId() {
		return id;
	}
	public String getPrimary() {
		return primary;
	}
	public String getSecondary() {
		return secondary;
	}
	
	@Override
	public String toString() {
		return primary + "-" + secondary;
	}

}
