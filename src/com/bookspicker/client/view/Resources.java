package com.bookspicker.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

/**
 * Resources used by the entire application.
 */
public interface Resources extends ClientBundle {
	public static final Resources INSTANCE = GWT.create(Resources.class);

	public interface Style extends CssResource {
		String title();
		
		String bpPanelTitle();

		String logo();

		String bpPanel();

		String bpPanelContent();

		String bpPanelLeftBorder();

		String bpPanelRightBorder();

		String bpPanelBottomBorder();

		String bpPanelTopBorder();

		String bpPanelTopRightBorder();

		String bpPanelBottomLeftBorder();

		String bpPanelBottomRightBorder();

		String bpPanelTopLeftBorder();

		String bundleView();

		String resultsView();

		String resultsBookDetails();

		String resultsBookTitle();

		String bpButton();

		// String selected();

		String searchBoxWrapper();

		String resultsList();

		String resultsBookImage();

		String pickButton();

		String searchBox();

		String suggestionBox();

		String bundleBooksList();

		String bundleBookDetails();

		String bundleBookTitle();

		String bundleBookImage();

		String removeButton();

		String mainBundleButtonWrapper();

		String mainBundleButton();

		String offersTable();

		String headerRow();

		String offerRow();

		String offerTableWrapper();

		String bestBundleView();

		String collapseToggleButton();

		String centralViewWrapper();

		String offerViewBookDetail();

		String selectedOfferRow();

		String offersList();

		String bookOfferView();

		String selectedLabel();

		String offerButtonPanel();

		String savingsLabel();

		String totalCostLabel();

		String description();

		String priceSummaryGrid();

		String loadingPanel();
		
		String loadingIcon();
		
		String loadingIconHidden();

		String navAbout();

		String navFaq();

		String navSearch();
		
		String navSell();

		String navHomeSelected(); 

		String navAboutSelected();

		String navFaqSelected();

		String navSearchSelected();
		
		String navSellSelected();

		String navHome();

		String navigation();

		String informationPageContent();
		
		String sellerPageWrapper();
		
		String buyPageWrapper();

		String emptyBundleLabel();

		String resultsInfoMessage();

		String selectedOfferPrice();

		String homeSearchBoxWrapper();

		String buyAllOffersButton();

		String bestBundleHeaderDiv();

		String editOfferView();
		
		String editOfferViewContent();

		String editOfferDetailsTable();

		String autoPricingOptionPanel();
		
		String fixedPricingOptionPanel();

		String commentsInput();

		String editOfferBookPanel();

		String editOfferDetailsPanel();

		String orText();

		String editOfferError();

		String loginButtonContainer();

		String topBarContainer();

		String logoutButton();

		String sellPageLoginButtonWrapper();

		String sellPageLoginStuffContainer();

		String offerManagementPanel();

		String greetingLabel();

		String buyOfferViewContent();

		String buyOfferDetailsPanel();

		String buyOfferBookDetailsPanel();

		String buyOfferViewHeading();

		String buyofferTextAndButtonTable();

		String offerManagementTable();

		String editOfferPriceContainer();

		String resultsBookViewNote();

		String bookDetailedView();

		String mustLoginLabel();

		String strategyDescriptionCell();

		String headerView();

		String resultsErrorMessage();

		String offerStore();

		String offerViewBookDetailImage();

		String selectSchoolSearchBox();
	}

	// CSS Styles for all of BP
	@Source("com/bookspicker/client/bookspicker.css")
	Style style();

	// Logo
	@Source("com/bookspicker/client/view/images/logo.png")
	ImageResource logo();
	
	// School Specific Logos
	@Source("com/bookspicker/client/view/images/mitLogo.png")
	ImageResource mitLogo();
	@Source("com/bookspicker/client/view/images/uchicagoLogo.png")
	ImageResource uchicagoLogo();
	@Source("com/bookspicker/client/view/images/dartmouthLogo.png")
	ImageResource dartmouthLogo();
	@Source("com/bookspicker/client/view/images/northwesternLogo.png")
	ImageResource northwesternLogo();

	// Find Best Bundle Button
	@Source("com/bookspicker/client/view/images/findBestBundleButton.png")
	ImageResource findBestBundleButton();
	
	// Find Best Bundle Button Disabled
	@Source("com/bookspicker/client/view/images/findBestBundleButtonDisabled.png")
	ImageResource findBestBundleButtonDisabled();

	// Back to Search Button
	@Source("com/bookspicker/client/view/images/backToSearch.png")
	ImageResource backToSearchButton();
	
	// Search Button
	@Source("com/bookspicker/client/view/images/searchButton.png")
	ImageResource searchButton();
	
	// Search Button
	@Source("com/bookspicker/client/view/images/searchButtonBlack.png")
	ImageResource searchButtonBlack();

	// Remove Button
	@Source("com/bookspicker/client/view/images/removeButton.png")
	ImageResource removeButton();

	// Pick Button
	@Source("com/bookspicker/client/view/images/pickButton.png")
	ImageResource pickButton();

	// Pick Button Disabled
	@Source("com/bookspicker/client/view/images/pickButtonDisabled.png")
	ImageResource pickButtonDisabled();

	// Buy Offer Button
	@Source("com/bookspicker/client/view/images/buyOfferButton.png")
	ImageResource buyOfferButton();
	
	// Buy Offer Button Disabled
	@Source("com/bookspicker/client/view/images/buyOfferButtonDisabled.png")
	ImageResource buyOfferButtonDisabled();

	// Buy All Offer Button
    @Source("com/bookspicker/client/view/images/buyAllOffersButton.png")
    ImageResource buyAllOffersButton();
	
	
	// Separator between the Buy Offer Button and the Select Offer Button
	@Source("com/bookspicker/client/view/images/buySelectSeparator.png")
	ImageResource buySelectSeparator();

