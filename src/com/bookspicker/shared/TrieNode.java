package com.bookspicker.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * 
 * Original version only worked for Trie. It was later
 * parametrized for any type using generics.
 * 
 * @author Taken from website, modified by Rodrigo Ipince
 *
 */
public class TrieNode<T> implements Comparable<TrieNode<T>> {
	
	private TrieNode<T> parent;
	private Queue<TrieNode<T>> children;
	private boolean isLeaf;	// Quick way to check if any children exist
	private boolean isWord;	// Does this node represent the last character of a word
	private char character;	// The character this node represents
	private T data; // not null iff isWord

	/**
	 * Constructor for top level root node.
	 */
	public TrieNode() {
		children = new PriorityQueue<TrieNode<T>>();
		isLeaf = true;
		isWord = false;
	}

	/**
	 * Constructor for child node.
	 */
	public TrieNode(char character) {
		this();
		this.character = character;
	}

	/**
	 * Adds a word to this node. This method is called recursively and
	 * adds child nodes for each successive letter in the word, therefore
	 * recursive calls will be made with partial words.
	 * @param word the word to add
	 */
	protected void addWord(String word, T data) {
		if (word != null && !word.isEmpty()) {
			isLeaf = false;
			char first = word.charAt(0);
			
			TrieNode<T> child = getNode(first);

			if (child == null) {
				child = new TrieNode<T>(word.charAt(0));
				child.parent = this;
				children.offer(child);
			}

			if (word.length() > 1) {
				child.addWord(word.substring(1), data);
			} else {
				child.isWord = true;
				child.data = data;
			}
		}
	}

	/**
	 * Returns the child TrieNode representing the given char,
	 * or null if no node exists.
	 * @param c
	 * @return
	 */
	protected TrieNode<T> getNode(char c) {
		// Sucky implementation, but ok for small datasets
		for (TrieNode<T> node : children)
			if (node.character == c)
				return node;
		return null;
	}
	
	/**
	 * Returns a List of String objects which are lower in the
	 * hierarchy that this node.
	 * @return
	 */
	protected List<T> getData(int limit) {
		
		// Create a list to return
		List<T> list = new ArrayList<T>();

		// If this node represents a word, add it
		if (isWord) {
			list.add(data);
		}

		// If any children
		if (!isLeaf) {
			Queue<TrieNode<T>> childrenCopy = new PriorityQueue<TrieNode<T>>(children);
			// Add any words belonging to any children
			while(!children.isEmpty()) {
				if (list.size() < limit || limit < 0){
					list.addAll(children.poll().getData(limit - list.size()));
				}
				else
					break;
			}
			children = childrenCopy;
		}
		return list;  
	}

	/**
	 * Gets the String that this node represents.
	 * For example, if this node represents the character t, whose parent
	 * represents the charater a, whose parent represents the character
	 * c, then the String would be "cat".
	 * @return
	 */
	public String toString() {
		if (parent == null) {
			return "";
		} else {
			return parent.toString() + new String(new char[] {character});
		}
	}
	
	@Override
	public int compareTo(TrieNode<T> other) {
		return character - other.character;
	}  
}
