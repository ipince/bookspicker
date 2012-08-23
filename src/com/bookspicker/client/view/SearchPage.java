package com.bookspicker.client.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.bookspicker.client.BooksPicker;
import com.bookspicker.client.HistoryToken;
import com.bookspicker.client.service.QueryService;
import com.bookspicker.client.service.QueryServiceAsync;
import com.bookspicker.client.service.StatService;
import com.bookspicker.client.service.StatServiceAsync;
import com.bookspicker.client.view.widgets.OfferTablePanel;
import com.bookspicker.client.view.widgets.buttons.BuyOfferButton;
import com.bookspicker.client.view.widgets.buttons.FindBestBundleButton;
import com.bookspicker.client.view.widgets.buttons.PickButton;
import com.bookspicker.client.view.widgets.buttons.RemoveButton;
import com.bookspicker.client.view.widgets.buttons.SelectOfferButton;
import com.bookspicker.shared.Book;
import com.bookspicker.shared.Bundle;
import com.bookspicker.shared.ClassBook;
import com.bookspicker.shared.Constants;
import com.bookspicker.shared.Item;
import com.bookspicker.shared.Offer;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * 
 * Controller for all the different views under the Search Page Panel.
 * 
 * @author Sinchan Banerjee, Jonathan Goldberg, Rodrigo Ipince
 * 
 */
public class SearchPage extends Composite implements HasHeader {

    private static SearchPageUiBinder uiBinder = GWT
    .create(SearchPageUiBinder.class);

    interface SearchPageUiBinder extends UiBinder<Widget, SearchPage> {
    }

    public static final boolean AUTO_COMPLETE = true;
    private static final String NO_BOOKS_FOUND_MSG = "We didn't find any books";
    private static final String CLASS_NOT_FOUND_MSG = "We didn't find a class";

    // Services
    private static final QueryServiceAsync queryService = GWT.create(QueryService.class);
    private static final StatServiceAsync statService = GWT.create(StatService.class);

    @UiField
    static SimplePanel headerViewWrapper;
    private static final HeaderView headerView = HeaderView.getHeaderView();

    @UiField
    static SimplePanel centralViewWrapper;

    @UiField
    static BundleView bundleView;
    static ResultsView resultsView;
    static BestBundleView bestBundleView;

    // State
    private static Bundle bundle;
    private static String currentQuery;
    private static String currentToken;

    // Used to keep track of which results to render
    // on the page
    private static int offerRequestCounter = 0;

    public SearchPage() {
        initWidget(uiBinder.createAndBindUi(this));

        bundle = new Bundle();


        headerView.setSelectedMenuItem(Page.SEARCH);
        headerViewWrapper.setWidget(headerView);
        centralViewWrapper.setStylePrimaryName(Resources.INSTANCE.style()
                .centralViewWrapper());
        resultsView = new ResultsView();
        bestBundleView = new BestBundleView(bundle);
        centralViewWrapper.setWidget(resultsView);
    }

    public void setState(String queryParam, String bundleParam,
            String offersParam, String displayedQuery) {

        String newToken = buildToken(queryParam, bundleParam, offersParam, displayedQuery);//resultsView.autoCompleteBox.getDisplayedQuery());

        GWT.log("current token: " + (currentToken == null ? "null" : currentToken), null);
        GWT.log("new token: " + newToken, null);

        if (newToken.equals(currentToken)) { // do nothing
            GWT.log("No change in state - doing nothing", null);
            return;
        }
        
        // If no bundle params, load what's in the cookie
        // Note: Cookies actually make this really complicated,
        // so I'm removing them for now
        //		if (bundleParam == null) {
        //			GWT.log("SearchPage - Using cookie: " + Cookies.getCookie("bundle"));
        //			bundleParam = Cookies.getCookie("bundle");
        //		}

        setBundleState(bundleParam, "true".equals(offersParam));
        setSearchState(queryParam, displayedQuery);
    }

    private void setSearchState(String query, String displayedQuery) {
        if (query == null) {
            clearSearch();
        } else {

            if (query.equals(currentQuery))
                return; // nothing to do

            // Prepare request
            String[] ids = query.split(",");
            List<String> queries = new ArrayList<String>();
            for (String id : ids) {
                queries.add(id.trim());
            }

            // Set actual state
            resultsView.autoCompleteBox.setText(displayedQuery);
            currentQuery = query;
            setHistoryToken(false);

            // Send the request
            resultsView.loadingIcon.removeStyleName(Resources.INSTANCE.style().loadingIconHidden());
            queryService.search(BooksPicker.getSchool(), queries, new SearchCallback(displayedQuery));
        }
    }

