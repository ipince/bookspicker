package com.bookspicker.server.data;

import java.util.List;

import com.bookspicker.shared.User;

public class FeedbackEmailSender {
	
	
	private static final String[] EXCLUDE_EMAILS = new String[]{};
	private static void sendFeedbackEmail() {
		List<User> allUsers = UserManager.getManager().getAllUsers();
		
		for (User user : allUsers) {
			if (!user.getLocalOffers().isEmpty()) {
				// has local offers
				
			}
		}
	}

}
