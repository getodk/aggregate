package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;

public class ExecuteDeleteTablesAdmin extends AbstractButtonBase implements ClickHandler {
 
  private static final String TOOLTIP_TEXT = UIConsts.EMPTY_STRING;
  
  private String aggregateUid;
  private PopupPanel popup;
  
  public ExecuteDeleteTablesAdmin(String aggregateUid, PopupPanel popup) {
    super("<img src=\"images/green_right_arrow.png\" /> Delete User", TOOLTIP_TEXT);
    this.aggregateUid = aggregateUid;
    this.popup = popup;
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    
    // OK -- we are to proceed.
    // Set up the callback object.
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
      @Override
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      @Override
      public void onSuccess(Boolean result) {
        AggregateUI.getUI().clearError();
        if ( result ) {
           Window.alert("Successfully deleted the user");
        } else {
         Window.alert("Error: unable to delete the user!");
        }
        AggregateUI.getUI().getTimer().refreshNow();
      }
    };
    // Make the call to the form service.
    SecureGWT.getOdkTablesAdminService().deleteAdmin(aggregateUid, callback);
    popup.hide();
  }
}