    /**
     * 
     */
    private void setBundleState(String param, boolean offerMode) {

        if (param == null) {
            clearBundle();
            displaySearch();
            setHistoryToken(false);
        } else {

            // Clear bundle as well (we might be removing
            // some books). But, in order to avoid querying
            // for data we already have, keep a copy of the
            // current books and re-populate the bundle from
            // it if necessary
            Bundle copy = new Bundle();
            for (Book book : bundle.getBooks())
                copy.addBook(book);
            clearBundle();

            // Prepare request
            String[] ids = null;
            ids = param.split(",");
            List<String> queries = new ArrayList<String>();
            for (String id : ids) {
                // Only search for ISBNs and classes!
                if (id.matches(Constants.ISBN_REGEX_STR) || 
                        id.matches(Constants.CLASS_REGEX_STR)) {

                    // Skip books that were already in bundle
                    if (!copy.containsBook(id)) {
                        queries.add(id);
                    } else {
                        addToBundle(copy.getBookByIsbn(id));
                        GWT.log("SearchPage - Skipping book that's already in bundle: " + id, null);
                    }
                }
            }

            // Send the request
            if (!queries.isEmpty()) {
                queryService.search(BooksPicker.getSchool(), queries, bundleHistoryCallback(offerMode));
            } else {
                if (offerMode && !bundle.isEmpty()) {
                    displayOffers();
                } else {
                    displaySearch();
                }
                setHistoryToken(false);
            }
        }
    }


    // ============= Handlers =============

