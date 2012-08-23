package com.bookspicker.client.view;

import java.util.List;

import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.widgets.OfferTablePanel;
import com.bookspicker.client.view.widgets.buttons.PickButton;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.NumberUtil;
import com.bookspicker.shared.Offer;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;

/**
 * View that displays a single Book that is meant to be shown in the results
 * list. (Displays more text than the BundleBookView).
 * 
 * @author sinchan, Rodrigo Ipince
 * 
 */
public class BookOfferView extends FlowPanel {
	
	Grid bookGrid;
	FlexCellFormatter detailsFormatter;
	CellFormatter bookViewFormatter;
	PickButton pickButton;
	FlexTable bookDetails;
	Book displayedBook;
	List<Offer> offers;
	OfferTablePanel offersPanel;

	public BookOfferView(Book book, Offer selectedOffer, List<Offer> list) {

		bookGrid = new Grid(1, 2);
		
		this.setStylePrimaryName(Resources.INSTANCE.style().bookOfferView());
		
		displayedBook = book;
		offers = list;
		bookDetails = new FlexTable();

		detailsFormatter = bookDetails.getFlexCellFormatter();
		bookViewFormatter = bookGrid.getCellFormatter();

		setImage(book.getImageUrl());

		setBookTitle(book.getTitle());
		setAuthors(book.getAuthorList());
		//setISBN(book.getIsbn());
		//setEdition(book.getEdition());
//		setType(0);
		setListPrice(book.getListPrice());

		RowFormatter rowFormatter = bookDetails.getRowFormatter();
		for (int j = 0; j < bookDetails.getRowCount(); j++) {
			detailsFormatter.setWidth(j, 0, "100%");

			rowFormatter.setStyleName(j, Resources.INSTANCE.style()
					.offerViewBookDetail());
			detailsFormatter.setHorizontalAlignment(j, 0,
					HasHorizontalAlignment.ALIGN_LEFT);
		}
		
		offersPanel = new OfferTablePanel(book, selectedOffer, offers);

		bookGrid.setWidget(0, 1, bookDetails);
		
		add(bookGrid);
		add(offersPanel);

		bookDetails.setCellSpacing(0);
		bookDetails.setCellPadding(0);

		bookViewFormatter.setWidth(0, 1, "100%");
		bookViewFormatter.setVerticalAlignment(0, 1,
				HasVerticalAlignment.ALIGN_TOP);
		bookViewFormatter.setHorizontalAlignment(0, 0,
				HasHorizontalAlignment.ALIGN_CENTER);
	}

	private void setListPrice(int listPrice) {
		if (listPrice != -1) {
			String priceFormatted = NumberUtil.getDisplayPrice(listPrice);
			Label editionLabel = new Label(priceFormatted + " is the List Price!");
			editionLabel.setStylePrimaryName("listPrice");
			bookDetails.setWidget(4, 0, editionLabel);
		}
	}

	private void setType(int i) {
		String type = "Necessity Unknown";
		switch (i) {
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
		switch (edition) {
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
			editionText = edition + "th Edition";
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
		for (int i = 0; i < authorList.length - 1; i++) {
			authors += authorList[i] + ", ";
		}
		if (authorList.length != 0) {
			if (authorList.length > 1) {
				authors += "and ";
			}
			authors += authorList[authorList.length - 1];
		}
		Label authorLabel = new Label(authors);
		authorLabel.setStylePrimaryName("bookAuthors");
		bookDetails.setWidget(1, 0, authorLabel);
	}

	private void setBookTitle(String titleText) {
		Label title = new Label(titleText);
		title.setStylePrimaryName(Resources.INSTANCE.style()
						.resultsBookTitle());
		bookDetails.setWidget(0, 0, title);
	}

	private void setImage(String imageUrl) {
		Image bookImage = new Image(imageUrl);
		if (imageUrl.isEmpty())
			bookImage = new Image(Resources.INSTANCE.noImageAvailableSmall());
		bookImage.setStylePrimaryName(Resources.INSTANCE.style()
				.offerViewBookDetailImage());
		bookGrid.setWidget(0, 0, bookImage);
		bookViewFormatter.setVerticalAlignment(0, 0,
				HasVerticalAlignment.ALIGN_TOP);
	}

	@Override
	public int hashCode() {
		return this.displayedBook.hashCode();
	}

	public void setPickable(boolean pickable) {
		this.pickButton.setDisabled(!pickable);
	}

}
