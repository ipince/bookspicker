package com.bookspicker.client.view.widgets;

import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.LocalOffer.Condition;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * This has been replaced by EditOfferView. Will delete as
 * soon as Sinchan confirms that he's moved his changes over.
 * 
 * @author Rodrigo Ipince
 * @deprecated
 */
public class EditOfferWidget extends Composite {
	
	private LocalOffer offer;
	private VerticalPanel container = new VerticalPanel();
	
	// Input fields
	private TextBox bookInput = new TextBox();
	private TextBox conditionInput = new TextBox();
	private TextBox priceInput = new TextBox();
	private TextBox locationInput = new TextBox();
	private TextArea commentsInput = new TextArea();
	
	public EditOfferWidget() {
		initWidget(container);
		createLayout();
		setOffer(null);
	}
	
	public void setOffer(LocalOffer offer) {
		if (offer == null) {
			this.offer = new LocalOffer();
			clearInputs();
		} else {
			this.offer = offer;
			populate(this.offer);
		}
	}

	private void clearInputs() {
		bookInput.setText("");
		conditionInput.setText("");
		priceInput.setText("");
		locationInput.setText("");
		commentsInput.setText("");
	}

	private void createLayout() {
		Grid details = new Grid(5, 2);
		
		details.setWidget(0, 0, new Label("Book ISBN*:"));
		details.setWidget(0, 1, bookInput);
		details.setWidget(1, 0, new Label("Condition*:"));
		details.setWidget(1, 1, conditionInput);
		details.setWidget(2, 0, new Label("Price*:"));
		details.setWidget(2, 1, priceInput);
		details.setWidget(3, 0, new Label("Location:"));
		details.setWidget(3, 1, locationInput);
		details.setWidget(4, 0, new Label("Comments:"));
		details.setWidget(4, 1, commentsInput);
		
		Button cancelButton = new Button("Cancel");
		Button saveButton = new Button("Save");
		saveButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				// TODO: populate offer and check validity
//				boolean valid = false;
//				if (valid)
//					
			}
			
		});
		
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.add(cancelButton);
		buttons.add(saveButton);
		
		container.add(details);
		container.add(buttons);
	}
	
	private void populate(LocalOffer offer) {
		bookInput.setText(offer.getBook().getTitle());
		conditionInput.setText(offer.getBookCondition().getDisplayName());
		priceInput.setText("na"); // TODO
//		locationInput.setText(offer.getLocation());
		commentsInput.setText(offer.getComments());
	}
	
	public LocalOffer getOffer() {
		
		// Update offer with details
		updateOfferWithInputs();
		
		return offer;
	}

	private void updateOfferWithInputs() {
		// TODO(rodrigo): un-hardcode
//		offer.setBookTitle(bookInput.getText());
//		offer.setBookIsbn("0262033844");
		offer.setBookCondition(Condition.ACCEPTABLE);
		offer.setAutoPricing(true);
//		offer.setLocation("Ashdown");
		offer.setComments(commentsInput.getText());
	}

	public void showError() {
		Window.alert("Please fill all the required fields");
	}

}
