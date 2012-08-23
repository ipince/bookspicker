package com.bookspicker.client.view;

import com.bookspicker.client.HistoryToken;
import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.Resources.Style;
import com.bookspicker.client.view.widgets.buttons.NavigationButton;
import com.google.gwt.user.client.ui.Grid;

public class Navigation extends Grid {

	private static Navigation INSTANCE;

	private Style style;
	NavigationButton home;
	NavigationButton search;
	NavigationButton sell;
	NavigationButton faq;
	NavigationButton about;

	public Navigation() {
		super(1, 5);

		style = Resources.INSTANCE.style();

		this.setStylePrimaryName(style.navigation());

		home = new NavigationButton(style.navHome(), style.navHomeSelected(),
				HistoryToken.HOME);
		this.setWidget(0, 0, home);

		search = new NavigationButton(style.navSearch(), style
				.navSearchSelected(),
				HistoryToken.SEARCH);
		this.setWidget(0, 1, search);
		
		sell = new NavigationButton(style.navSell(), style.navSellSelected(),
				HistoryToken.SELLER);
		this.setWidget(0, 2, sell);

		faq = new NavigationButton(style.navFaq(), style.navFaqSelected(),
				HistoryToken.FAQ);
		this.setWidget(0, 3, faq);

		about = new NavigationButton(style.navAbout(),
				style.navAboutSelected(), HistoryToken.ABOUT);
		this.setWidget(0, 4, about);

	}

	private void resetAll() {
		home.setSelected(false);
		search.setSelected(false);
		sell.setSelected(false);
		faq.setSelected(false);
		about.setSelected(false);
	}

	public static Navigation getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Navigation();
		}
		return INSTANCE;
	}

	public void setSelected(Page pageType) {
		resetAll();
		switch (pageType) {
		case HOME:
			home.setSelected(true);
			break;
		case SEARCH:
			search.setSelected(true);
			break;
		case SELL:
			sell.setSelected(true);
			break;
		case FAQ:
			faq.setSelected(true);
			break;
		case ABOUT:
			about.setSelected(true);
			break;
		}
	}
}
