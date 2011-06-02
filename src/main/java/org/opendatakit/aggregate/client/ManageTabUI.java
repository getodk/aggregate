/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.preferences.Preferences;
import org.opendatakit.aggregate.constants.common.FormOrFilter;
import org.opendatakit.aggregate.constants.common.PageUpdates;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ManageTabUI extends TabPanel {
	
	private static final String GOOGLE_MAPS_API_KEY_LABEL = "<h2>Google Maps API Key</h2> To obtain a key signup at <a href=\"http://code.google.com/apis/maps/signup.html\"> Google Maps </a>";
  // Management Navigation
	private static final String FORMS = "forms";
	private static final String EXPORT = "export";
    static final String PUBLISH = "publish";
	static final String PERMISSIONS = "permissions";
	private static final String UTILITIES = "utilities";
	private static final String[] MANAGEMENT_MENU = {FORMS, EXPORT, PUBLISH, PERMISSIONS, UTILITIES};
	static final String MANAGEMENT = "management";
   UrlHash hash;
   AggregateUI baseUI;
   
   // Forms tab
	private FlexTable uploadTable = new FlexTable();
	private FlexTable listOfForms;
	
   // Publish tab
   private FlexTable publishTable;
   
   // Export tab
   private FlexTable exportTable;
	
	// Permissions tab
	private PermissionsSheet permissionsSheet;
	
	private TextBox mapsApiKey = new TextBox();
	
	public ManageTabUI(FlexTable listOfForms, FlexTable publishTable, FlexTable exportTable, AggregateUI parent) {
		super();
		this.hash = UrlHash.getHash();
		this.baseUI = parent;

	   this.listOfForms = listOfForms;
	   
	   this.publishTable = publishTable;
	   setupPublishPanel();
	   this.exportTable = exportTable;
	   setupExportPanel();
		
		permissionsSheet = new PermissionsSheet(this);
		
		this.add(setupFormManagementPanel(), "Forms");
		this.add(exportTable, "Export");
		this.add(publishTable, "Publish");
		this.add(permissionsSheet, "Permissions");
		this.add(setupUtilitiesPanel(), "Utilities");
		this.add(setupPreferencesPanel(), "Preferences");
		
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
		Button uploadFormButton = new Button("<img src=\"images/yellow_plus.png\" /> New Form");
		uploadFormButton.addClickHandler(new ClickHandler() {
		  @Override
		  public void onClick(ClickEvent event) {
			baseUI.clearError();
		    hash.goTo("../ui/upload");
		  }
		});
	    uploadTable.setWidget(0, 0, uploadFormButton);
	    Button uploadSubmissionsButton = new Button("<img src=\"images/blue_up_arrow.png\" /> Upload Data");
	    uploadSubmissionsButton.addClickHandler(new ClickHandler() {
	      @Override
	      public void onClick(ClickEvent event) {
			baseUI.clearError();
	        hash.goTo("../ui/submission");
	      }
	    });
	    uploadTable.setWidget(0, 1, uploadSubmissionsButton);
	    
	    listOfForms.setText(0, 0, "Title");
	    listOfForms.setText(0, 1, "Form Id");
	    listOfForms.setText(0, 2, "User");
	    listOfForms.setText(0, 3, "Downloadable");
	    listOfForms.setText(0, 4, "Accept Submissions");
	    listOfForms.setText(0, 5, "Publish");
	    listOfForms.setText(0, 6, "Export");
	    listOfForms.setText(0, 7, "Delete");
	    listOfForms.getRowFormatter().addStyleName(0, "titleBar");
	    listOfForms.addStyleName("dataTable");
	    listOfForms.getElement().setId("form_management_table");

	    VerticalPanel formManagementPanel = new VerticalPanel();
	    formManagementPanel.add(uploadTable);
	    formManagementPanel.add(listOfForms);
	    return formManagementPanel;
	}
	
	public VerticalPanel  setupPreferencesPanel() {
	  HTML labelMapsKey = new HTML(GOOGLE_MAPS_API_KEY_LABEL); 
	  String key = Preferences.getGoogleMapsApiKey();
	  mapsApiKey.setText(key);
    
	  Button updateMapsApiKeyButton = new Button("Update");
	  updateMapsApiKeyButton.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
        	baseUI.clearError();
        	Preferences.setGoogleMapsApiKey(mapsApiKey.getText());        
        }
	    
	  });
	  
	  VerticalPanel  preferencesPanel = new VerticalPanel();
      preferencesPanel.add(labelMapsKey);
	  preferencesPanel.add(mapsApiKey);
	  preferencesPanel.add(updateMapsApiKeyButton);
	  return preferencesPanel ;
	}
	
	public void setupPublishPanel() {
	  publishTable.setText(0, 0, "Created By");
	  publishTable.setText(0, 1, "Status");
	  publishTable.setText(0, 2, "Start Date");
	  publishTable.setText(0, 3, "Action");
	  publishTable.setText(0, 4, "Type");
	  publishTable.setText(0, 5, "Name");
	  publishTable.addStyleName("dataTable");
	  publishTable.getRowFormatter().addStyleName(0, "titleBar");
	  
	  if (hash.get(UrlHash.FORM) != null && !hash.get(UrlHash.FORM).equals("")) {
	    baseUI.getExternalServicesList(hash.get(UrlHash.FORM));
	  }
	}
   
   public void setupExportPanel() {
     exportTable.setText(0, 0, "File Type");
     exportTable.setText(0, 1, "Status");
     exportTable.setText(0, 2, "Time Requested");
     exportTable.setText(0, 3, "Time Completed");
     exportTable.setText(0, 4, "Last Retry");
     exportTable.setText(0, 5, "Download File");
     exportTable.addStyleName("dataTable");
     exportTable.getRowFormatter().addStyleName(0, "titleBar");
     baseUI.getExportList();
   }
	
	public HTML setupUtilitiesPanel() {
		return new HTML("Content Forthcoming");
	}
	
	ClickHandler getSubMenuClickHandler(
			final String menu, final String subMenu) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				baseUI.clearError();
				baseUI.getTimer().restartTimer(baseUI);
				baseUI.update(FormOrFilter.FORM, PageUpdates.FORMTABLE);
				hash.clear();
				hash.set(UrlHash.MAIN_MENU, menu);
				hash.set(UrlHash.SUB_MENU, subMenu);
				hash.put();
			}
		};
	}

	public void setSubSelection(String subMenu, String subSubMenu) {
		hash.clear();
		hash.set(UrlHash.MAIN_MENU, MANAGEMENT);
		hash.set(UrlHash.SUB_MENU, subMenu);
		hash.set(UrlHash.FORM, subSubMenu);
		hash.put();
	}
}