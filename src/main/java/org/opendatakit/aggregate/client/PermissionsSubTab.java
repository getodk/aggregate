package org.opendatakit.aggregate.client;

import org.opendatakit.common.security.common.GrantedAuthorityName;

import com.google.gwt.user.client.ui.VerticalPanel;

public class PermissionsSubTab extends VerticalPanel implements SubTabInterface {

  private TemporaryAccessConfigurationSheet accessConfig;
  
  public PermissionsSubTab() {
  }
  
  @Override
  public void update() {

    if ( AggregateUI.getUI().getUserInfo().getGrantedAuthorities().contains(GrantedAuthorityName.ROLE_SITE_ACCESS_ADMIN)) {
		if ( accessConfig == null ) {
			accessConfig = new TemporaryAccessConfigurationSheet(this);
			add(accessConfig);
		}
		accessConfig.setVisible(true);
    } else {
    	accessConfig.setVisible(false);
    }
  }
}
