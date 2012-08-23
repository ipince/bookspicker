package com.bookspicker.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

//TODO(Rodrigo) - Code review
//TODO(Jonathan): add support for older editions (provide books' id) + onlineURL.

public class ClassBook implements Item, IsSerializable {

	public static enum Necessity {      
		REQUIRED, RECOMMENDED, RESERVE_ONLY, UNKNOWN;
	}
	
	public static enum Source {
		MIT, THE_COOP, LOCAL, MANUAL;
		
		public String getDisplayName() {
			switch (this) {
			case MIT: return "MIT";
			case THE_COOP: return "The Coop";
			case LOCAL: return "Local sales (unverified)";
			case MANUAL: return "Manually added (unverified)";
			}
			return "Error"; // should never happen
		}
		
		public String getDescription() {
			switch (this) {
			case MIT: return " (according to your school)";
			case THE_COOP: return " (according to your bookstore)";
			case LOCAL: return " (according to a submission to our local marketplace (unverified))";
			case MANUAL: return " (according to a manual submission to BooksPicker (unverified))";
			}
			return "Error"; // should never happen
		}
	}

	private Long id;
	private Book book;
	private SchoolClass schoolClass;
	private Necessity necessity;
	private String notes = "";
	private Source source;

	@SuppressWarnings("unused")
	private ClassBook() {
		// For Hibernate
	}

	public ClassBook(SchoolClass schoolClass, Book book, Necessity necessity, Source source) {
		this(schoolClass, book, necessity, source, "");
	}
	
	public ClassBook(SchoolClass schoolClass, Book book, Necessity necessity, Source source, String notes) {
		setBook(book);
		setSchoolClass(schoolClass);
		setNecessity(necessity);
		setSource(source);
		setNotes(notes);
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Override
	public Book getBook() {
		return book;
	}
	public void setBook(Book book) {
		this.book = book;
	}
	public void setSchoolClass(SchoolClass schoolClass) {
		this.schoolClass = schoolClass;
	}
	public SchoolClass getSchoolClass() {
		return schoolClass;
	}
	public Necessity getNecessity() {
		return necessity;
	}
	public void setNecessity(Necessity necessity) {
		this.necessity = necessity;
	}
	public void setNotes(String notes) {
		this.notes = notes == null ? "" : notes;
	}
	public String getNotes() {
		return notes;
	}
	public void setSource(Source source) {
		this.source = source;
	}
	public Source getSource() {
		return source;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Id: " + id);
		sb.append("\nClass: " + schoolClass.getCode());
		sb.append("\nBook ISBN: " + book.getIsbn());
		sb.append("\nNecessity: " + necessity.toString());
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ClassBook) {
			ClassBook other = (ClassBook) obj;
			return book.equals(other.book) && schoolClass.equals(other.schoolClass) &&
					necessity.equals(other.necessity) && notes.equals(other.notes);
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return book.hashCode() + 3 * schoolClass.hashCode() +
				5 * necessity.hashCode() + 7 * (notes == null ? 0 : notes.hashCode());
	}

}