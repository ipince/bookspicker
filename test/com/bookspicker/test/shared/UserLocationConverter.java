package com.bookspicker.test.shared;

import java.util.List;

import com.bookspicker.server.data.LocalOfferManager;
import com.bookspicker.server.data.UserManager;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.Location;
import com.bookspicker.shared.User;


public class UserLocationConverter {
	
	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= 111; i++) {
			User user = UserManager.getManager().getUserWithUid(i + "");
			List<LocalOffer> offers = LocalOfferManager.getManager().getLocalOffersOwnedBy(user);
			Location location = null;
			for (LocalOffer offer : offers) {
				if (offer.getLocation() != null)
					location = offer.getLocation();
			}
			if (location != null) {
				if (user.getLocation() != null && user.getLocation() == location) {
					sb.append("Left " + user.getFullName() + "'s location set to " + location.getDisplayName() + "\n");
				} else {
					user.setLocation(location);
					user = UserManager.getManager().updateUser(user);
					sb.append("Set " + user.getFullName() + "'s location to " + location.getDisplayName() + "\n");
				}
			}
		}
		System.out.println(sb.toString());
	}

}
