package com.bookspicker.client.view;

import com.bookspicker.client.view.Resources;
import com.bookspicker.client.view.pageContents.AboutPageContent;
import com.bookspicker.client.view.pageContents.FaqPageContent;
import com.bookspicker.client.view.pageContents.HomePageContent;
import com.bookspicker.client.view.pageContents.PageNotFoundContent;
import com.bookspicker.client.view.widgets.BPPanel;
import com.bookspicker.shared.School;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dev.resource.Resource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class InformationPage extends Composite implements HasHeader {

	private static InformationPageUiBinder uiBinder = GWT
			.create(InformationPageUiBinder.class);

	interface InformationPageUiBinder extends UiBinder<Widget, InformationPage> {
	}
	
	@UiField
	static SimplePanel centralViewWrapper;
	@UiField 
	SimplePanel headerViewWrapper;
	
	private static final HeaderView headerView = HeaderView.getHeaderView();
	
	private Page pageType;
	
	BPPanel centralView;
	public InformationPage(Page pageType) {
		
		initWidget(uiBinder.createAndBindUi(this));

		headerViewWrapper.setWidget(headerView);
			
		this.pageType = pageType;
		headerView.setSelectedMenuItem(pageType);
		switch(pageType){
			case HOME:
				centralView = new BPPanel("BooksPicker", new HomePageContent());
				break;
			case FAQ:
				centralView = new BPPanel("Frequently Asked Questions", new FaqPageContent());
				break;
			case ABOUT:
				centralView = new BPPanel("About BooksPicker", new AboutPageContent());
				break;
			case PAGE_NOT_FOUND:
				centralView = new BPPanel("Page Not Found", new PageNotFoundContent());
				break;
		}

		centralViewWrapper.setWidget(centralView);
		centralViewWrapper.setStylePrimaryName(Resources.INSTANCE.style().informationPageContent());
	}
	
	@Override
	public void updateLoginContainer() {
		headerView.updateLoginContainer();
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		headerViewWrapper.setWidget(headerView);
		headerView.setSelectedMenuItem(pageType);
	}
	
}
