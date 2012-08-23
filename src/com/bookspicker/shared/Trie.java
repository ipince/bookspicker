package com.bookspicker.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

/**
 * Trie to be used to keep suggestions.
 * 
 * @author Taken from website, modified by Rodrigo Ipince
 */
public class Trie<T extends Comparable<T>> {
	
	private TrieNode<T> root;

	/**
	 * Constructor
	 */
	public Trie() {
		root = new TrieNode<T>();
	}

	/**
	 * Adds a word to the Trie
	 * @param word
	 */
	public void addWord(String word, T data) {
		if (word != null && !word.isEmpty()) 
			root.addWord(word.toLowerCase(), data);
	}
	
	public void clear() {
		root = new TrieNode<T>();
	}
	
	/**
	 * Get the data in the Trie with the given prefix
	 * 
	 * @param prefix cannot be null
	 * @return a List containing String objects containing the words in
	 *         the Trie with the given prefix.
	 */
	public List<T> getData(String prefix, int limit) {
		// Find the node which represents the last letter of the prefix
		TrieNode<T> lastNode = root;
//		lastNode.sortChildren();
		prefix = prefix.toLowerCase();
		for (int i = 0; i < prefix.length(); i++) {
			lastNode = lastNode.getNode(prefix.charAt(i));
//			lastNode.sortChildren();
			// If no node matches, then no words exist, 
			// return empty list
			if (lastNode == null) return new ArrayList<T>();	 
		}

		//Return the words which eminate from the last node
		List<T> list = lastNode.getData(limit);
		return list;
	}
}