    public static ClickHandler pickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            PickButton button = (PickButton) event.getSource();
            if (button.isEnabled()) {
                Book book = button.getAssociatedBook();
                addToBundle(book);
                setHistoryToken(true);
            }
        }
    };

    public static ClickHandler removeHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            // Get the book
            RemoveButton button = (RemoveButton) event.getSource();
            BundleBookView bookView = button.getAssociatedBookView();
            Book book = bookView.getDisplayedBook();

            GWT.log("Removing book from bundle: " + book.getTitle(), null);

            removeFromBundle(book, bookView);

            // Update state
            setHistoryToken(true);
        }
    };

    public static class SearchHandler implements ClickHandler, KeyPressHandler, KeyDownHandler, SelectionHandler<Suggestion> {

        @Override
        public void onClick(ClickEvent event) {
            findBooks(resultsView.autoCompleteBox.getText(), resultsView.autoCompleteBox.getDisplayedQuery());
        }

        @Override
        public void onKeyPress(KeyPressEvent event) {
            if (event.getCharCode() == KeyCodes.KEY_ENTER) {
                DeferredCommand.addCommand(new Command() {
                    @Override
                    public void execute() {
                        findBooks(resultsView.autoCompleteBox.getText(), resultsView.autoCompleteBox.getDisplayedQuery());
                    }
                });
            }
        }

        @Override
        public void onKeyDown(KeyDownEvent event) {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
                if (resultsView.autoCompleteBox.isSuggestionListShowing())
                    resultsView.autoCompleteBox.hideSuggestionList();
            }
        }

        @Override
        public void onSelection(SelectionEvent<Suggestion> event) {
            findBooks(resultsView.autoCompleteBox.getText(), resultsView.autoCompleteBox.getDisplayedQuery());
        }
    
        private void findBooks(String query, final String displayedQuery) {
            // Set history stuff
            currentQuery = query;
            setHistoryToken(true);

            if (query.isEmpty()) {
                resultsView.clearResults();
            } else {
                // Build request
                String[] raw = query.split(",");
                List<String> queries = new ArrayList<String>();
                for (int i = 0; i < raw.length; i++)
                    queries.add(raw[i].trim());

                resultsView.loadingIcon.removeStyleName(Resources.INSTANCE.style().loadingIconHidden());
                queryService.search(BooksPicker.getSchool(), queries, new SearchCallback(displayedQuery));
            }
        }
    }

    
    public static ClickHandler buyHandler = new ClickHandler() {
    	@Override
    	public void onClick(ClickEvent event) {
    		BuyOfferButton button = (BuyOfferButton) event.getSource();

    		Book book = button.getBook();
    		Offer clickedOffer = button.getOffer();
    		List<Offer> competingOffers = bundle.getBookOffers(book);

    		// Save stat
    		statService.logBuyClick(book.getIsbn(),
    				clickedOffer, competingOffers,
    				emptyCallback);

    		// Open window with offer
    		Window.open(button.getOffer().getUrl(), "_blank", "");
    	}
    };

    public static ClickHandler buyAllSelectedOffersHandler = new ClickHandler() {
    	@Override
    	public void onClick(ClickEvent event) {
    		Offer selectedOffer;
    		List<Offer> competingOffers;
    		for (Book book : bundle.getBooks()) {
    			selectedOffer = bundle.getSelectedOffer(book);
    			competingOffers = bundle.getBookOffers(book);
    			if (selectedOffer != null) {

    				// Save stat
    				statService.logBuyClick(book.getIsbn(),
    						selectedOffer, competingOffers,
    						emptyCallback);

    				// Open window with offer
    				Window.open(selectedOffer.getUrl(), "_blank", "");
    			} else {
    				GWT.log("SearchPage - Warning! There's a book with no selected offer in the bundle!", null);
    			}
    		}
    	}
    };

    public static ClickHandler selectHandler = new ClickHandler() {
    	@Override
    	public void onClick(ClickEvent event) {
    		// TODO(rodrigo): need to send to server if bundle
    		// has more than 1 book.
    		// and to update this stuff so that the bestBundleView's total
    		// price labels and the bundle itself is updated accordingly.

    		SelectOfferButton button = (SelectOfferButton) event.getSource();

    		OfferTablePanel panel = button.getPanel();
    		Offer offer = button.getOffer();

    		panel.updateSelected(offer);

    		SearchPage.bundle.setSelectedOffer(panel.getBook(), offer);
    		SearchPage.bestBundleView.refreshCosts(bundle);
    	}
    };

    public static ClickHandler findBestBundleHandler = new ClickHandler() {
    	@Override
    	public void onClick(ClickEvent event) {
    		FindBestBundleButton button = (FindBestBundleButton) event.getSource();
    		if (button.isEnabled()) {
    			displayOffers();
    			setHistoryToken(true);
    		}
    	}
    };

    public static ClickHandler backToSearchHandler = new ClickHandler() {
    	@Override
    	public void onClick(ClickEvent event) {
    		displaySearch();
    		setHistoryToken(true);
    	}
    };

    // ============= Callbacks =============

    	/**
    	 * Returns an AsyncCallback to be used in QueryService.getBookInfo().
    	 * 
    	 * Depending on insertInBundle, the callback will either insert the corresponding
    	 * books (returned by the QueryService) into the bundle or into the
    	 * search result area.
    	 */
    private AsyncCallback<Map<String, List<Item>>> bundleHistoryCallback(final boolean offerMode) { 
    	return new AsyncCallback<Map<String, List<Item>>>() {
    		@Override
    		public void onSuccess(Map<String, List<Item>> results) {
    			List<Item> result;
    			for (String query : results.keySet()) {
    				result = results.get(query);
    				// put book(s) in bundle
    				for (Item item : result) {
    					addToBundle(item.getBook());
    				}
    			}
    			if (offerMode && !bundle.isEmpty()) {
    				displayOffers();
    			} else {
    				displaySearch();
    			}
    			setHistoryToken(false);
    		}

    		@Override
    		public void onFailure(Throwable caught) {
    			// Do nothing....
    			if (offerMode && !bundle.isEmpty()) {
    				displayOffers();
    			} else {
    				displaySearch();
    			}
    			setHistoryToken(false);
    			GWT.log("There was an error retrieving the books", null);
    		}
    	};
    }

    public static AsyncCallback<Bundle> getOfferCallback(final int requestNumber) {
    	return new AsyncCallback<Bundle>() {
    		@Override
    		public void onSuccess(Bundle result) {
    			if (requestNumber == offerRequestCounter) {
    				bestBundleView.setLoadingVisible(false);
    				bundle = result;
    				bestBundleView.refreshBundle(result);
    				bundleView.setBookRemovability(true);
    			}
    		}

    		@Override
    		public void onFailure(Throwable caught) {
    			if (requestNumber == offerRequestCounter) {
    				bestBundleView.setLoadingVisible(false);
    				bundleView.setBookRemovability(true);
    			}
    			GWT.log(caught.getMessage(), caught);
    		}
    	};
    }

    private static AsyncCallback<Void> emptyCallback = new AsyncCallback<Void>() {
    	@Override
    	public void onSuccess(Void result) {
    		GWT.log("SearchPage - onSuccess at empty callback", null);
    	}
    	@Override
    	public void onFailure(Throwable caught) {
    		GWT.log("SearchPage - onFailure at empty callback", null);
    	}
    };

    // ============= Convenience methods =============

    private static void addToBundle(Book book) {
    	bundle.addBook(book);
    	bundleView.addBook(book);
    	resultsView.setBookPickability(book, false);
    }

    private static void removeFromBundle(Book book, BundleBookView bookView) {
    	// Remove from bundle and view
    	bundle.removeBook(book);
    	bundleView.removeBookView(bookView);
    	resultsView.setBookPickability(book, true);
    	if (bundleView.isInOfferMode()) {
    		if (bundle.isEmpty()) {
    			displaySearch();
    			setHistoryToken(true);
    		} else {
    			displayOffers();
    		}
    	}
    }

    private static void displaySearch() {
    	if (bundleView.isInOfferMode()) {
    		bundleView.toggleOfferMode();
    		bestBundleView.clear();
    		centralViewWrapper.setWidget(resultsView);
    	}
    }

    private static void displayOffers() {
    	if (!bundleView.isInOfferMode()) {
    		bundleView.toggleOfferMode();
    		bundleView.setBookRemovability(true);
    		centralViewWrapper.setWidget(bestBundleView);
    	}
    	bestBundleView.clear();
    	bestBundleView.setLoadingVisible(true);
    	bundleView.setBookRemovability(false);
    	offerRequestCounter++;
    	GWT.log("SearchPage - Sending request #" + offerRequestCounter + " for offers", null);
    	//TODO: Figure out why things are crashing with null pointer exceptions for non-MIT schools.
    	queryService.getOffersForBundle(BooksPicker.getSchool(), bundle, true, getOfferCallback(offerRequestCounter));
    }

    private static void clearBundle() {
    	bundle.clear();
    	bundleView.clear();
    	resultsView.setAllBooksPickable();
    }

    private static void clearSearch() {
    	currentQuery = "";
    	resultsView.searchTextBox.reset();
    	resultsView.autoCompleteBox.cleanSavedQuery();
    	resultsView.clearResults();
    }

    public static void reset(){
    	clearBundle();
    	clearSearch();
    	displaySearch();
    }

    private static void populateSearchResults(Map<String, List<Item>> results, String displayedQuery) {

    	List<Item> result;
    	// used to determine whether to show a header for each query or not
    	boolean multipleQueries = results.keySet().size() > 1;
    	for (String query : results.keySet()) {
    		result = results.get(query);

    		if (result == null) {
    			// class not found
    			resultsView.addInfoMessage(CLASS_NOT_FOUND_MSG + " matching \"" + displayedQuery + "\"");
    		} else {
    			if (result.isEmpty()) {
    				// No books found -> display differently depending on query type
    				if (query.matches(Constants.CLASS_REGEX_STR)) {
    					resultsView.addInfoMessage(NO_BOOKS_FOUND_MSG + " for " + displayedQuery + "." +
    							"<br /><br />This class probably requires no books, but we cannot guarantee it. Here's the <a href='http://course.mit.edu/" + displayedQuery + "' target='_blank'>class website</a> in case you wish to double-check." +
    							"<br /><br />Let us know if we're missing any of " + displayedQuery + "'s books through the feedback tab on the right.");
    				} else {
    					resultsView.addInfoMessage("<br />"+NO_BOOKS_FOUND_MSG + " matching \"" + displayedQuery + "\"");
    				}
    			} else {

    				// Show ClassBooks first
    				ClassBook cb;
    				boolean headerShown = false;
    				for (Item item : result) {
    					//TODO (Sinchan) - can you please change the info message to an error message. See how it looks when 
    					// you query for multiple books and one of the ISBN is broken. (e.g: 9780262033844, 6.003)

    					if (item == null) {
    						resultsView.addErrorMessage(NO_BOOKS_FOUND_MSG + " matching \"" + query + "\"<br />");
    						headerShown = true;
    					}

    					if (item instanceof ClassBook) {
    						cb = (ClassBook) item;
    						// Only display the class title for the first book!
    						if (!headerShown) {
    							if (cb.getSchoolClass().getTitle()!= null){
    								resultsView.addInfoMessage("Books for " + cb.getSchoolClass().getCode() + " - " + cb.getSchoolClass().getTitle());
    							}
    							else {
    								resultsView.addInfoMessage("Books for " + cb.getSchoolClass().getCode());
    							}

    							headerShown = true;
    						}
    						resultsView.addClassBook(cb);
    					} else if (item instanceof Book) {
    						// Only show header for regular title search if many queries were made (to distinguish)
    						if (multipleQueries && !headerShown) {
    							resultsView.addInfoMessage("Books matching \"" + query + "\":");
    							headerShown = true;
    						}
    						resultsView.addBook((Book) item);
    					}
    				}
    			}
    		}
    	}
    }

    // ============= History stuff =============

    private static void setHistoryToken(boolean addToStack) {
    	currentToken = buildToken();
    	Cookies.setCookie("searchState", currentToken);
    	if (addToStack)
    		History.newItem(currentToken, false);

    	//sets cookie for 36H
    	if (bundle.isEmpty()) {
    		// We remove the cookie because we don't want to
    		// save empty bundles to cookies. If so, we'll waste
    		// a server call searching for an empty book
    		Cookies.removeCookie("bundle");
    	} else {
    		Cookies.setCookie("bundle", bundle.getIsbnListOfAllBooks(),
    				new Date(System.currentTimeMillis() + 129600000L));
    	}
    }

    private static String buildToken() {
    	return buildToken(currentQuery, bundle.getIsbnListOfAllBooks(), (bundleView.isInOfferMode() ? "true" : null), resultsView.autoCompleteBox.getDisplayedQuery());
    }

    public static String buildToken(String queryParam, String bundleParam, String offersParam, String displayedQuery) {
    	StringBuilder sb = new StringBuilder();
    	sb.append(HistoryToken.SEARCH);
    	if ((bundleParam != null && !bundleParam.isEmpty()) || (queryParam != null && !queryParam.isEmpty()) ||
    			(offersParam != null && !offersParam.isEmpty())) {
    		sb.append(HistoryToken.PARAM_STARTER);
    	}

    	String delimeter = "";
    	if (bundleParam != null && !bundleParam.isEmpty()) {
    		sb.append(HistoryToken.PARAM_BUNDLE);
    		sb.append("=");
    		sb.append(bundleParam);
    		delimeter = HistoryToken.PARAM_DELIMETER;
    	}

    	if (queryParam != null && !queryParam.isEmpty()) {
    		sb.append(delimeter);
    		delimeter = HistoryToken.PARAM_DELIMETER;
    		sb.append(HistoryToken.PARAM_QUERY);
    		sb.append("=");
    		sb.append(queryParam);
    		sb.append(HistoryToken.PARAM_DELIMETER);
    		sb.append(HistoryToken.DISPLAYED_QUERY);
    		sb.append("=");
    		sb.append(displayedQuery);
    	}

    	if (offersParam != null && !offersParam.isEmpty()) {
    		sb.append(delimeter);
    		delimeter = HistoryToken.PARAM_DELIMETER;
    		sb.append(HistoryToken.PARAM_OFFERS);
    		sb.append("=");
    		sb.append(offersParam);
    	}

    	return sb.toString();
    }

    @Override
    public void updateLoginContainer() {
    	headerView.updateLoginContainer();
    }

    @Override
    protected void onLoad() {
    	super.onLoad();
    	headerViewWrapper.setWidget(headerView);
    	headerView.setSelectedMenuItem(Page.SEARCH);
    }

    public static class SearchCallback implements AsyncCallback<Map<String, List<Item>>> {
    	private final String  _displayedQuery; 

    	public SearchCallback(String displayedQuery) {
    		_displayedQuery = displayedQuery;
    	}

    	@Override
    	public void onFailure(Throwable caught) {
    		resultsView.clearResults();
    		resultsView.loadingIcon.addStyleName(Resources.INSTANCE.style().loadingIconHidden());
    		// Do nothing....
    		GWT.log("There was an error retrieving the books", null);

    	}

    	@Override
    	public void onSuccess(Map<String, List<Item>> result) {
    		resultsView.clearResults();
    		populateSearchResults(result, _displayedQuery);
    		resultsView.loadingIcon.addStyleName(Resources.INSTANCE.style().loadingIconHidden());
    	}
    }

}
