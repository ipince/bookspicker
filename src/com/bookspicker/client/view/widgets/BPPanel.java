package com.bookspicker.client.view.widgets;

import com.bookspicker.client.view.Resources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class BPPanel extends Composite {
	interface MyUiBinder extends UiBinder<Widget, BPPanel> {}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	@UiField
	Label title;
	@UiField
	FlowPanel contentContainer;
//	@UiField
//	TableElement table;

	public BPPanel(String panelTitle) {
		// sets title label and contentContainer
		initWidget(uiBinder.createAndBindUi(this));
		title.setText(panelTitle);
		contentContainer.setStyleName(Resources.INSTANCE.style().bpPanelContent());
	}
	
	public BPPanel(String panelTitle, Widget content) {
		// sets title label and contentContainer
		initWidget(uiBinder.createAndBindUi(this));
		title.setText(panelTitle);
		contentContainer.setStyleName(Resources.INSTANCE.style().bpPanelContent());
		contentContainer.add(content);		
	}
	
	public BPPanel() {
		initWidget(uiBinder.createAndBindUi(this));
		title.getElement().getParentElement().removeFromParent();
	}

	public FlowPanel getContentPanel(){
		return contentContainer;
	}
	
//	protected void addTableStyleName(String name){
//		table.addClassName(name);
//	}
	

}
