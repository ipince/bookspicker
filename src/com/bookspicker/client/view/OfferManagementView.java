package com.bookspicker.client.view;

import java.util.ArrayList;
import java.util.List;

import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.Resources.Style;
import com.bookspicker.client.presenter.OfferManagementPresenter;
import com.bookspicker.client.view.widgets.HasOffer;
import com.bookspicker.client.view.widgets.buttons.AddNewListingButton;
import com.bookspicker.client.view.widgets.buttons.OfferButton;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.NumberUtil;
import com.bookspicker.shared.User;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;

/**
 * 
 * @author Rodrigo Ipince
 */
public class OfferManagementView extends Composite
	implements OfferManagementPresenter.DataDisplay {
	
	// main content wrapper; will contain greetingLabel
	// and offerPanel
	private final SimplePanel contentPanel;
	
	private final Label greetingLabel; // says 'hi' to user
	
	// contains actual offers view; will contain noOffersView
	// if there are no offers, and offersView if there are
	private final FlowPanel offersPanel;
	
	private final Label noOffersView; // For use when there are 0 offers
	private final FlowPanel offersView; // Container for offerTable
	private final FlexTable offerTable; // Table that holds  actual offers
	
	private final AddNewListingButton createOfferButton;
	
	private final List<HasOffer> editButtons;
	private final List<HasOffer> postButtons;
	private final List<HasOffer> deleteButtons;
	
	// Formatting stuff
	private final RowFormatter rowFormatter;
	private final FlexCellFormatter cellFormatter;
	
	private static final Style STYLE = Resources.INSTANCE.style();
	
	private boolean bound = false;
	
	public OfferManagementView() {
		contentPanel = new SimplePanel();
		initWidget(contentPanel);
		
		// Button lists
		editButtons = new ArrayList<HasOffer>();
		postButtons = new ArrayList<HasOffer>();
		deleteButtons = new ArrayList<HasOffer>();
		
		// Initialize main layout
		// Greeting part
		greetingLabel = new Label();
		greetingLabel.setStylePrimaryName(STYLE.greetingLabel());
		
		
		// Offer panel
		offersPanel = new FlowPanel();
		
		// Widgets for when there are zero offers
		noOffersView = new HTML("<b>You do not have any offers</b>");
		
		// Widgets for when there are 1+ offers
		offersView = new FlowPanel();
		offerTable = new FlexTable(); // offer table itself
		offerTable.setStylePrimaryName(STYLE.offerManagementTable());
		rowFormatter = offerTable.getRowFormatter();
		cellFormatter = offerTable.getFlexCellFormatter();
		initOfferTable();
		Label listingsLabel = new Label("Here are your listings:");
		listingsLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
		offersView.add(listingsLabel);
		offersView.add(offerTable);
		
		createOfferButton = new AddNewListingButton();
		
		// Main panel
		FlowPanel mainPanel = new FlowPanel();
		mainPanel.setStylePrimaryName(STYLE.offerManagementPanel());
		mainPanel.add(greetingLabel);
		mainPanel.add(offersPanel);
		mainPanel.add(createOfferButton);
		contentPanel.setWidget(mainPanel);
//		contentPanel.setVisible(false);
	}
	
	private void initOfferTable() {
		// TODO(rodrigo): removed class/location for multi-school support
		String[] columnHeaders = new String[] { "Book title (ISBN)", /*"Class",*/ "Condition",
				/*"Location",*/ "Price", "Comments", "Status" };
		rowFormatter.setStyleName(0, STYLE.headerRow());
		for (int c = 0; c < columnHeaders.length; c++) {
			offerTable.setWidget(0, c, new Label(columnHeaders[c]));
		}
	}
	
	// ===== Methods from Presenter.Display =====
	
	@Override
	public boolean isBound() {
		return bound;
	}
	@Override
	public void setBound(boolean bound) {
		this.bound = bound;
	}
	@Override
	public Widget asWidget() {
		return this;
	}
	@Override
	public HasClickHandlers getCreateOfferButton() {
		return createOfferButton;
	}
	@Override
	public List<HasOffer> getEditButtons() {
		return editButtons;
	}
	@Override
	public List<HasOffer> getPostButtons() {
		return postButtons;
	}
	@Override
	public List<HasOffer> getDeleteButtons() {
		return deleteButtons;
	}
	@Override
	public void setData(User user) {
		greetingLabel.setText("Hi " + user.getName() + ",");
		
		List<LocalOffer> offers = user.getLocalOffers();
		
		boolean hasNoRelevantOffers = true;
		for (LocalOffer offer : offers) {
			if (!offer.isDeleted()) {
				hasNoRelevantOffers = false;
				break;
			}
		}
		
		if (hasNoRelevantOffers) {
			offersPanel.clear();
			offersPanel.add(noOffersView);
		} else {
			offersPanel.clear();
			offersPanel.add(offersView);
			
			offerTable.removeAllRows();
			initOfferTable();
			
			editButtons.clear();
			postButtons.clear();
			deleteButtons.clear();
			
			LocalOffer offer;
			int row = 1;
			for (int i = 0; i < offers.size(); i++) {
				offer = offers.get(i);
				// Skip deleted offers
				if (offer.isDeleted())
					continue;
				addOfferToRow(offerTable, rowFormatter, cellFormatter, row++, offer);
			}
		}
		
//		contentPanel.setVisible(true);
	}
	
	private void addOfferToRow(FlexTable table,
			RowFormatter rowFormatter, FlexCellFormatter cellFormatter,
			int row, LocalOffer offer) {
		
		rowFormatter.setVerticalAlign(row, HasVerticalAlignment.ALIGN_MIDDLE);
		
		// TODO(rodrigo): removed class/location for multi-school support
		// if you uncomment it, be sure to update the indeces!
		table.setText(row, 0, offer.getBook().getTitle() + " (" + offer.getBook().getIsbn() + ")");
//		table.setText(row, 1, offer.getClassCode() == null ? "(none)" : offer.getClassCode());
		table.setText(row, 1, offer.getBookCondition().getDisplayName());
//		table.setText(row, 3, offer.getLocation() == null ? "(none)" : offer.getLocation().getDisplayName());
		if (offer.isAutoPricing()) {
			if (offer.isSold())
				table.setText(row, 2, NumberUtil.getDisplayPrice(offer.getSellingPrice())); // TODO: add popup
			else
				table.setText(row, 2, (offer.getAutoPrice() < 0 ? "N/A" : NumberUtil.getDisplayPrice(offer.getAutoPrice())) + " (auto)"); // TODO: add popup
		} else {
			table.setText(row, 2, NumberUtil.getDisplayPrice(offer.getTotalPrice()));
		}
//		cellFormatter.setHorizontalAlignment(row, 5, HasHorizontalAlignment.ALIGN_CENTER);
		table.setText(row, 3, offer.getComments() == null ? "(none)" : offer.getComments()); // TODO: cut if too long
		table.setText(row, 4, offer.getStatus());
		
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		buttons.setStylePrimaryName(STYLE.offerButtonPanel());
		
		OfferButton editButton = new OfferButton(offer, Resources.INSTANCE.editButton(), Resources.INSTANCE.editButtonDisabled());
		editButton.setEnabled(!offer.isSold()); // disable if sold
		buttons.add(editButton);
		editButtons.add(editButton);
		
		if (offer.isActive()) {
			OfferButton postButton = new OfferButton(offer, Resources.INSTANCE.deactivateButton(), Resources.INSTANCE.deactivateButtonDisabled());
			postButton.setEnabled(!offer.isSold()); // disable if sold
			buttons.add(postButton);
			postButtons.add(postButton);
		} else {
			OfferButton postButton = new OfferButton(offer, Resources.INSTANCE.reactivateButton(), Resources.INSTANCE.reactivateButtonDisabled());
//			postButton.setEnabled(!offer.isSold()); // disable if sold
			buttons.add(postButton);
			postButtons.add(postButton);
		}
		
		OfferButton deleteButton = new OfferButton(offer, Resources.INSTANCE.deleteButton(), Resources.INSTANCE.deactivateButtonDisabled());
		buttons.add(deleteButton);
		deleteButtons.add(deleteButton);
		
		table.setWidget(row, 5, buttons);
	}

}
