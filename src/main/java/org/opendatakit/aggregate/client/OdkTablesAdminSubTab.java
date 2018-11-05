/*
 * Copyright (C) 2013 University of Washington
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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import org.opendatakit.aggregate.client.preferences.OdkTablesAdmin;
import org.opendatakit.aggregate.client.table.OdkAdminListTable;
import org.opendatakit.aggregate.client.widgets.AddTablesAdminButton;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.security.common.GrantedAuthorityName;

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

    if (AggregateUI.getUI().getUserInfo().getGrantedAuthorities()
        .contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES)) {

      // Set up the callback object.
      AsyncCallback<OdkTablesAdmin[]> callback = new AsyncCallback<OdkTablesAdmin[]>() {
        @Override
        public void onFailure(Throwable caught) {
          if (caught instanceof AccessDeniedException) {
            // swallow it...
            AggregateUI.getUI().clearError();
            OdkTablesAdmin[] admins = new OdkTablesAdmin[0];
            listOfAdmins.updateAdmin(admins);
          } else {
            AggregateUI.getUI().reportError(caught);
          }
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

}
