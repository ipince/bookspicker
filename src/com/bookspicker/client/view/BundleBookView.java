package com.bookspicker.client.view;

import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.widgets.buttons.RemoveButton;
import com.bookspicker.shared.Book;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

/**
 * View that displays a single Book that is 
 * meant to be shown in the Bundle book list.
 * (Displays less text than the ResultsBookView).
 * 
 * @author sinchan
 *
 */
public class BundleBookView extends Grid {
	FlexCellFormatter detailsFormatter;
	CellFormatter bookViewFormatter;	
	RemoveButton removeButton;
	FlexTable bookDetails;
	Book displayedBook;
	
	public BundleBookView(Book book, boolean showRemoveButton) {
		super(1,3);
		
		displayedBook = book;
		
		bookDetails = new FlexTable();
		bookDetails.setStylePrimaryName(Resources.INSTANCE.style().bundleBookDetails());

		detailsFormatter = bookDetails.getFlexCellFormatter();
		bookViewFormatter = this.getCellFormatter();
		
		setImage(book.getImageUrl());
		setBookTitle(book.getTitle());
		setAuthors(book.getAuthorList());
//		setISBN(book.getIsbn());
//		setEdition(book.getEdition());
//		setType(0);
		
		this.setWidget(0, 1, bookDetails);
		
		if (showRemoveButton) {
			removeButton = new RemoveButton(this);
			this.setWidget(0, 2, removeButton);
		}
		
		bookDetails.setCellSpacing(0);
		bookDetails.setCellPadding(0);
		
		for(int j=0;j<5;j++){
			detailsFormatter.setWidth(j, 0, "100%");
			detailsFormatter.setHorizontalAlignment(j, 0, HasHorizontalAlignment.ALIGN_LEFT);
		}				
		
		bookViewFormatter.setWidth(0, 1, "100%");
		bookViewFormatter.setVerticalAlignment(0, 2, HasVerticalAlignment.ALIGN_TOP);
		bookViewFormatter.setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
		bookViewFormatter.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);	
	}
	
	public Book getDisplayedBook(){
		return displayedBook;
	}
	

	private void setType(int i) {
		String type = "Necessity Unknown";
		switch(i){
			case 0:
				type = "Required";
				break;
			case 1:
				type = "Recommended";
				break;
		}
		Label typeLabel = new Label(type);
		typeLabel.setStylePrimaryName("bookType");		
		bookDetails.setWidget(4, 0, typeLabel);
	}

	private void setEdition(Integer edition) {
		String editionText = "Unknown Edition";
		switch(edition){
		case -1:
			break;
		case 1:
			editionText = "1st Edition";
			break;
		case 2:
			editionText = "2nd Edition";
			break;
		case 3:
			editionText = "3rd Edition";
			break;
		default:
			editionText = edition+ "th Edition";
			break;
		}
		
		Label editionLabel = new Label(editionText);
		editionLabel.setStylePrimaryName("bookEdition");
		bookDetails.setWidget(3, 0, editionLabel);
	}

	private void setISBN(String isbn) {
		Label isbnLabel = new Label(isbn);
		isbnLabel.setStylePrimaryName("bookISBN");
		bookDetails.setWidget(2, 0, isbnLabel);
	}

	private void setAuthors(String[] authorList) {
		String authors = "";
		for(int i=0; i<authorList.length-1; i++){
			authors+= authorList[i]+", ";
		}
		if(authorList.length!=0){
			if(authorList.length>1){
				authors+="and ";
			}
			authors+=authorList[authorList.length-1];
		}
		Label authorLabel = new Label(authors);
		authorLabel.setStylePrimaryName("bookAuthors");
		bookDetails.setWidget(1, 0, authorLabel);
	}

	private void setBookTitle(String titleText){
		Label title = new Label(titleText);
		title.setStylePrimaryName(Resources.INSTANCE.style().bundleBookTitle());
		bookDetails.setWidget(0, 0, title);
	}
	
	private void setImage(String imageUrl) {
		Image bookImage = new Image(imageUrl);
		if (imageUrl.isEmpty())
			bookImage = new Image(Resources.INSTANCE.noImageAvailableSmall());
		bookImage.setStylePrimaryName(Resources.INSTANCE.style().bundleBookImage());
		this.setWidget(0, 0, bookImage);		
	}
	
	@Override
	public int hashCode() { 
		return this.displayedBook.hashCode();
	}

	public void setRemovability(boolean removable) {
		removeButton.setVisible(removable);
	}
}
