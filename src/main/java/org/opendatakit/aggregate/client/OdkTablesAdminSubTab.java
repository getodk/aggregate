package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.preferences.OdkTablesAdmin;
import org.opendatakit.aggregate.client.table.OdkAdminListTable;
import org.opendatakit.aggregate.client.widgets.AddTablesAdminButton;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;

public class OdkTablesAdminSubTab extends AggregateSubTabBase {

  private OdkAdminListTable listOfAdmins;
  private FlexTable nav;
  
  public OdkTablesAdminSubTab() {
    // vertical
    setStylePrimaryName(UIConsts.VERTICAL_FLOW_PANEL_STYLENAME);
    
    nav = new FlexTable();
    nav.setWidget(0, 0, new AddTablesAdminButton());
    
    add(nav);
    listOfAdmins = new OdkAdminListTable();
    add(listOfAdmins);
    
  }

  @Override
  public boolean canLeave() {
    return true;
  }

  @Override
  public void update() {

    // Set up the callback object.
    AsyncCallback<OdkTablesAdmin[]> callback = new AsyncCallback<OdkTablesAdmin[]>() {
      @Override
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      @Override
      public void onSuccess(OdkTablesAdmin[] admins) {
        AggregateUI.getUI().clearError();
        listOfAdmins.updateAdmin(admins);
      }
    };
    // Make the call to the form service.
    SecureGWT.getOdkTablesAdminService().listAdmin(callback);

  }

}
