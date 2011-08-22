package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.client.widgets.ExecuteNewTablesUser;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

public class NewTablesAdminPopup extends PopupPanel {
 
  private TextBox name;
  private TextBox externalUid;
  
  public NewTablesAdminPopup() {
     super(false);

     name = new TextBox();
     externalUid = new TextBox();

     FlexTable layout = new FlexTable();
     layout.setWidget(0, 0, new ClosePopupButton(this));
     layout.setWidget(0, 1, new HTML("Create a New User"));
     layout.setWidget(1, 0, new HTML("Name:"));
     layout.setWidget(1, 1, name);
     layout.setWidget(2, 0, new HTML("User ID:"));
     layout.setWidget(2, 1, externalUid);
     layout.setWidget(3, 1, new ExecuteNewTablesUser(this));
    
     setWidget(layout);
  }
  
  public String getName() {
     return name.getText();
  }
  
  public String getExternalUid() {
     return externalUid.getText();
  }
  
}