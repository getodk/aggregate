package org.opendatakit.aggregate.client;

import org.opendatakit.common.security.common.GrantedAuthorityName;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PermissionsSubTab extends VerticalPanel implements SubTabInterface {

  private TemporaryAccessConfigurationSheet accessConfig;
  
  public PermissionsSubTab() {
  }

  @Override
  public boolean canLeave() {
	  if ( accessConfig != null ) {
		  if ( accessConfig.isUiOutOfSyncWithServer() ) {
			boolean outcome = Window.confirm("Unsaved changes exist.\n"
					+ "Changes will be lost if you move off of the Permissions tab.\n"
					+ "\nDiscard unsaved changes?");
			return outcome;
		  }
	  }
	  return true;
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
