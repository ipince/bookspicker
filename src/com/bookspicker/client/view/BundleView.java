package com.bookspicker.client.view;

import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.SearchPage;
import com.bookspicker.client.view.Resources.Style;
import com.bookspicker.client.view.widgets.BPPanel;
import com.bookspicker.client.view.widgets.buttons.FindBestBundleButton;
import com.bookspicker.shared.Book;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;

/**
 * View that displays the bundle (i.e. the list of books in the bundle). Each
 * book is shown through BundleBookView objects.
 * 
 * @author sinchan
 * 
 */
public class BundleView extends BPPanel {
	private FlowPanel bestBundleWrapper = new FlowPanel();
	private FlowPanel booksPanel = new FlowPanel();
	private FindBestBundleButton findBestBundleButton;
	private PushButton backToSearchButton;

	private final Style STYLE = Resources.INSTANCE.style();
	
	private boolean offerMode = false;
	
	private static final String EMPTY_BUNDLE_STRING = "Your cart is empty. Add books to it by searching on the right!";

	private HTML emptyBundleLabel = new HTML(EMPTY_BUNDLE_STRING);
	
	public BundleView() {
		super("Your Cart");
		this.setStylePrimaryName(STYLE.bundleView());
		booksPanel.setStylePrimaryName(STYLE.bundleBooksList());
		bestBundleWrapper.setStylePrimaryName(STYLE.mainBundleButtonWrapper());

		findBestBundleButton = new FindBestBundleButton();
		bestBundleWrapper.add(findBestBundleButton);

		this.getContentPanel().add(bestBundleWrapper);

		emptyBundleLabel.setStylePrimaryName(STYLE.emptyBundleLabel());
		booksPanel.add(emptyBundleLabel);
		
		this.getContentPanel().add(booksPanel);
		
		Image backImage = new Image(Resources.INSTANCE.backToSearchButton());
		backToSearchButton = new PushButton(backImage);
		backToSearchButton.setStylePrimaryName(STYLE.bpButton());
		backToSearchButton.addStyleName(STYLE.mainBundleButton());
		backToSearchButton.addClickHandler(SearchPage.backToSearchHandler);
	}

	public void addBook(Book book) {
		if (booksPanel.getWidgetCount() == 1){
			findBestBundleButton.setEnabled(true);
			booksPanel.remove(emptyBundleLabel);
		}
		booksPanel.add(new BundleBookView(book, true));
	}
	
	public void clear() {
		booksPanel.clear();
		booksPanel.add(emptyBundleLabel);
		findBestBundleButton.setEnabled(false);
	}

	public void removeBookView(BundleBookView bookView) {
		if (booksPanel.getWidgetCount() == 1){
			findBestBundleButton.setEnabled(false);
			booksPanel.add(emptyBundleLabel);
		}
		booksPanel.remove(bookView);
	}

	public void toggleBestBundleButton() {
		findBestBundleButton.setEnabled(!findBestBundleButton.isEnabled());
	}
	
	public boolean isInOfferMode() {
		return offerMode;
	}
	
	/**
	 * Toggles the contents of the large panel, alternating between the book
	 * search results view and the offer results view.
	 */
	public void toggleOfferMode() {
		offerMode = !offerMode;
		if (offerMode) {
			bestBundleWrapper.clear();
			bestBundleWrapper.add(backToSearchButton);
		} else {
			bestBundleWrapper.clear();
			bestBundleWrapper.add(findBestBundleButton);
		}
	}
	
	public void setBookRemovability(boolean removable) {
		for (int i = 0; i < booksPanel.getWidgetCount(); i++) {
			BundleBookView view = (BundleBookView) booksPanel.getWidget(i);
			view.setRemovability(removable);
		}
	}
}
