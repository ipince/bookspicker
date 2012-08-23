package com.bookspicker.server.social;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.bookspicker.server.data.FriendshipManager;
import com.bookspicker.server.services.HelperThreads;
import com.bookspicker.shared.Friendship;

/**
 * 
 * @author Rodrigo Ipince
 */
public class SocialGraph implements ServletContextListener {
	
	private static final boolean LOAD_FROM_DB = true;
	private static final Graph<WeightedNode> THE_GRAPH = new Graph<WeightedNode>();
	
	private static final PathFinder<WeightedNode, WeightedNodePath> FINDER = new PathFinder<WeightedNode, WeightedNodePath>();
	
//	public static void main(String[] args) {
//		List<Friendship> friends = FriendshipManager.getManager().getAllFriendships();
//		addFriends(friends);
//		
//		double distance = getSocialDistance("707912", "708316");
//		
//		System.out.println("The social distance is: " + distance);
//	}
	
	public static synchronized void addFriends(List<Friendship> friends) {
		for (Friendship friendship : friends) {
			addFriend(friendship);
		}
	}
	
	public static synchronized void addFriend(Friendship friendship) {
		WeightedNode node1, node2;
		// Make sure nodes are in graph
		node1 = new WeightedNode(friendship.getPrimary(), 1);
		node2 = new WeightedNode(friendship.getSecondary(), 1);
		THE_GRAPH.addNode(node1);
		THE_GRAPH.addNode(node2);
		
		// Add edge between them (bijection!)
		THE_GRAPH.addEdge(node1, node2);
		THE_GRAPH.addEdge(node2, node1);
	}
	
	public static synchronized double getSocialDistance(String fib1, String fib2) {
//		double shortestDistance = getShortestDistance(fib1, fib2);
		double shortestDistance = 3;
		int mutualFriends = countMutualFriends(fib1, fib2);
		if (mutualFriends > 0)
			shortestDistance = 2;
		boolean areFriends = areFriends(fib1, fib2);
		if (areFriends)
			shortestDistance = 1;
		
		if (mutualFriends > 100) // cap at 100
			mutualFriends = 100;
		
		return shortestDistance - (mutualFriends * 1.0 / 100);
	}

	private static synchronized double getShortestDistance(String fib1, String fib2) {
		Set<WeightedNodePath> origin = new HashSet<WeightedNodePath>();
		origin.add(new WeightedNodePath(new WeightedNode(fib1, 1)));		
		Set<WeightedNode> goal = new HashSet<WeightedNode>();
		goal.add(new WeightedNode(fib2, 1));
		
		WeightedNodePath path;
		try {
			path = FINDER.findPath(THE_GRAPH, origin, goal);
			System.out.println(path.toString() + "; cost is " + path.cost());
			return path.cost() - 1;
		} catch (NoSuchElementException e) {
			System.out.println("No path was found!");
			return 10;
		} catch (Exception e) {
			// Can happen if nodes are not in path... This shouldn't happen in
			// theory, but just in case
			System.out.println("An error ocurred finding the path");
			e.printStackTrace();
			return 10;
		}
	}
	
	private static synchronized int countMutualFriends(String fib1, String fib2) {
		WeightedNode first = new WeightedNode(fib1, 1);
		WeightedNode second = new WeightedNode(fib2, 1);
		
		int mutualFriends = 0;
		try {
			Set<WeightedNode> firstFriends = THE_GRAPH.listChildren(first);
			Set<WeightedNode> secondFriends = THE_GRAPH.listChildren(second);
			
			
			for (WeightedNode node1 : firstFriends) {
				for (WeightedNode node2 : secondFriends) {
					if (node1.equals(node2))
						mutualFriends++;
				}
			}
			
		} catch (Exception e) {
			// do nothing
		}
		
		System.out.println("Mutual friends: " + mutualFriends);
		
		return mutualFriends;
	}
	
	private static boolean areFriends(String fib1, String fib2) {
		WeightedNode first = new WeightedNode(fib1, 1);
		WeightedNode second = new WeightedNode(fib2, 1);
		
		try {
			Set<WeightedNode> firstFriends = THE_GRAPH.listChildren(first);
			for (WeightedNode node1 : firstFriends) {
				if (node1.equals(second))
					return true;
			}
		} catch (Exception e) {
			// do nothing
		}
		
		return false;
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// do nothing
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		// Load up friendships from the database
		if (LOAD_FROM_DB) {
			HelperThreads.execute(new Runnable() {
				@Override
				public void run() {
					System.out.println("Retrieving friendships from db");
					long now = System.currentTimeMillis();
					List<Friendship> friendships = FriendshipManager.getManager().getAllFriendships();
					System.out.println("Done getting frienships from db: took " + (System.currentTimeMillis() - now) + "ms");
					addFriends(friendships);
				}
			});
		}
	}

}
