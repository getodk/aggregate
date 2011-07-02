package org.opendatakit.aggregate.client;

import org.opendatakit.common.security.client.GrantedAuthorityInfo;
import org.opendatakit.common.security.common.GrantedAuthorityNames;

import com.google.gwt.user.client.ui.VerticalPanel;

public class PermissionsSubTab extends VerticalPanel implements SubTabInterface {

  private TemporaryAccessConfigurationSheet accessConfig;
  
  public PermissionsSubTab() {
  }
  
  @Override
  public void update() {

    final PermissionsSubTab temp = this;
    
    if ( AggregateUI.getUI().getUserInfo().getGrantedAuthorities().contains(
            new GrantedAuthorityInfo(GrantedAuthorityNames.ROLE_SITE_ACCESS_ADMIN.toString()))) {
          accessConfig = new TemporaryAccessConfigurationSheet(temp);
          accessConfig.setVisible(true);
          add(accessConfig);
    } else {
    	if ( accessConfig != null ) {
    		remove(accessConfig);
    		accessConfig = null;
    	}
    }
  }
}
