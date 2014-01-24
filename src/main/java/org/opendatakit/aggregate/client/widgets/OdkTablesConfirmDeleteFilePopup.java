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

import org.opendatakit.aggregate.client.AggregateSubTabBase;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.popups.AbstractPopupBase;
import org.opendatakit.aggregate.client.popups.OdkTablesConfirmDeleteRowPopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

/**
 * The popup that performs the deletion of a file from the datastore. NB: This
 * differs from the {@link OdkTablesConfirmDeleteRowPopup} in that it deletes a
 * row in an actual datastore table, as opposed to a row in a user-defined
 * OdkTables table.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesConfirmDeleteFilePopup extends AbstractPopupBase {

  private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Delete File";
  private static final String TOOLTIP_TXT = "Delete this file";
  private static final String HELP_BALLOON_TXT = "Delete this file.";

  // the table to delete from
  private final String tableId;
  // the row you're deleting
  private final String rowId;

  @SuppressWarnings("unused")
  private AggregateSubTabBase basePanel;

  public OdkTablesConfirmDeleteFilePopup(AggregateSubTabBase basePanel, String tableId, String rowId) {
    super();
    this.basePanel = basePanel;
    this.tableId = tableId;
    this.rowId = rowId;

    AggregateButton deleteButton = new AggregateButton(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    deleteButton.addClickHandler(new ExecuteDelete());

    FlexTable layout = new FlexTable();

    HTML message = new HTML("Are you sure you want to delete this file?");
    layout.setWidget(0, 0, message);
    layout.setWidget(0, 1, deleteButton);
    layout.setWidget(0, 2, new ClosePopupButton(this));

    setWidget(layout);
  }

  private class ExecuteDelete implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      // Set up the callback object.
      AsyncCallback<Void> callback = new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable caught) {
          AggregateUI.getUI().reportError(caught);
        }

        @Override
        public void onSuccess(Void v) {
          AggregateUI.getUI().clearError();

          AggregateUI.getUI().getTimer().refreshNow();
        }
      };
      // Make the call to the form service.
      SecureGWT.getServerDataService().deleteTableFile(tableId, rowId, callback);
      hide();
    }
  }
}
