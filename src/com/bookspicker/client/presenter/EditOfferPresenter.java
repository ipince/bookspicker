package com.bookspicker.client.presenter;

import java.util.ArrayList;
import java.util.List;

import com.bookspicker.client.BooksPicker;
import com.bookspicker.client.service.LocalOfferServiceAsync;
import com.bookspicker.client.event.EditOfferCancelledEvent;
import com.bookspicker.client.event.OfferSavedEvent;
import com.bookspicker.client.service.QueryServiceAsync;
import com.bookspicker.client.view.widgets.ListChangeHandler;
import com.bookspicker.shared.AuthenticationException;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.Bundle;
import com.bookspicker.shared.Constants;
import com.bookspicker.shared.Item;
import com.bookspicker.shared.LocalOffer;
import com.bookspicker.shared.Location;
import com.bookspicker.shared.NumberUtil;
import com.bookspicker.shared.Offer;
import com.bookspicker.shared.School;
import com.bookspicker.shared.User;
import com.bookspicker.shared.LocalOffer.Condition;
import com.bookspicker.shared.LocalOffer.Strategy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Controls the EditOfferView.
 * 
 * @author Rodrigo Ipince
 */
public class EditOfferPresenter implements Presenter {
	
	public interface Display {
		// Offer detail containers
		Book getBook();
		HasValue<String> getBookInput();
		HasValue<String> getClassInput();
		HasValue<String> getCondition();
		ListBox getConditionList(); // TODO(rodrigo): nasty dependency on ListBox!
		boolean getAutoPrice();
		HasValue<String> getStrategy();
		ListBox getStrategyList(); // TODO(rodrigo): nasty dependency on ListBox!
		HasValue<String> getLowerBound();
		HasValue<Integer> getFixedPrice();
		HasValue<String> getLocation();
		HasValue<String> getComments();
		
		// Buttons and interactive stuff
		HasValueChangeHandlers<String> getCurrentBookHandler();
		HasClickHandlers getSaveButton();
		HasClickHandlers getCancelButton();
		void setBookLoading(boolean loading);
		void setBook(Book book); // TODO: remove dependency
		void setPricesLoading(boolean loading);
		void setAutoPrice(boolean autoPrice);
		void setAutoPricePreview(int price);
		void setPrices(int lowestSaleNew,
				int lowestSaleUsed,
				int lowestInternational,
				int highestBuyback);
		void setError(String message);
		
		// Utility
		void setBound(boolean bound);
		boolean isBound();
		Widget asWidget();
	}
	
	private final LocalOfferServiceAsync offerService;
	private final QueryServiceAsync queryService;
	private final HandlerManager eventBus;
	private final Display display; // View
	
	/**
	 * Model. The offer we're presenting. Should never be null.
	 */
	private LocalOffer offer; // Model
	
	// Part of the model of what we're presenting too (prices)
//	private int lowestNew = 0;
//	private int lowestUsed = 0;
	private List<Offer> competingOffers = new ArrayList<Offer>();
	
	public EditOfferPresenter(LocalOfferServiceAsync offerService,
			QueryServiceAsync queryService,
			HandlerManager eventBus, Display view) {
		this.offerService = offerService;
		this.queryService = queryService;
		this.eventBus = eventBus;
		this.display = view;
		this.offer = new LocalOffer();
	}
	
	public void setOffer(LocalOffer offer) {
		if (offer == null) {
			this.offer = new LocalOffer();
		} else {
			this.offer = offer;
		}
	}

	@Override
	public void go(HasWidgets container) {
		bind();
		display.setError(""); // clear errors
		populateDisplayFromOffer();
		container.clear();
		container.add(display.asWidget());
	}

