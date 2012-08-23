package com.bookspicker.test.server.social;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.bookspicker.server.data.FriendshipManager;
import com.bookspicker.shared.Friendship;

public class FriendshipTest extends TestCase {
	
	@Override
	public void setUp() {
//		FriendshipManager.getManager().createAndSaveFriendship("1", "2");
//		FriendshipManager.getManager().createAndSaveFriendship("1", "3");
//		FriendshipManager.getManager().createAndSaveFriendship("2", "3");
//		FriendshipManager.getManager().createAndSaveFriendship("2", "4");
	}
	
	@Test
	public void testLoadAllFriendships() {
		List<Friendship> friends = FriendshipManager.getManager().getAllFriendships();
		System.out.println(friends.size());
	}

}