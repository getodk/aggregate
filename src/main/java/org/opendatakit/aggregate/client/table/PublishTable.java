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

package org.opendatakit.aggregate.client.table;

import java.util.Date;

import org.opendatakit.aggregate.client.externalserv.ExternServSummary;
import org.opendatakit.aggregate.client.widgets.DeletePublishButton;
import org.opendatakit.aggregate.client.widgets.PurgeButton;
import org.opendatakit.aggregate.client.widgets.RestartButton;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.common.security.client.UserSecurityInfo;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

/**
 * List all the external services to which forms are published.
 */
public class PublishTable extends FlexTable {

  private static final int HEADER_ROW = 0;
  private static final int STARTING_ROW = HEADER_ROW + 1;

  private static int PURGE_DATA = 0;
  private static int CREATED_BY = 1;
  private static int STATUS = 2;
  private static int LAST_PUBLISHED = 3;
  private static int TIME_PUBLISH_START = 4;
  private static int ACTION = 5;
  private static int TYPE = 6;
  private static int OWNERSHIP = 7;
  private static int NAME = 8;
  private static int DELETE = 9;

  public PublishTable() {
    super();
    this.setText(HEADER_ROW, PURGE_DATA, " ");
    this.setText(HEADER_ROW, CREATED_BY, "Created By");
    this.setText(HEADER_ROW, STATUS, "Status");
    this.setText(HEADER_ROW, LAST_PUBLISHED, "Published Through");
    this.setText(HEADER_ROW, TIME_PUBLISH_START, "Start Date");
    this.setText(HEADER_ROW, ACTION, "Action");
    this.setText(HEADER_ROW, TYPE, "Type");
    this.setText(HEADER_ROW, OWNERSHIP, "Owner");
    this.setText(HEADER_ROW, NAME, "Name");
    this.setText(HEADER_ROW, DELETE, "Delete");
    this.addStyleName("exportTable");
    this.getRowFormatter().addStyleName(HEADER_ROW, "titleBar");
  }

  public void updatePublishPanel(String formId, ExternServSummary[] eSS) {
    while (this.getRowCount() > STARTING_ROW)
      this.removeRow(STARTING_ROW);
    if (eSS == null) {
    	// this happens if there is no publishing set up for this form
        return;
    }
    for (int i = 0; i < eSS.length; i++) {
      ExternServSummary e = eSS[i];
      PurgeButton purgeButton = new PurgeButton(formId, e);
      this.setWidget(i + STARTING_ROW, PURGE_DATA, purgeButton);
      String user = e.getUser();
      String displayName = UserSecurityInfo.getDisplayName(user);
      this.setText(i + STARTING_ROW, CREATED_BY, displayName);
      if ( e.getStatus() == OperationalStatus.BAD_CREDENTIALS)  {
        this.setWidget(i + STARTING_ROW, STATUS, new RestartButton(e, RestartButton.Circumstance.CREDENTIALS));
      } else if (e.getStatus() == OperationalStatus.ABANDONED ) {
        this.setWidget(i + STARTING_ROW, STATUS, new RestartButton(e, RestartButton.Circumstance.ABANDONED));
      } else if (e.getStatus() == OperationalStatus.PAUSED ) {
        this.setWidget(i + STARTING_ROW, STATUS, new RestartButton(e, RestartButton.Circumstance.PAUSED));
      } else {
        this.setText(i + STARTING_ROW, STATUS, e.getStatus().toString());
      }
      Date d = e.getTimeLastStreamingCursor();
      if ( d == null ) {
        d = e.getTimeLastUploadCursor();
      }
      this.setText(i + STARTING_ROW, LAST_PUBLISHED, (d == null) ? "" : d.toString());
      this.setText(i + STARTING_ROW, TIME_PUBLISH_START, e.getTimeEstablished().toString());
      this.setText(i + STARTING_ROW, ACTION, e.getPublicationOption().getDescriptionOfOption());
      this.setText(i + STARTING_ROW, TYPE, e.getExternalServiceType().getDisplayText());
      this.setWidget(i + STARTING_ROW, OWNERSHIP, new HTML(e.getOwnership()));
      this.setWidget(i + STARTING_ROW, NAME, new HTML(e.getName()));
      this.setWidget(i + STARTING_ROW, DELETE,  new DeletePublishButton(e));
    }
  }

}