	private void bind() {
		if (!display.isBound()) {
			
			// Listen on strategy changes
			ListChangeHandler strategyHandler = new ListChangeHandler(new Runnable() {
				@Override
				public void run() {
					populateStrategyFromDisplay(offer);
					updateAutoPrice();
				}
			});
			display.getStrategyList().addChangeHandler(strategyHandler);
			display.getStrategyList().addKeyDownHandler(strategyHandler);
			
			// Listen on condition changes
			ListChangeHandler conditionHandler = new ListChangeHandler(new Runnable() {
				@Override
				public void run() {
					populateConditionFromDisplay(offer);
					updateAutoPrice();
				}
			});
			display.getConditionList().addChangeHandler(conditionHandler);
			display.getConditionList().addKeyDownHandler(conditionHandler);
			
			// Listen on lower bound changes
			display.getLowerBound().addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					populateLowerBoundFromDisplay(offer);
					updateAutoPrice();
				}
			});
			
			display.getSaveButton().addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					doSave();
				}
			});

			display.getCancelButton().addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					eventBus.fireEvent(new EditOfferCancelledEvent());
				}
			});

			display.getCurrentBookHandler().addValueChangeHandler(new ValueChangeHandler<String>() {

				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					String input = event.getValue();
					GWT.log("EditOfferPresenter - Book input value changed to: " + input, null);
					if (!input.isEmpty()) {
						if (input.matches(Constants.ISBN_REGEX_STR)) {
							display.setError("");
							if (offer.getBook() == null || !offer.getBook().getIsbn().equals(input))
								updateBook(input);
						} else {
							display.setError("Please enter a valid ISBN!");
						}
					}
				}
			});
			display.setBound(true);
		}
	}

	private AsyncCallback<List<Item>> bookCallback = new AsyncCallback<List<Item>>() {
		@Override
		public void onSuccess(List<Item> result) {
			if (result.isEmpty() || result.get(0) == null) {
				display.setError("Sorry, we couldn't find a book matching your query");
				GWT.log("EditOfferPresenter - Book not found!", null);
				offer.setBook(null);
				display.setBook(null);
				competingOffers.clear();
				updateAutoPrice();
				return;
			} else {
				display.setError("");
			}
			
			if (result.size() == 1) { // Only one book found!
				Item item = result.get(0);
				offer.setBook(item.getBook());
				display.setBook(item.getBook());
				updatePrices(item.getBook());
			} else {
				// TODO More than one book found - wtf?
				offer.setBook(null);
				display.setBook(null);
				competingOffers.clear();
				updateAutoPrice();
			}
		}
		
		@Override
		public void onFailure(Throwable caught) {
			GWT.log("EditOfferPresenter - Error while finding book!", null);
			offer.setBook(null);
			display.setBook(null);
			competingOffers.clear();
			updateAutoPrice();
			display.setError("Oops! An error ocurred on the server while trying " +
					"to retrieve the book information. Please try " +
					"again! If the error persists, check your internet " +
					"connectivity or refresh the website. Sorry!");
		}
	};
	
	protected AsyncCallback<Bundle> priceCallback = new AsyncCallback<Bundle>() {
		@Override
		public void onSuccess(Bundle result) {
			List<Offer> offers = result.getBookOffers(offer.getBook());
			Offer lowestUsedOffer = null;
			Offer lowestNewOffer = null;
			Offer lowestInternationalOffer = null;
			for (Offer offer : offers) {
				// Check international editions first
				if (offer.isInternationalEdition()) {
					if (lowestInternationalOffer == null) {
						lowestInternationalOffer = offer;
					} else if (offer.getTotalPrice() < lowestInternationalOffer.getTotalPrice()) {
						lowestInternationalOffer = offer;
					}
						
				} else { // Non-international editions
					if (offer.isNew()) {
						if (lowestNewOffer == null)
							lowestNewOffer = offer;
						else if (offer.getTotalPrice() < lowestNewOffer.getTotalPrice())
							lowestNewOffer = offer;					
					} else { // used
						if (lowestUsedOffer == null)
							lowestUsedOffer = offer;
						else if (offer.getTotalPrice() < lowestUsedOffer.getTotalPrice())
							lowestUsedOffer = offer;
					}
				}
			}
			
			int lowestNew = lowestNewOffer != null ? lowestNewOffer.getTotalPrice() : 0;
			int lowestUsed = lowestUsedOffer != null ? lowestUsedOffer.getTotalPrice() : 0;
			int lowestInternational = lowestInternationalOffer != null ? lowestInternationalOffer.getTotalPrice() : 0;
			competingOffers = offers;
			updateAutoPrice();
			display.setPricesLoading(false);
			display.setPrices(lowestNew, lowestUsed, lowestInternational, 0);
		}
		@Override
		public void onFailure(Throwable caught) {
			competingOffers.clear();
			updateAutoPrice();
			display.setPricesLoading(false);
			display.setPrices(0, 0, 0, 0);
			display.setError("Oops! An error ocurred on the server while trying " +
					"to retrieve the market prices. Please try " +
					"again! If the error persists, check your internet " +
					"connectivity or refresh the website. Sorry!");
		}
	};
	
	
	private void doSave() {
		LocalOffer newOffer = populateOfferFromDisplay();
		if (newOffer != null) {
			// Copy over offer details.
			// Why not just 'save' the newOffer? Because it will
			// create a new offer! We don't want that if we're
			// editing an existing offer (so we want to conserve
			// the id and all metadata).
			// TODO(rodrigo): we should probably change this so
			// that we have a LeanLocalOffer that's used to get
			// user input and all that, and the real LocalOffer
			// with all the metadata lives only on the server.
			offer.copy(newOffer);
			
			offerService.saveOffer(offer, new AsyncCallback<User>() {

				@Override
				public void onSuccess(User result) {
					eventBus.fireEvent(new OfferSavedEvent(result));
				}

				@Override
				public void onFailure(Throwable caught) {
					if (caught instanceof AuthenticationException) {
						display.setError("You need to be logged in to edit this offer");
					}
					GWT.log("An error escaped: " + caught.getMessage(), null);
				}
			});
		}
	}
	
	private void updateBook(String isbn) {
		GWT.log("EditOfferPresenter - Fetching book with isbn: " + isbn, null);
		display.setBookLoading(true);
		queryService.getBookInfo(School.NONE, isbn, bookCallback);
	}
	
	private void updatePrices(Book book) {
		// HACK!! We create a bundle just because 
		// query service needs a bundle... fugly!
		Bundle dummyBundle = new Bundle();
		dummyBundle.addBook(book);
		
		display.setPricesLoading(true);
		queryService.getOffersForBundle(BooksPicker.getSchool(), dummyBundle, false, priceCallback);
	}
	
	/**
	 * Updates the auto-price display if and only if we have
	 * enough data to do so.
	 * 
	 * This should be called every time anything that could affect
	 * the auto-price changes. Be sure to update the model (Offer)
	 * and reference prices before calling this method though.
	 * 
	 * More specifically, this should be called whenever the prices
	 * change, when the strategy or lower bound changes, and when
	 * the condition changes. In theory, we should also call it
	 * whenever the book changes, but since a book change triggers
	 * a price change, calling it after prices change is good enough.
	 */
	private void updateAutoPrice() {
		// We need:
		//  - The condition (inside offer)
		//  - The strategy (inside offer)
		//  - The list price? (inside offer)
		//  - The reference price(s) (external)
		offer.calculatePrice(competingOffers, null);
		display.setAutoPricePreview(offer.getAutoPrice());
	}
	
	// Model -> View and View -> Model
	
	/**
	 * Does input validation and populates an Offer object
	 * with the inputs if they're valid.
	 * 
	 * @return true iff the inputs are valid
	 */
	private LocalOffer populateOfferFromDisplay() {
		
		// Use a new (clean) offer object
		LocalOffer newOffer = new LocalOffer();
		// Set the offer's school to the current school!
		newOffer.setSchool(BooksPicker.getSchool());
		
		Book book = display.getBook();
		if (book == null) {
			display.setError("Please choose a book by inputting an " +
					"ISBN. It should be a 10 or 13 digit number with no dashes.");
			return null;
		}
		newOffer.setBook(book);
		
		String classCode = display.getClassInput().getValue();
		if (classCode != null && !classCode.isEmpty()) {
			if (classCode.matches(Constants.CLASS_REGEX_STR)) {
				newOffer.setClassCode(classCode);
			} else {
				display.setError("The class code your provided ( " + 
						classCode + ") doesn't look like an MIT " +
						"class. Make sure it looks like '21F.222' " +
						"and don't include J's at the end");
				return null;
			}
		}
		
		String error;
		error = populateConditionFromDisplay(newOffer);
		if (error != null) {
			display.setError(error);
			return null;
		}

		
		if (display.getAutoPrice()) {
			newOffer.setAutoPricing(true);
			
			error = populateStrategyFromDisplay(newOffer);
			if (error != null) {
				display.setError(error);
				return null;
			}
			
			error = populateLowerBoundFromDisplay(newOffer);
			if (error != null) {
				display.setError(error);
				return null;
			}
			
		} else {
			newOffer.setAutoPricing(false);
			Integer price = display.getFixedPrice().getValue();
			if (price == null) {
				display.setError("The price you entered is invalid. Please " +
						"set it in the form 'xx.xx'. It must be a positive number");
				return null;
			}
			newOffer.setFixedPrice(price);
		}
		
		try {
			Location loc = Location.valueOf(display.getLocation().getValue());
			newOffer.setLocation(loc);
		} catch (Exception e) {
			newOffer.setLocation(null);
		}
		
		String comments = display.getComments().getValue();
		if (comments != null && !comments.isEmpty()) {
			if (comments.length() > 250) {
				display.setError("Please enter a shorter comment! Don't write a novel, keep it short and sweet :)");
				return null;
			}
			newOffer.setComments(comments);
		}
		
		// Everything seems good, so return newly populated offer
		return newOffer;
	}
	
	private String populateConditionFromDisplay(LocalOffer offer) {
		String condStr = display.getCondition().getValue();
		try {
			Condition cond = Condition.valueOf(condStr);
			offer.setBookCondition(cond);
			return null;
		} catch (Exception e) {
			offer.setBookCondition(null);
		}
		return "Please select a condition from the dropdown";
	}
	
	private String populateStrategyFromDisplay(LocalOffer offer) {
		String stratStr = display.getStrategy().getValue();
		try {
			Strategy strategy = Strategy.valueOf(stratStr);
			offer.setStrategy(strategy);
			return null;
		} catch (Exception e) {
			offer.setStrategy(null);
		}
		return "You need to select a strategy if you want to use automatic pricing";
	}
	
	private String populateLowerBoundFromDisplay(LocalOffer offer) {
		String input = display.getLowerBound().getValue();
		Integer lower = parseInteger(input);

		offer.setLowerBoundPrice(lower);
		if (lower == null || lower < 0) {
			return "The lower bound you entered is invalid. " +
					"Please set it in the form 'xx.xx' or leave it blank " +
					"to not set a lower bound. Negative numbers are not allowed!";
		}
		return null;
	}
	
	private Integer parseInteger(String input) {
		if (input.isEmpty()) {
			return 0;
		} else {
			try {
				Double priceDbl = Double.valueOf(input);
				return (int) Math.round(priceDbl * 100);
			} catch (NumberFormatException nfe) {
				// do nothing (lower = null)
			} catch (NullPointerException npe) {
				// do nothing (lower = null)
			}
		}
		return null;
	}
	
	
	/**
	 * Fills out the view with the Offer's details
	 */
	private void populateDisplayFromOffer() {
		// Book
		if (offer.getBook() != null) {
			display.getBookInput().setValue(offer.getBook().getIsbn());
			updatePrices(offer.getBook());
		} else {
			display.getBookInput().setValue(null);
			display.setPricesLoading(false);
			competingOffers.clear();
			updateAutoPrice();
			display.setPrices(0, 0, 0, 0);
		}
		display.setBook(offer.getBook());
		
		// Class
		display.getClassInput().setValue(offer.getClassCode());
		
		// Condition
		if (offer.getBookCondition() != null) {
			display.getCondition().setValue(offer.getBookCondition().toString());
		} else {
			display.getCondition().setValue(null);
		}
		
		// Price
		if (offer.isAutoPricing()) {
			display.setAutoPrice(true);
			
			if (offer.getStrategy() != null) {
				display.getStrategy().setValue(offer.getStrategy().toString());
			} else {
				display.getStrategy().setValue(null);
			}
			
			// Set lower bound and reset fixed price
			Integer value = offer.getLowerBoundPrice();
			if (value != null && value > 0) {
				display.getLowerBound().setValue(NumberUtil.getDisplayPrice(value, false, false));
			} else {
				display.getLowerBound().setValue(null);
			}
			display.getFixedPrice().setValue(null);
		} else {
			display.setAutoPrice(false);
			display.getFixedPrice().setValue(offer.getFixedPrice());
			display.getLowerBound().setValue(null);
		}
		
		// Location
		if (offer.getLocation() != null) {
			display.getLocation().setValue(offer.getLocation().toString());
		} else {
			display.getLocation().setValue(null);
		}
		
		// Comments
		display.getComments().setValue(offer.getComments());
	}
}
