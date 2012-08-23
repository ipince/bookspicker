package com.bookspicker.client.view;

import com.bookspicker.client.view.Resources;
import com.bookspicker.client.presenter.OfferManagementPresenter.LoginDisplay;
import com.bookspicker.client.view.widgets.buttons.FacebookLoginButton;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Displays login button and description so user can log in.
 * 
 * @author Rodrigo Ipince
 */
public class LoginView extends Composite implements LoginDisplay {

	private final FlowPanel contentContainer = new FlowPanel();
	
	private final FacebookLoginButton loginButton;
	
	private boolean bound;
	
	public LoginView(boolean includeDescription) {
		initWidget(contentContainer);
		
		if (includeDescription) {
			contentContainer.addStyleName(Resources.INSTANCE.style().sellPageLoginStuffContainer());
			contentContainer.add(new HTML("<b>Now you can sell your books on BooksPicker! Log in to get started.</b><br />"));

			loginButton = new FacebookLoginButton(Resources.INSTANCE.regFacebookLoginButton());
			FlowPanel loginWrapper = new FlowPanel();
			loginWrapper.addStyleName(Resources.INSTANCE.style().sellPageLoginButtonWrapper());
			loginWrapper.add(loginButton);
			contentContainer.add(loginWrapper);
			
			contentContainer.add(new HTML("We use Facebook to protect our buyers " +
					"from bogus sellers, not that you're one of them of course...<br/>" +
					"When applicable, we also use your friend information to match " +
					"you up with a buyer who's 'socially close' to you. And hey, it's " +
					"one less password to remember!<br />"));
			contentContainer.add(new HTML("Don't worry, we will never post anything " +
			"to your wall without your permission."));
		}
		else{
			loginButton = new FacebookLoginButton(Resources.INSTANCE.headerFacebookLoginButton());
			contentContainer.add(loginButton);
		}
		
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public HasClickHandlers getLoginButton() {
		return loginButton;
	}

	@Override
	public boolean isBound() {
		return bound;
	}

	@Override
	public void setBound(boolean bound) {
		this.bound = bound;
	}

}
