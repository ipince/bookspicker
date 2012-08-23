package com.bookspicker.client.view;

import com.bookspicker.client.HistoryToken;
import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.widgets.buttons.SchoolChangeButton;
import com.bookspicker.client.presenter.LoginHandler;
import com.bookspicker.client.presenter.LogoutHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class HeaderView extends Composite {
	interface MyUiBinder extends UiBinder<Widget, HeaderView> {}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	
	@UiField
	DockLayoutPanel headerPanel;
	@UiField
	Image logo;
	@UiField
	Navigation nav;
	@UiField
	FlowPanel topBarContainer;
	@UiField
	SimplePanel loginContainer;
	//@UiField
	//SimplePanel changeSchoolContainer;
	
	private HTML logout = new HTML("<u>Logout</u>");
	private LoginView login = new LoginView(false);
	
	private static final HeaderView INSTANCE = new HeaderView();

	private HeaderView() {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.setStylePrimaryName(Resources.INSTANCE.style().headerView());
		
		topBarContainer.getParent().getElement().getStyle().clearOverflow();
		topBarContainer.setStylePrimaryName(Resources.INSTANCE.style().topBarContainer());
		getLogo().setStyleName(Resources.INSTANCE.style().logo());
		getLogo().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem(HistoryToken.HOME);
			}
		});
		
		loginContainer.setStylePrimaryName(Resources.INSTANCE.style().loginButtonContainer());
		//changeSchoolContainer.setStylePrimaryName(Resources.INSTANCE.style().loginButtonContainer());
		
		// Setup buttons
		//changeSchoolContainer.setWidget(new SchoolChangeButton());
		
		logout.setStylePrimaryName(Resources.INSTANCE.style().logoutButton());
		logout.addStyleName(Resources.INSTANCE.style().bpButton());
		logout.addClickHandler(new LogoutHandler());
		login.getLoginButton().addClickHandler(new LoginHandler());
		
		updateLoginContainer();
	}
	
	public void setSelectedMenuItem(Page pageType) {
		nav.setSelected(pageType);
	}
	
	public void updateLoginContainer() {
		GWT.log("HeaderView - updating login container. Cookies are: " + Cookies.getCookieNames(), null);
		if (Cookies.getCookie("uid") != null) {
			loginContainer.setWidget(logout);
		} else {
			loginContainer.setWidget(login);
		}
	}
	
	public static final HeaderView getHeaderView(){
		return INSTANCE;
	}

	public void setLogo(ImageResource resource) {
		this.logo.setResource(resource);
		this.logo.setWidth(resource.getWidth()+"px");
	}

	public Image getLogo() {
		return logo;
	}

}
