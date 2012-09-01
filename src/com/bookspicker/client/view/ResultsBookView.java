package com.bookspicker.client.view;

import java.util.ArrayList;
import java.util.List;

import com.bookspicker.client.view.widgets.LoadingPanel;
import com.bookspicker.client.view.widgets.ModernOfferTablePanel;
import com.bookspicker.client.view.widgets.OfferTablePanel;
import com.bookspicker.client.view.widgets.buttons.PickButton;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.ClassBook;
import com.bookspicker.shared.ClassBook.Necessity;
import com.bookspicker.shared.ClassBook.Source;
import com.bookspicker.shared.Item;
import com.bookspicker.shared.Offer;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * View that displays a single Book that is 
 * meant to be shown in the results list.
 * (Displays more text than the BundleBookView).
 * 
 * @author sinchan, Rodrigo Ipince
 *
 */
public class ResultsBookView extends FlexTable {
	FlexCellFormatter detailsFormatter;
	CellFormatter bookViewFormatter;
	PickButton pickButton;
	FlexTable bookDetails;
	Item displayedItem;
	ModernOfferTablePanel onlineOffersPanel;
	ModernOfferTablePanel localOffersPanel;
	LoadingPanel loadingPanel = new LoadingPanel("Looking for the best prices...");
	
	public ResultsBookView(ClassBook classBook) {
		super();
		
		displayedItem = classBook;
		setBookDetails(classBook.getBook(), classBook.getNecessity(),
				classBook.getSource(), classBook.getNotes());
	}
	
	public ResultsBookView(Book book) {
		super();
		
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
		
		pickButton = new PickButton();
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
	
	public void showOffers(List<Offer> offers){
		List<Offer> onlineOffers = new ArrayList<Offer>();
		List<Offer> localOffers = new ArrayList<Offer>();
		for(Offer offer:offers){
			if(offer.getStoreName().isLocal()){
				localOffers.add(offer);
			} else {
				onlineOffers.add(offer);
			}
		}
		
		FlexTable offersPanelTable = new FlexTable();
		Label offersPanelLabel;
		
		if(localOffers.size() != 0){
			offersPanelLabel = new Label("Buy Locally");
			offersPanelLabel.setStylePrimaryName(Resources.INSTANCE.style().offerPanelLabel());
			offersPanelTable.setWidget(0, 0, offersPanelLabel);
			offersPanelTable.getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
			offersPanelTable.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_LEFT);
			
			localOffersPanel = new ModernOfferTablePanel(displayedItem.getBook(), localOffers.get(0), localOffers);
			offersPanelTable.setWidget(1, 0, localOffersPanel);
		}
		
		if(onlineOffers.size() != 0){
			offersPanelLabel = new Label("Buy Online");
			offersPanelLabel.setStylePrimaryName(Resources.INSTANCE.style().offerPanelLabel());
			offersPanelTable.setWidget(2, 0, offersPanelLabel);
			offersPanelTable.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
			offersPanelTable.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
			
			onlineOffersPanel = new ModernOfferTablePanel(displayedItem.getBook(), onlineOffers.get(0), onlineOffers);
			offersPanelTable.setWidget(3, 0, onlineOffersPanel);
		}
		
		offersPanelTable.getElement().getStyle().setWidth(100, Unit.PCT);
		this.setWidget(1, 0, offersPanelTable);
		this.getFlexCellFormatter().setColSpan(1, 0, 3);
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
	
	public Book getBook(){
		return displayedItem.getBook();
	}

	public void showLoading() {
		//TODO: made check prices button to hide prices.. or just remove the button..
		this.setWidget(1, 0, loadingPanel);
		this.getFlexCellFormatter().setColSpan(1, 0, 3);
	}

	public void reset() {
		this.removeRow(1);
	}
	
}
