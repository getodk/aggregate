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

import org.opendatakit.aggregate.client.table.OdkTablesViewAppLevelFileInfo;
import org.opendatakit.aggregate.client.widgets.ServletPopupButton;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.security.common.GrantedAuthorityName;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * This class builds the subtab that allows for viewing and managing the files
 * that are associated with an appName but not an individual tableId. <br>
 *
 * Copied from ODKTableManageTableFilesSubTab
 *
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesManageAppLevelFilesSubTab extends AggregateSubTabBase {

  // this is the panel with the add button
  private FlexTable selectTablePanel;

  private HorizontalPanel topPanel;

  // the string constants for adding a file
  private static final String ADD_FILE_TXT = "Add an application file";
  private static final String ADD_FILE_TOOLTIP_TXT = "Upload a file";
  private static final String ADD_FILE_BALLOON_TXT = "Upload a file that is not associated with a specific tableId";
  private static final String ADD_FILE_BUTTON_TXT = "<img src=\"images/yellow_plus.png\" />"
      + ADD_FILE_TXT;

  // this is a button for adding a file.
  private ServletPopupButton addFileButton;

  // the box that shows the data
  private OdkTablesViewAppLevelFileInfo tableFileData;

  /**
   * Sets up the View Table subtab.
   */
  public OdkTablesManageAppLevelFilesSubTab() {

    addFileButton = new ServletPopupButton(ADD_FILE_BUTTON_TXT, ADD_FILE_TXT,
        UIConsts.APP_LEVEL_FILE_UPLOAD_SERVLET_ADDR, this, ADD_FILE_TOOLTIP_TXT, ADD_FILE_BALLOON_TXT);

    setStylePrimaryName(UIConsts.VERTICAL_FLOW_PANEL_STYLENAME);

    tableFileData = new OdkTablesViewAppLevelFileInfo(this);

    selectTablePanel = new FlexTable();
    selectTablePanel.getElement().setId("app_level_panel");
    selectTablePanel.setHTML(0, 0, "<h2> Application Level Files </h2>");
    selectTablePanel.setWidget(1, 0, addFileButton);

    topPanel = new HorizontalPanel();
    topPanel.add(selectTablePanel);
    topPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_JUSTIFY);
    add(topPanel);
    add(tableFileData);
  }

  @Override
  public boolean canLeave() {
    // sure you can leave.
    return true;
  }

  @Override
  public void update() {

    if ( AggregateUI.getUI().getUserInfo().getGrantedAuthorities().contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES)) {
      tableFileData.updateData();
    }
  }

}
