package com.bookspicker.shared;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.bookspicker.shared.ClassBook.Necessity;
import com.bookspicker.shared.ClassBook.Source;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.IsSerializable;

public class SchoolClass implements IsSerializable {

	private Long id;
	private School school; // required
	
	// These three things are taken from B&N's database
	private String course; // required
	private String clas; // required
	private String section; // required
	
	// Should include the section only if there are multiple
	// sections for the same course/class.
	// We perform searches on this field
	private String code; // required
	private String title; // optional
	private Term term; // required
	private String jointSubjects; // optional
	private Date lastActivityDate; // optional
	private Date warehouseLoadDate; // optional
	private List<ClassBook> books = new ArrayList<ClassBook>(); // req

	public SchoolClass() {		
	}

	public SchoolClass(String code, String title, Term term) {
		this.setCode(code);
		this.setTitle(title);
		this.setTerm(term);
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getCode() {
		return code;
	}
	public String getTitle() {
	    return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Term getTerm() {
		return term;
	}
	public void setTerm(Term term) {
		this.term = term;
	}
	public void setJointSubjects(String jointSubjects) {
		this.jointSubjects = jointSubjects;
	}
	public String getJointSubjects() {
		return jointSubjects;
	}
	public void setLastActivityDate(Date lastActivityDate) {
		this.lastActivityDate = lastActivityDate;
	}
	public Date getLastActivityDate() {
		return lastActivityDate;
	}
	public void setWarehouseLoadDate(Date warehouseLoadDate) {
		this.warehouseLoadDate = warehouseLoadDate;
	}
	public Date getWarehouseLoadDate() {
		return warehouseLoadDate;
	}

	public List<ClassBook> getBooks() {
		return books;
	}

	public void setBooks(List<ClassBook> books) {
		this.books = books;
	}
	
	public void addBook(Book book, Necessity necessity, Source source) {
		ClassBook cb = new ClassBook(this, book, necessity, source);
		if (!books.contains(cb))
			books.add(cb);
	}
	
	public void addBook(Book book, Necessity necessity, Source source, String notes) {
		ClassBook cb = new ClassBook(this, book, necessity, source, notes);
		if (!books.contains(cb))
			books.add(cb);
	}
	
	public List<String> getIsbnList() {
		List<String> isbnList = new ArrayList<String>();
		for (ClassBook book : getBooks()) {
			isbnList.add(book.getBook().getIsbn());
		}
		return isbnList;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Code: " + code);
		sb.append("\nTitle: " + title);
		sb.append("\nTerm: " + term.toString());
		sb.append("\nBooks: " + (books == null ? "null" : books.toString()));
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		return code.hashCode() + 3 * term.hashCode();
	}

	public void setSchool(School school) {
		this.school = school;
	}

	public School getSchool() {
		return school;
	}

	public void setCourse(String course) {
		this.course = course;
	}

	public String getCourse() {
		return course;
	}

	public void setClas(String clas) {
		this.clas = clas;
	}

	public String getClas() {
		return clas;
	}

	public void setSection(String section) {
		this.section = section;
	}
	
	public String getSection() {
		return section;
	}
	
	public String getFormatedClassName() {
	    StringBuilder sb = new StringBuilder();
	    if (getCourse() != null) {
	        sb.append(getCourse());
	    }
	    if (getClas() != null) {
	    	if(this.school.equals(School.MIT))
	    		sb.append(getClas());
	    	else
	    		sb.append(" " + getClas());
	    }
	    if (getSection() != null) {
	    	// Only append section for non-MIT schools.
	    	if (!school.equals(School.MIT)) {
		        sb.append(" / Sec. ");
		        sb.append(getSection());
	    	}
	    }
	    if (getTitle() != null && !getTitle().equals("")) {
	        String className = getTitle().length() < 45 ? getTitle() : getTitle().substring(0, 32) + "...";
	        sb.append (" - ");
	        sb.append(className);
	    }
	  return sb.toString();
	}
}