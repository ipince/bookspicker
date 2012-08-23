package com.bookspicker.client.view.widgets.buttons;

import com.bookspicker.client.HistoryToken;
import com.bookspicker.client.view.Resources;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FocusPanel;

public class NavigationButton extends FocusPanel {

	private final String historyToken;
	private String styleName;
	private String selectedStyleName;
	
	
	public NavigationButton(String _styleName, String _selectedStyleName, String _historyToken){
		super();
		
		styleName = _styleName;
		selectedStyleName = _selectedStyleName;
		this.setStylePrimaryName(styleName);
		
		this.addStyleName(Resources.INSTANCE.style().bpButton());
		
		historyToken = _historyToken;
		this.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (historyToken.equals(HistoryToken.SEARCH)) {
					History.newItem(Cookies.getCookie("searchState") == null ? 
							HistoryToken.SEARCH : Cookies.getCookie("searchState"));
				} else {
					History.newItem(historyToken);
				}
			}
		});
	}
	
	public void setSelected(boolean selected){
		if(selected){
//			this.removeStyleName(styleName);
			this.setStyleName(selectedStyleName);
			this.addStyleName(Resources.INSTANCE.style().bpButton());
		}else{
//			this.removeStyleName(selectedStyleName);
			this.setStyleName(styleName);
			this.addStyleName(Resources.INSTANCE.style().bpButton());
		}
	}

}
