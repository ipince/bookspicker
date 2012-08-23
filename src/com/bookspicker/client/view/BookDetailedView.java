package com.bookspicker.client.view;

import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.widgets.buttons.PickButton;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.NumberUtil;
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

public class BookDetailedView extends FlowPanel {
	
	private Grid bookGrid;
	private FlexCellFormatter detailsFormatter;
	private CellFormatter bookViewFormatter;
	private PickButton pickButton;
	private FlexTable bookDetails;
	private Book displayedBook;

	public BookDetailedView(Book book) {

		bookGrid = new Grid(1, 2);
		
		this.setStylePrimaryName(Resources.INSTANCE.style().bookDetailedView());
		
		displayedBook = book;
		bookDetails = new FlexTable();

		detailsFormatter = bookDetails.getFlexCellFormatter();
		bookViewFormatter = bookGrid.getCellFormatter();

		setImage(book.getImageUrl());

		setBookTitle(book.getTitle());
		setAuthors(book.getAuthorList());
		setISBN(book.getIsbn());
		setEdition(book.getEdition());
		setListPrice(book.getListPrice());

		RowFormatter rowFormatter = bookDetails.getRowFormatter();
		for (int j = 0; j < bookDetails.getRowCount(); j++) {
			detailsFormatter.setWidth(j, 0, "100%");

			rowFormatter.setStyleName(j, Resources.INSTANCE.style()
					.offerViewBookDetail());
			detailsFormatter.setHorizontalAlignment(j, 0,
					HasHorizontalAlignment.ALIGN_LEFT);
		}

		bookGrid.setWidget(0, 1, bookDetails);
		
		add(bookGrid);

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
			bookImage = new Image(Resources.INSTANCE.noImageAvailable());
		bookImage.setStylePrimaryName(Resources.INSTANCE.style()
				.resultsBookImage());
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
