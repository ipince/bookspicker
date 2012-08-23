package com.bookspicker.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class User implements IsSerializable {
	
	private Long id; // uid for bookspicker purposes
	private String fib; // facebook id
	
	private String name; // req
	private String fbEmail; // req
	private String mitEmail; // req
	
	private Location location; // optional
	
	// transient (not persisted)
	private List<String> friends = new ArrayList<String>(); // list of friend's fids
	private List<LocalOffer> offers = new ArrayList<LocalOffer>();
	
	public User() {}; // For GWT-RPC
	
	public User(String fib, String name) {
		this.setFib(fib);
		this.name = name;
	}

	public String getName() {
		String[] names = name.split(" ");
		if (names.length > 0)
			return names[0];
		return name;
	}

	public void setFib(String fib) {
		this.fib = fib;
	}

	public String getFib() {
		return fib;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setFbEmail(String fbEmail) {
		this.fbEmail = fbEmail;
	}

	public String getFbEmail() {
		return fbEmail;
	}

	public void setMitEmail(String mitEmail) {
		this.mitEmail = mitEmail;
	}

	public String getMitEmail() {
		return mitEmail;
	}

	public void setFriends(List<String> friends) {
		this.friends = friends;
	}

	public List<String> getFriends() {
		return friends;
	}

	public boolean isAdmin() {
		return "707912".equals(fib);
	}
	
	public void addLocalOffer(Offer offer) {
		getOffers().add(offer);
	}

	public void setOffers(List<LocalOffer> offers) {
		this.offers = offers;
	}

	public List<Offer> getOffers() {
		return new ArrayList<Offer>(offers);
	}
	
	public List<LocalOffer> getLocalOffers() {
		return offers;
	}
	
	public void clearOffers() {
		offers.clear();
	}
	
	public String getFullName() {
		return name;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}

}
