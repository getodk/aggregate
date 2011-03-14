package org.opendatakit.aggregate.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ManageTabUI extends TabPanel {
	
	// Management Navigation
	private static final String FORMS = "forms";
	private static final String EXPORT = "export";
	private static final String PERMISSIONS = "permissions";
	private static final String UTILITIES = "utilities";
	private static final String[] MANAGEMENT_MENU = {FORMS, EXPORT, PERMISSIONS, UTILITIES};
	static final String MANAGEMENT = "management";
	private FlexTable uploadTable = new FlexTable();
	private UrlHash hash;
	private FlexTable listOfForms;
	
	public ManageTabUI(FlexTable listOfForms) {
		super();
		this.hash = UrlHash.getHash();
		this.listOfForms = listOfForms;
		this.add(setupFormManagementPanel(), "Forms");
		this.add(setupExportsPanel(), "Export");
		this.add(setupPermissionsPanel(), "Permissions");
		this.add(setupUtilitiesPanel(), "Utilities");
		
		int selected = 0;
		String subMenu = hash.get(UrlHash.SUB_MENU);
		for (int i = 0; i < MANAGEMENT_MENU.length; i++)
			if (subMenu.equals(MANAGEMENT_MENU[i]))
				selected = i;
		this.selectTab(selected);
		
		for (int i = 0; i < MANAGEMENT_MENU.length; i++)
			this.getTabBar().getTab(i)
				.addClickHandler(getSubMenuClickHandler(
						MANAGEMENT, MANAGEMENT_MENU[i]));
	}
	
	public VerticalPanel setupFormManagementPanel() {
		Button uploadFormButton = new Button();
	    uploadFormButton.setHTML("<img src=\"images/blue_up_arrow.png\" /> Upload Form");
	    uploadTable.setWidget(0, 0, uploadFormButton);
		    
	    listOfForms.setText(0, 0, "Title");
	    listOfForms.setText(0, 1, "Form Id");
	    listOfForms.setText(0, 2, "User");
	    listOfForms.setText(0, 3, "Enabled");
	    listOfForms.setText(0, 4, "Publish");
	    listOfForms.setText(0, 5, "Export");
	    listOfForms.setText(0, 6, "Delete");
	    listOfForms.getRowFormatter().addStyleName(0, "titleBar");
	    listOfForms.addStyleName("dataTable");
	    listOfForms.getElement().setId("form_management_table");

	    VerticalPanel formManagementPanel = new VerticalPanel();
	    formManagementPanel.add(uploadTable);
	    formManagementPanel.add(listOfForms);
	    return formManagementPanel;
	}
	
	public FlexTable setupExportsPanel() {
		return new FlexTable();
	}
	
	public HTML setupPermissionsPanel() {
		return new HTML("Content Forthcoming");
	}
	
	public HTML setupUtilitiesPanel() {
		return new HTML("Content Forthcoming");
	}
	
	ClickHandler getSubMenuClickHandler(
			final String menu, final String subMenu) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hash.clear();
				hash.set(UrlHash.MAIN_MENU, menu);
				hash.set(UrlHash.SUB_MENU, subMenu);
				hash.put();
			}
		};
	}
}