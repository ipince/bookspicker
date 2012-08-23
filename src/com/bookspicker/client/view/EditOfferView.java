package com.bookspicker.client.view;

import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.Resources.Style;
import com.bookspicker.client.presenter.EditOfferPresenter;
import com.bookspicker.client.view.widgets.HasValueDummy;
import com.bookspicker.client.view.widgets.HelpWidget;
import com.bookspicker.client.view.widgets.ListChangeHandler;
import com.bookspicker.client.view.widgets.SuggestionTextArea;
import com.bookspicker.client.view.widgets.SuggestionTextBox;
import com.bookspicker.client.view.widgets.buttons.CancelButton;
import com.bookspicker.client.view.widgets.buttons.SaveButton;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.LocalOffer.Condition;
import com.bookspicker.shared.LocalOffer.Strategy;
import com.bookspicker.shared.Location;
import com.bookspicker.shared.NumberUtil;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Displays a LocalOffer for editing purposes.
 * 
 * @author Rodrigo Ipince
 */
public class EditOfferView extends Composite implements EditOfferPresenter.Display {

	private static final String PRICE_RADIO_NAME = "price";
	private static final String AUTO_PRICE_HELP = "Don't know how to price " +
			"your book? Let us price it for you! Just choose a strategy and " +
			"you're good to go. The price will depend on the current market " +
			"conditions as well as the book's condition. Thus, <i>the price " +
			"today may differ from the price tomorrow</i>. If you're scared that " +
			"the auto price might drop too low, you can choose a lower bound and we'll never " +
			"sell your book for less than that. Try it out!";
	private static final String LOCATION_HELP = "If you tell us the book's " +
			"location, we'll do our best to match you with the buyer that's " +
			"closest to you.";
	private static final String MARKET_PRICES_HELP = "These are the current " +
			"market prices for your book. We show you these for reference, to " +
			"help you choose a good price!";
	
	private static EditOfferViewUiBinder uiBinder = GWT.create(EditOfferViewUiBinder.class);
	
	interface EditOfferViewUiBinder extends UiBinder<Widget, EditOfferView> {}
	
	private static final Style STYLE = Resources.INSTANCE.style();
	
	@UiField
	protected FlowPanel contentPanel;
	
	@UiField
	protected FlowPanel formPanel;
	
	@UiField
	protected FlowPanel bookPanel;
	
	// Input fields
	private final TextBox bookInput = new SuggestionTextBox("ISBN (10 or 13 digits)");
	private final TextBox classInput = new SuggestionTextBox("e.g., 6.002");
	private final ListBox conditionInput = new ListBox();
	private final Label conditionDescription = new Label();
	private final RadioButton autoPriceButton;
	private final RadioButton fixedPriceButton;
	private final ListBox strategyInput = new ListBox();
	private final Label strategyDescription = new Label();
	private final SuggestionTextBox lowerBoundInput = new SuggestionTextBox("Lower bound (optional)");
	private final Label autoPricePreview = new Label();
	private final TextBox fixedPriceInput = new SuggestionTextBox("Fixed price");
	private final ListBox locationInput = new ListBox();
	private final TextArea commentsInput = new SuggestionTextArea("Comments about the book's condition, the location, why it's useful, etc...");
	
	private final Label error = new Label();
	
	// Book information
	private final SimplePanel bookContainer = new SimplePanel();
	private final SimplePanel priceContainer = new SimplePanel();
	private final FlowPanel priceDetails = new FlowPanel();
	private final HTML listPrice = new HTML();
	private final HTML lowestNewPrice = new HTML();
	private final HTML lowestUsedPrice = new HTML();
	private final HTML lowestInternationalPrice = new HTML();
	private final HTML highestBuybackPrice = new HTML();
	private final Image loadingIcon = new Image(Resources.INSTANCE.loadingIconSmall());
	private final SimplePanel priceLoading = new SimplePanel();
	
	private final SaveButton saveButton;
	private final CancelButton cancelButton;
	
	private Book book;
	private boolean bound = false;
	
	private Condition currentCondition;
	