	// Select Offer Button
	@Source("com/bookspicker/client/view/images/selectOfferButton.png")
	ImageResource selectOfferButton();
	
	// Change School Setting Button
	@Source("com/bookspicker/client/view/images/schoolChangeButton.png")
	ImageResource schoolChangeButton();
	
	// Facebook Login Button
	@Source("com/bookspicker/client/view/images/facebookLoginButton.png")
	ImageResource regFacebookLoginButton();
	
	// Facebook Login Button for Site Header
	@Source("com/bookspicker/client/view/images/headerFacebookLoginButton.png")
	ImageResource headerFacebookLoginButton();
	
	// Save Button
	@Source("com/bookspicker/client/view/images/saveButton.png")
	ImageResource saveButton();
	
	// Cancel Button
	@Source("com/bookspicker/client/view/images/cancelButton.png")
	ImageResource cancelButton();
	
	// Edit Button
	@Source("com/bookspicker/client/view/images/editButton.png")
	ImageResource editButton();
	
	// Edit Button Disabled
	@Source("com/bookspicker/client/view/images/editButtonDisabled.png")
	ImageResource editButtonDisabled();
	
	// Re-Activate Button
	@Source("com/bookspicker/client/view/images/reactivateButton.png")
	ImageResource reactivateButton();
	
	// Re-Activate Button Disabled
	@Source("com/bookspicker/client/view/images/reactivateButtonDisabled.png")
	ImageResource reactivateButtonDisabled();
	
	// De-Activate Button
	@Source("com/bookspicker/client/view/images/deactivateButton.png")
	ImageResource deactivateButton();
	
	// De-Activate Button Disabled
	@Source("com/bookspicker/client/view/images/deactivateButtonDisabled.png")
	ImageResource deactivateButtonDisabled();
	
	// Delete Button
	@Source("com/bookspicker/client/view/images/deleteButton.png")
	ImageResource deleteButton();
	
	// Delete Button Disabled
	@Source("com/bookspicker/client/view/images/deleteButtonDisabled.png")
	ImageResource deleteButtonDisabled();
	
	// Add new listing Button
	@Source("com/bookspicker/client/view/images/addNewListingButton.png")
	ImageResource addNewListingButton();
	
	
	// Borders for BPPanel (go clockwise based on number
	// i.e. bpPanelBorder1 is the top border and then
	// bpPanelBorder2 is the right border etc.)
	@Source("com/bookspicker/client/view/images/bpPanelBorder1.gif")
	@ImageOptions(repeatStyle = RepeatStyle.Horizontal)
	ImageResource bpPanelBorder1();

	@Source("com/bookspicker/client/view/images/bpPanelBorder2.gif")
	@ImageOptions(repeatStyle = RepeatStyle.Vertical)
	ImageResource bpPanelBorder2();

	@Source("com/bookspicker/client/view/images/bpPanelBorder3.gif")
	@ImageOptions(repeatStyle = RepeatStyle.Horizontal)
	ImageResource bpPanelBorder3();

	@Source("com/bookspicker/client/view/images/bpPanelBorder4.gif")
	@ImageOptions(repeatStyle = RepeatStyle.Vertical)
	ImageResource bpPanelBorder4();

	// Corner Images for BPPanel (go clockwise based on number
	// i.e. bpPanelCorner1 is the top-left Corner and then
	// bpPanelCorner2 is the top-right border etc.)
	@Source("com/bookspicker/client/view/images/bpPanelCorner1.gif")
	ImageResource bpPanelCorner1();

	@Source("com/bookspicker/client/view/images/bpPanelCorner2.gif")
	ImageResource bpPanelCorner2();

	@Source("com/bookspicker/client/view/images/bpPanelCorner3.gif")
	ImageResource bpPanelCorner3();

	@Source("com/bookspicker/client/view/images/bpPanelCorner4.gif")
	ImageResource bpPanelCorner4();

	@Source("com/bookspicker/client/view/images/ajax-loader.gif")
	ImageResource loadingIcon();
	
	@Source("com/bookspicker/client/view/images/loaderSmall.gif")
	ImageResource loadingIconSmall();

	// Navigation Button Images
	@Source("com/bookspicker/client/view/images/nav_home.gif")
	ImageResource navHomeImg();

	@Source("com/bookspicker/client/view/images/nav_home_over.gif")
	ImageResource navHomeOverImg();

	@Source("com/bookspicker/client/view/images/nav_search.gif")
	ImageResource navSearchImg();

	@Source("com/bookspicker/client/view/images/nav_search_over.gif")
	ImageResource navSearchOverImg();
	
	@Source("com/bookspicker/client/view/images/nav_sell.gif")
	ImageResource navSellImg();

	@Source("com/bookspicker/client/view/images/nav_sell_over.gif")
	ImageResource navSellOverImg();

	@Source("com/bookspicker/client/view/images/nav_faq.gif")
	ImageResource navFaqImg();

	@Source("com/bookspicker/client/view/images/nav_faq_over.gif")
	ImageResource navFaqOverImg();

	@Source("com/bookspicker/client/view/images/nav_about.gif")
	ImageResource navAboutImg();

	@Source("com/bookspicker/client/view/images/nav_about_over.gif")
	ImageResource navAboutOverImg();
	
	@Source("com/bookspicker/client/view/images/help.gif")
	ImageResource helpIcon();

	@Source("com/bookspicker/client/view/images/noImageAvailable.jpg")
	ImageResource noImageAvailable();
	
	@Source("com/bookspicker/client/view/images/noImageAvailableSmall.jpg")
	ImageResource noImageAvailableSmall();
	
}
