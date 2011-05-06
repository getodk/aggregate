package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.form.ExportSummary;
import org.opendatakit.aggregate.client.form.ExternServSummary;
import org.opendatakit.aggregate.client.form.FormService;
import org.opendatakit.aggregate.client.preferences.Preferences;
import org.opendatakit.aggregate.constants.common.FormOrFilter;
import org.opendatakit.aggregate.constants.common.PageUpdates;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
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
	private static final String PERMISSIONS = "permissions";
	private static final String UTILITIES = "utilities";
	private static final String[] MANAGEMENT_MENU = {FORMS, EXPORT, PUBLISH, PERMISSIONS, UTILITIES};
	static final String MANAGEMENT = "management";
   UrlHash hash;
   AggregateUI parent;
   
   // Forms tab
	private FlexTable uploadTable = new FlexTable();
	private FlexTable listOfForms;
	
	// Publish tab
	private FlexTable publishTable = new FlexTable();
	
	// Export tab
	private FlexTable exportTable = new FlexTable();
	
	private TextBox mapsApiKey = new TextBox();
	
	public ManageTabUI(FlexTable listOfForms, AggregateUI parent) {
		super();
		this.hash = UrlHash.getHash();
		this.listOfForms = listOfForms;
		this.parent = parent;

		this.add(setupFormManagementPanel(), "Forms");
		this.add(exportTable, "Export");
		this.add(publishTable, "Publish");
		this.add(setupPermissionsPanel(), "Permissions");
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
		    hash.goTo("ui/upload");
		  }
		});
	    uploadTable.setWidget(0, 0, uploadFormButton);
	    Button uploadSubmissionsButton = new Button("<img src=\"images/blue_up_arrow.png\" /> Upload Data");
	    uploadSubmissionsButton.addClickHandler(new ClickHandler() {
	      @Override
	      public void onClick(ClickEvent event) {
	        hash.goTo("ui/submission");
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
          Preferences.setGoogleMapsApiKey(mapsApiKey.getText());        
        }
	    
	  });
	  
	  VerticalPanel  preferencesPanel = new VerticalPanel();
     preferencesPanel.add(labelMapsKey);
	  preferencesPanel.add(mapsApiKey);
	  preferencesPanel.add(updateMapsApiKeyButton);
	  return preferencesPanel ;
	}
	
	public FlexTable setupExportsPanel() {
		return new FlexTable();
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
	    getExternalServicesList(hash.get(UrlHash.FORM));
	  }
	}
	
	private void updatePublishPanel(ExternServSummary[] eSS) {
	  if (eSS == null)
       return;
     while (publishTable.getRowCount() > 1)
	    publishTable.removeRow(1);
	  for (int i = 0; i < eSS.length; i++) {
	    ExternServSummary e = eSS[i];
	    publishTable.setWidget(i + 1, 0, new Anchor(e.getUser()));
	    publishTable.setText(i + 1, 1, e.getStatus().toString());
	    publishTable.setText(i + 1, 2, e.getEstablished().toString());
	    publishTable.setText(i + 1, 3, e.getAction());
	    publishTable.setText(i + 1, 4, e.getType());
	    publishTable.setWidget(i + 1, 5, new HTML(e.getName()));
	  }
	}
	
	public void getExternalServicesList(String formId) {
	  if (parent.formSvc == null) {
	    parent.formSvc = GWT.create(FormService.class);
	  }
	  
	  AsyncCallback<ExternServSummary[] > callback = new AsyncCallback<ExternServSummary []>() {
      @Override
      public void onFailure(Throwable caught) {
        // TODO Auto-generated method stub
      }

      @Override
      public void onSuccess(ExternServSummary[] result) {
        updatePublishPanel(result);
      }
	  };
	  
	  parent.formSvc.getExternalServices(formId, callback);
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
     getExportList();
   }
   
   private void updateExportPanel(ExportSummary[] eS) {
     if (eS == null)
       return;
     while (exportTable.getRowCount() > 1)
       exportTable.removeRow(1);
     for (int i = 0; i < eS.length; i++) {
       ExportSummary e = eS[i];
       exportTable.setText(i + 1, 0, e.getFileType().toString());
       exportTable.setText(i + 1, 1, e.getStatus().toString());
       exportTable.setText(i + 1, 2, e.getTimeRequested().toString());
       exportTable.setText(i + 1, 3, e.getTimeCompleted().toString());
       exportTable.setText(i + 1, 4, e.getTimeLastAction().toString());
       exportTable.setWidget(i + 1, 5, new HTML(e.getResultFile()));
     }
   }
   
   public void getExportList() {
     if (parent.formSvc == null) {
       parent.formSvc = GWT.create(FormService.class);
     }
     
     AsyncCallback<ExportSummary[] > callback = new AsyncCallback<ExportSummary []>() {
      @Override
      public void onFailure(Throwable caught) {
        // TODO Auto-generated method stub
      }

      @Override
      public void onSuccess(ExportSummary[] result) {
        updateExportPanel(result);
      }
     };
     
     parent.formSvc.getExports(callback);
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
				parent.getTimer().restartTimer(parent);
				parent.update(FormOrFilter.FORM, PageUpdates.FORMTABLE);
				hash.clear();
				hash.set(UrlHash.MAIN_MENU, menu);
				hash.set(UrlHash.SUB_MENU, subMenu);
				hash.put();
			}
		};
	}
}