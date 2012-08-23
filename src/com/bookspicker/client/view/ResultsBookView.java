package com.bookspicker.client.view;

import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.widgets.buttons.PickButton;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.ClassBook;
import com.bookspicker.shared.Item;
import com.bookspicker.shared.ClassBook.Necessity;
import com.bookspicker.shared.ClassBook.Source;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

/**
 * View that displays a single Book that is 
 * meant to be shown in the results list.
 * (Displays more text than the BundleBookView).
 * 
 * @author sinchan, Rodrigo Ipince
 *
 */
public class ResultsBookView extends Grid {
	FlexCellFormatter detailsFormatter;
	CellFormatter bookViewFormatter;
	PickButton pickButton;
	FlexTable bookDetails;
	Item displayedItem;
	
	public ResultsBookView(ClassBook classBook) {
		super(1,3);
		
		displayedItem = classBook;
		setBookDetails(classBook.getBook(), classBook.getNecessity(),
				classBook.getSource(), classBook.getNotes());
	}
	
	public ResultsBookView(Book book) {
		super(1,3);
		
		displayedItem = book;
		setBookDetails(book, null, null, null);
	}
	
	private void setBookDetails(Book book, Necessity need,
			Source source, String notes) {
		bookDetails = new FlexTable();
		bookDetails.setStylePrimaryName(Resources.INSTANCE.style().resultsBookDetails());

		detailsFormatter = bookDetails.getFlexCellFormatter();
		bookViewFormatter = this.getCellFormatter();
		
		setImage(book.getImageUrl());
		setBookTitle(book.getTitle());
		setAuthors(book.getAuthorList());
		setISBN(book.getIsbn());
		if(book.getEdition()!=-1){
			setEdition(book.getEdition());
		}
		if (need != null) {
			setType(need, source);
		}
		
//		if (source != null) {
//			Label sourceLabel = new Label("Source: " + source.getDisplayName());
//			sourceLabel.setStylePrimaryName("bookType"); // TODO fix
//			bookDetails.setWidget(5, 0, sourceLabel);
//		}
		
		if (notes != null && !notes.isEmpty()) {
			Label notesLabel = new Label("Note: " + notes);
			notesLabel.setStylePrimaryName(Resources.INSTANCE.style().resultsBookViewNote()); 
			bookDetails.setWidget(6, 0, notesLabel);
		}
		
		this.setWidget(0, 1, bookDetails);
		
		pickButton = new PickButton(book);
		this.setWidget(0, 2, pickButton);	
		
		bookDetails.setCellSpacing(0);
		bookDetails.setCellPadding(0);
		
		for(int j=0;j<5;j++){
			detailsFormatter.setWidth(j, 0, "100%");
			detailsFormatter.setHorizontalAlignment(j, 0, HasHorizontalAlignment.ALIGN_LEFT);
		}				
		
		bookViewFormatter.setWidth(0, 1, "100%");
		bookViewFormatter.setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
		bookViewFormatter.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);		
	}

	private void setType(Necessity need, Source source) {
		// TODO (sinchan): should probably change
		// this to setNecessity => need to change style names
		// as well.
		String type = "Necessity Unknown";
		switch (need) {
			case REQUIRED:
				type = "Required" + source.getDescription();
				break;
			case RECOMMENDED:
				type = "Recommended" + source.getDescription();;
				break;
			case RESERVE_ONLY:
				type = "Reserve Only" + source.getDescription();;
				break;
			case UNKNOWN:
				type = "Necessity Unknown";
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
		title.setStylePrimaryName(Resources.INSTANCE.style().resultsBookTitle());
		bookDetails.setWidget(0, 0, title);
	}
	
	private void setImage(String imageUrl) {
		Image bookImage = new Image(imageUrl);
		if (imageUrl.isEmpty())
			bookImage = new Image(Resources.INSTANCE.noImageAvailable());
		bookImage.setStylePrimaryName(Resources.INSTANCE.style()
				.resultsBookImage());
		this.setWidget(0, 0, bookImage);
	}
	
	@Override
	public int hashCode() {
		return this.displayedItem.hashCode();
	}


	public void setPickable(boolean pickable) {
		this.pickButton.setDisabled(!pickable);
	}
	
}
