package com.bookspicker.shared;

import java.io.Serializable;
import java.util.Arrays;

import com.google.gwt.user.client.rpc.IsSerializable;

// TODO(Rodrigo) - Add Null checker based on the output the hibernate provides.
// TODO(rodrigo): add database constraints (e.g., isbn must be unique)
// TODO(rodrigo): TEST database constraints

public class Book implements Item, IsSerializable, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String title;
	private String[] authorList;
	private String isbn = "";
	private String ean = ""; // EAN is ISBN-13
	private Integer listPrice;
	private String imageUrl = "";
	private Integer edition;
	private String publisher;
	
	public Book(String title, String[] authorList, String isbn,
			String ean, int listPrice, String imageUrl, int edition, String publisher) {
		
		if (isbn == null && title == null) {
			throw new IllegalArgumentException("Book doesn't have a title nor isbn");
		}
		this.title = title;
		this.isbn = convertToIsbn10(isbn);
		if (ean != null) {
			this.ean = ean;
		} else {
			ean = "";
		}

		if (authorList == null) {
			this.authorList = new String[0]; 
		} else { 
			this.authorList = authorList;
			Arrays.sort(this.authorList);
		}
		if (ean == null) {
			ean = "";
		} else { 
			this.ean = ean;
		}
		setImageUrl(imageUrl);

		if (publisher == null) {
			this.publisher = "";
		} else {
			this.publisher = publisher;
		}
		this.listPrice = listPrice;
		this.edition = edition;
	} 

	public Book() {
		// For Hibernate and GWT-RPC
	}
	
	@Override
	public Book getBook() { // For Item interface
		return this;
	}
	
	public Long getId() {
		return id;
	}
	@SuppressWarnings("unused")
	private void setId(Long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String[] getAuthorList() {
		return authorList;
	}
	public void setAuthorList(String[] authorList) {
		this.authorList = authorList;
	}
	public String getIsbn() {
		return isbn;
	}
	public void setIsbn(String isbn) {
		this.isbn = convertToIsbn10(isbn);
	}
	public String getEan() {
		return ean;
	}
	public void setEan(String ean) {
		this.ean = ean;
	}
	public Integer getListPrice() {
		return listPrice;
	}
	public void setListPrice(Integer listPrice) {
		this.listPrice = listPrice;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = (imageUrl == null ? "" : imageUrl);
	}
	public Integer getEdition() {
		return edition;
	}
	public void setEdition(Integer edition) {
		this.edition = edition;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	
	/**
	 * If the isbn is a 13-digit ISBN, returns its 10-digit
	 * counterpart. The string must be a valid ISBN.
	 */
	private String convertToIsbn10(String isbn) {
		if (isbn == null)
			return isbn;
		if (!isbn.matches(Constants.ISBN_REGEX_STR)) {
			System.err.println("ISBN not match: " + isbn);
		}
		if (isbn.matches(Constants.ISBN_REGEX_STR) &&
				isbn.length() == 13 && isbn.startsWith("978")) {
			return isbn.substring(3);
		}
		return isbn;
	}

	@Override
	public int hashCode() {
		int result = 1;
		result = 7 * result + Arrays.hashCode(authorList);
//		result = 11 * result + ((id == null) ? 0 : id.hashCode());
		result = 13 * result + ((ean == null) ? 0 : ean.hashCode());
		result = 17 * result + edition;
		result = 19 * result + ((imageUrl == null) ? 0 : imageUrl.hashCode());
		result = 23 * result + ((isbn == null) ? 0 : isbn.hashCode());
		long temp = listPrice.longValue();
		result = 29 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + ((publisher == null) ? 0 : publisher.hashCode());
		result = 37 * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}
	
	@Override
	// Assumes that this's fields are non-null
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj instanceof Book) {
			Book other = (Book) obj;
			if (id != null && other.id != null && (id.equals(other.id))) {
				return true;
			}
			if (title.equals(other.title) &&
					Arrays.equals(authorList, other.authorList) &&
					(isbn == null || isbn.equals(other.isbn)) && 
					(ean == null || ean.equals(other.ean)) &&
					listPrice.equals(other.listPrice) && 
					(imageUrl == null || imageUrl.equals(other.imageUrl)) && // TODO: remove null part (only there because we de-serialized objects that were serialized when null was a possibility
					edition.equals(other.edition) && publisher.equals(other.publisher))
				return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Title: " + title);
		sb.append("\nAuthors: " + Arrays.toString(authorList));
		sb.append("\nISBN: " + isbn);
		sb.append("\nEAN: " + ean);
		sb.append("\nPrice: " + listPrice);
		sb.append("\nImage: " + imageUrl);
		sb.append("\nEdition: " + edition);
		sb.append("\nPublisher: " + publisher);
		return sb.toString();
	}
	
	public String toRowString(String sep) {
		StringBuilder sb = new StringBuilder();
		sb.append(title + sep);
		sb.append(Arrays.toString(authorList) + sep);
		sb.append(isbn + sep);
		sb.append(ean + sep);
		sb.append(listPrice + sep);
		sb.append(imageUrl + sep);
		sb.append(edition + sep);
		sb.append(publisher);
		return sb.toString();
	}
}