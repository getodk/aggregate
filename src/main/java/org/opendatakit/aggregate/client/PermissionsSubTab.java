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
    	if ( accessConfig != null ) {
    		accessConfig.setVisible(false);
    	}
    }
  }
}
