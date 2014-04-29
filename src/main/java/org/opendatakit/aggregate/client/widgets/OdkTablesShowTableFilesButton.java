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

package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.OdkTablesManageInstanceFilesSubTab;
import org.opendatakit.aggregate.client.table.OdkTablesTableList;
import org.opendatakit.aggregate.constants.common.SubTabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class OdkTablesShowTableFilesButton extends AggregateButton implements ClickHandler {

  private static final String BUTTON_TXT =
      "<img src=\"images/green_right_arrow.png\" /> Data Files Associated with Table";
  private static final String TOOLTIP_TXT = "Display Data Files for this Table";
  private static final String HELP_BALLOON_TXT = "View the data files that have " +
  		"been uploaded for this table.";

  // the table id of the button this table is tied to.
  private String tableId;
  // the parent table to which this table belongs
  @SuppressWarnings("unused")
  private OdkTablesTableList parentTable;

  public OdkTablesShowTableFilesButton(OdkTablesTableList parentTable,
      String tableId) {
    super(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    this.parentTable = parentTable;
    this.tableId = tableId;
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    OdkTablesManageInstanceFilesSubTab fileSubTab =
        (OdkTablesManageInstanceFilesSubTab) AggregateUI.getUI()
        .getSubTab(SubTabs.MANAGE_INSTANCE_FILES);
    fileSubTab.setCurrentTable(tableId);
    fileSubTab.update();
    AggregateUI.getUI().redirectToSubTab(SubTabs.MANAGE_INSTANCE_FILES);
  }
}
