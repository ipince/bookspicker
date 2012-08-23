package com.bookspicker.server.bundling;

import java.util.HashSet;
import java.util.Set;

import com.bookspicker.shared.Offer.StoreName;

public class Filter {
	
	// ============= Simple, user chosen offers ============
	
	// TODO(rodrigo): talk to others to change the Offer class
	// so that it has a reference to the Book. So here we can
	// keep the user chosen offers, instead of having the info
	// live inside the Offer class.
	
	// =============  Location-based filter  ===============
	private static double MAX_DISTANCE = 10000; // 4x size of US
	private double maxDistance; // in miles
	private boolean distanceSetByUser = false;
	
	/**
	 * Sets the maximum distance for an offer to be enabled.
	 * @param distance
	 */
	public void setMaxDistance(double distance) {
		if (distance > MAX_DISTANCE)
			distance = MAX_DISTANCE;
		maxDistance = distance;
		distanceSetByUser = true;
	}
	
	public void resetDistance() {
		maxDistance = MAX_DISTANCE;
		distanceSetByUser = false;
	}
	
	public double getMaxDistance() {
		return maxDistance;
	}
	
	// ================  Which stores  =====================
	private Set<StoreName> disabledStores = new HashSet<StoreName>();
	
	public void disableStore(StoreName store) {
		disabledStores.add(store);
	}
	
	public void enableStore(StoreName store) {
		disabledStores.remove(store);
	}
	
	public Set<StoreName> getDisabledStores() {
		return disabledStores;
	}
	
	// =========  Condition (used, new, rental)  =============
	
	// ==============  Number of stores  =====================
	
	
	/**
	 * Determines if the filter is 'meaningful' in the sense
	 * that it actually might change the optimal bundle.
	 */
	public boolean isMeaningful() {
		return !disabledStores.isEmpty() || distanceSetByUser;
	}

}
