package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.permissions.PermissionsSheet;
import org.opendatakit.common.security.client.GrantedAuthorityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.security.SecurityServiceAsync;
import org.opendatakit.common.security.common.GrantedAuthorityNames;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PermissionsSubTab extends VerticalPanel implements SubTabInterface {

  private TemporaryAccessConfigurationSheet accessConfig;

  private PermissionsSheet permissionsSheet;
  
  public PermissionsSubTab() {
 
  }

  public void createAdvanced() {
    permissionsSheet = new PermissionsSheet(this);
    add(permissionsSheet);
    permissionsSheet.setVisible(true);
  }
  
  @Override
  public void update() {
    // TODO Auto-generated method stub

  }

  public void configure() {

    final PermissionsSubTab temp = this;
    
    SecurityServiceAsync service = SecureGWT.get().createSecurityService();
    service.getUserInfo(new AsyncCallback<UserSecurityInfo>() {

      @Override
      public void onFailure(Throwable caught) {
        Window.alert("Unable to contact server");
      }

      @Override
      public void onSuccess(UserSecurityInfo result) {
        if (result.getGrantedAuthorities().contains(
            new GrantedAuthorityInfo(GrantedAuthorityNames.ROLE_ACCESS_ADMIN.toString()))) {
          accessConfig = new TemporaryAccessConfigurationSheet(temp);
          accessConfig.setVisible(true);
          add(accessConfig);
        }
      }
    });
  }
}