	public EditOfferView() {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.setStylePrimaryName(STYLE.editOfferView());
		
		contentPanel.setStylePrimaryName(STYLE.editOfferViewContent());
		
		formPanel.setStylePrimaryName(STYLE.editOfferDetailsPanel());
		
		// Initialize main layout
		Grid details = new Grid(4, 2);
		
		details.setStylePrimaryName(STYLE.editOfferDetailsTable());		
		
		details.setCellSpacing(0);

		
		// Condition stuff
		conditionInput.addItem("- Select Condition -", "");
		for (Condition cond : LocalOffer.Condition.values()) {
			conditionInput.addItem(cond.getDisplayName(), cond.toString());
		}
		
		ListChangeHandler descriptionHandler = new ListChangeHandler(new Runnable() {
			@Override
			public void run() {
				try {
					Condition cond = Condition.valueOf(conditionInput.getValue(conditionInput.getSelectedIndex()));
					if (currentCondition == null || cond != currentCondition) {
						conditionDescription.setText(cond.getDescription());
						currentCondition = cond;
					}
				} catch (Exception e) {
					conditionDescription.setText("");
					currentCondition = null;
				}
			}
		});
		conditionInput.addKeyDownHandler(descriptionHandler);
		conditionInput.addChangeHandler(descriptionHandler);
		
		FlowPanel conditionPanel = new FlowPanel();
		conditionPanel.add(conditionInput);
		conditionPanel.add(conditionDescription);
		
		// Price stuff
		FlowPanel pricePanel = new FlowPanel();
		
		FlowPanel autoPanel = new FlowPanel();
		autoPanel.setStylePrimaryName(STYLE.autoPricingOptionPanel());
		
		FlowPanel fixedPanel = new FlowPanel();
		fixedPanel.setStylePrimaryName(STYLE.fixedPricingOptionPanel());
		
		HorizontalPanel hPanel = new HorizontalPanel();
		autoPriceButton = new RadioButton(PRICE_RADIO_NAME, "Automatic Pricing");
		PriceHandler autoHandler = new PriceHandler(true);
		autoPriceButton.addValueChangeHandler(autoHandler);
		autoPriceButton.addFocusHandler(autoHandler);
		autoPriceButton.setValue(true, true); // set default
		hPanel.add(autoPriceButton);
		hPanel.add(new HelpWidget(AUTO_PRICE_HELP,true));
		
		strategyInput.addItem("- Select Strategy -", "");
		for (Strategy strat : LocalOffer.Strategy.values()) {
			strategyInput.addItem(strat.getDisplayName(), strat.toString());
		}
		setAutoPricePreview(-1);
		
		ListChangeHandler strategyHandler = new ListChangeHandler(new Runnable() {
			@Override
			public void run() {
				try {
					Strategy strat = Strategy.valueOf(strategyInput.getValue(strategyInput.getSelectedIndex()));
					if (strat != null)
						strategyDescription.setText(strat.getDescription());
				} catch (Exception e) {
					strategyDescription.setText("");
				}
			}
		});
		strategyInput.addKeyDownHandler(strategyHandler);
		strategyInput.addChangeHandler(strategyHandler);
		
		lowerBoundInput.setMaxLength(6); // Max price is 999,999
		
		FlexTable autoPricingTopRow = new FlexTable();
		autoPricingTopRow.setCellPadding(5);
		autoPricingTopRow.setWidget(0, 0, hPanel);
		autoPricingTopRow.setWidget(0, 1, lowerBoundInput);
		autoPricingTopRow.setWidget(1, 0, autoPricePreview);
		autoPricingTopRow.setWidget(1, 1, strategyInput);
		autoPricingTopRow.setWidget(2, 0, strategyDescription);
		
		FlexCellFormatter autoPriceFormatter = autoPricingTopRow.getFlexCellFormatter();
		autoPriceFormatter.setColSpan(2, 0, 3);
		autoPriceFormatter.addStyleName(2, 0, STYLE.strategyDescriptionCell());
		
		autoPanel.add(autoPricingTopRow);
		
		Label orText = new Label("or");
		orText.setStylePrimaryName(STYLE.orText());
		
		fixedPriceButton = new RadioButton(PRICE_RADIO_NAME, "Choose your own price");
		PriceHandler fixedHandler = new PriceHandler(false);
		fixedPriceButton.addValueChangeHandler(fixedHandler);
		fixedPriceButton.addFocusHandler(fixedHandler);
		fixedPanel.add(fixedPriceButton);
		fixedPriceButton.getElement().getStyle().setPaddingRight(10, Unit.PX);
		fixedPriceInput.setMaxLength(6);
		fixedPanel.add(fixedPriceInput);
		
		pricePanel.add(autoPanel);
		pricePanel.add(orText);
		pricePanel.add(fixedPanel);
		
		// Location stuff
		HorizontalPanel locationPanel = new HorizontalPanel();
		locationPanel.add(new Label("Location:"));
		locationPanel.add(new HelpWidget(LOCATION_HELP, true));
		locationInput.addItem("- Choose Location -", "");
		for (Location loc : Location.values()) {
			locationInput.addItem(loc.getDisplayName(), loc.toString());
		}
		
		commentsInput.setStylePrimaryName(STYLE.commentsInput());
		
		Label isbnLabel = new Label("Book ISBN:*");
		isbnLabel.getElement().getStyle().setWidth(6, Unit.EM);
		details.setWidget(0, 0, isbnLabel);
		details.setWidget(0, 1, bookInput);
		// TODO(rodrigo): commented out for multi-school support.
		// if you uncomment them, make sure to update the indeces
		// AND also the details Grid!!
		// appropriately
//		details.setWidget(1, 0, new Label("Class:"));
//		details.setWidget(1, 1, classInput);
		details.setWidget(1, 0, new Label("Condition:*"));
		details.setWidget(1, 1, conditionPanel);
		details.setWidget(2, 0, new Label("Price:*"));
		details.setWidget(2, 1, pricePanel);
//		details.setWidget(4, 0, locationPanel);
//		details.setWidget(4, 1, locationInput);
		details.setWidget(3, 0, new Label("Comments:"));
		details.setWidget(3, 1, commentsInput);
		bookInput.setFocus(true);
		
		saveButton = new SaveButton();
		cancelButton = new CancelButton();
		
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.add(saveButton);
		buttons.add(cancelButton);
		buttons.setSpacing(10);
		
		error.setStylePrimaryName(STYLE.editOfferError());
		
		formPanel.add(details);
		formPanel.add(error);
		formPanel.add(buttons);
		
		
		// Book stuff
		bookPanel.add(new HTML("<b>Currently selected book:</b>"));
		bookPanel.add(bookContainer);
		bookContainer.getElement().getStyle().setMarginBottom(15, Unit.PX);
		HorizontalPanel headerPanel = new HorizontalPanel();
		headerPanel.add(new HTML("<b>Current market conditions:</b>"));
		headerPanel.add(new HelpWidget(MARKET_PRICES_HELP, false));
		priceLoading.setWidget(loadingIcon);
		headerPanel.add(priceLoading);
		
		bookPanel.add(headerPanel);
		
		bookPanel.setStylePrimaryName(STYLE.editOfferBookPanel());
		
		priceDetails.add(listPrice);
		priceDetails.add(lowestNewPrice);
		priceDetails.add(lowestUsedPrice);
		priceDetails.add(lowestInternationalPrice);
		priceDetails.add(highestBuybackPrice);
		priceContainer.setWidget(priceDetails);
		priceContainer.setStylePrimaryName(STYLE.editOfferPriceContainer());
		bookPanel.add(priceContainer);
		
		loadingIcon.setStylePrimaryName(STYLE.loadingIcon());
		
		setBook(null);
		setPrices(0, 0, 0, 0);

		contentPanel.add(formPanel);
		contentPanel.add(bookPanel);
	}
	
	// Methods for EditOfferPresenter.Display

	@Override
	public HasClickHandlers getCancelButton() {
		return cancelButton;
	}
	@Override
	public HasClickHandlers getSaveButton() {
		return saveButton;
	}
	@Override
	public Book getBook() {
		return book;
	}
	@Override
	public HasValue<String> getBookInput() {
		return bookInput;
	}
	@Override
	public HasValue<String> getClassInput() {
		return classInput;
	}
	@Override
	public HasValue<String> getCondition() {
		return conditionWrapper;
	}
	@Override
	public ListBox getConditionList() {
		return conditionInput;
	}
	@Override
	public HasValue<String> getLocation() {
		return locationWrapper;
	}
	@Override
	public boolean getAutoPrice() {
		return autoPriceButton.getValue();
	}
	@Override
	public void setAutoPrice(boolean autoPrice) {
		autoPriceButton.setValue(autoPrice, true);
		fixedPriceButton.setValue(!autoPrice, true);
	}
	@Override
	public HasValue<String> getStrategy() {
		return strategyWrapper;
	}
	@Override
	public ListBox getStrategyList() {
		return strategyInput;
	}
	@Override
	public HasValue<String> getLowerBound() {
		return lowerBoundInput;
	}
	@Override
	public void setAutoPricePreview(int price) {
		if (price < 0) {
			autoPricePreview.setText("Current auto price: N/A");
		} else {
			autoPricePreview.setText("Current auto price: " + NumberUtil.getDisplayPrice(price));
		}
	}
	@Override
	public HasValue<Integer> getFixedPrice() {
		return fixedPriceWrapper;
	}
	@Override
	public HasValue<String> getComments() {
		return commentsInput;
	}
	@Override
	public HasValueChangeHandlers<String> getCurrentBookHandler() {
		return bookInput;
	}
	@Override
	public void setBookLoading(boolean loading) {
		if (loading)
			bookContainer.setWidget(loadingIcon);
		else
			bookContainer.clear();
	}
	@Override
	public void setPricesLoading(boolean loading) {
		GWT.log("EditOfferView - Setting prices to load", null);
		priceLoading.setVisible(loading);
	}

	@Override
	public void setBook(Book book) {
		setBookLoading(false);
		this.book = book;
		if (book == null) {
			bookContainer.setWidget(new HTML("<i>No book selected.<br />Select a book by entering its ISBN on the left!</i>"));			
			listPrice.setHTML("list price: <b> N/A </b>");
			setPrices(0, 0, 0, 0);
		} else {
			bookContainer.setWidget(new BundleBookView(book, false));
			listPrice.setHTML("list price: <b> " + (book.getListPrice() <= 0 ? "N/A" : NumberUtil.getDisplayPrice(book.getListPrice()) + " </b>"));
		}
	}
	@Override
	public void setPrices(int lowestSaleNew, int lowestSaleUsed,
			int lowestInternational, int highestBuyback) {
		lowestNewPrice.setHTML("lowest new sale price: <b>" + (lowestSaleNew <= 0 ? "N/A" : NumberUtil.getDisplayPrice(lowestSaleNew))+"</b>");
		lowestUsedPrice.setHTML("lowest used sale price: <b>" + (lowestSaleUsed <= 0 ? "N/A" : NumberUtil.getDisplayPrice(lowestSaleUsed))+"</b>");
		lowestInternationalPrice.setHTML("lowest international edition sale price: <b>" + (lowestInternational <= 0 ? "N/A" : NumberUtil.getDisplayPrice(lowestInternational))+"</b>");
		highestBuybackPrice.setHTML("highest buy-back price: <b>" + (highestBuyback <= 0 ? "N/A" : NumberUtil.getDisplayPrice(highestBuyback))+"</b>");
	}
	@Override
	public void setError(String message) {
		error.setText(message);
	}

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
	
	// Utility methods
	
	private class PriceHandler implements FocusHandler, ValueChangeHandler<Boolean> {
		private final boolean autoPriceHandler;
		
		public PriceHandler(boolean auto) {autoPriceHandler = auto;}
		@Override
		public void onFocus(FocusEvent event) {
			setInputStatus();
		}
		@Override
		public void onValueChange(ValueChangeEvent<Boolean> event) {
			GWT.log("EditOfferView - " + (autoPriceHandler ? "Auto" : "Fixed") + " radio button changed value to: " + event.getValue(), null);
			if (event.getValue())
				setInputStatus();
		}
		
		private void setInputStatus() {
			strategyInput.setEnabled(autoPriceHandler);
			lowerBoundInput.setEnabled(autoPriceHandler);
			fixedPriceInput.setEnabled(!autoPriceHandler);
		}
	}
	
	// Wrappers around ListBox
	private HasValue<String> conditionWrapper = newListWrapper(conditionInput);
	private HasValue<String> strategyWrapper = newListWrapper(strategyInput);
	private HasValue<String> locationWrapper = newListWrapper(locationInput);
	
	private HasValue<String> newListWrapper(final ListBox input) {
		return new HasValueDummy<String>() {
			@Override
			public String getValue() {
				try {
					return input.getValue(input.getSelectedIndex());
				} catch (IndexOutOfBoundsException e) {
					return null;
				}
			}
			@Override
			public void setValue(String value) {
				// TODO fix!
				for (int i = 0; i < input.getItemCount(); i++) {
					if (input.getValue(i).equals(value)) {
						input.setSelectedIndex(i);
						DomEvent.fireNativeEvent(Document.get().createChangeEvent(), input);
						return;
					}
				}
				input.setSelectedIndex(0); // no match
				DomEvent.fireNativeEvent(Document.get().createChangeEvent(), input);
			}
		};
	}
	
	private HasValue<Integer> fixedPriceWrapper = new HasValueDummy<Integer>() {
		@Override
		public Integer getValue() {
			try {
				Double priceDbl = Double.valueOf(fixedPriceInput.getText());
				int price = (int) Math.round(priceDbl * 100);
				if (price > 0) 
					return price;
			} catch (NumberFormatException nfe) {
				// null
			} catch (NullPointerException npe) {
				// null
			}
			return null;
		}

		@Override
		public void setValue(Integer value) {
			if (value == null) {
				fixedPriceInput.setValue(null);
			} else if (value > 0) {
				fixedPriceInput.setValue(NumberUtil.getDisplayPrice(value, false, false));
			}
		}
	};
}
