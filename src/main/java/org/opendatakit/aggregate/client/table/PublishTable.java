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

import org.opendatakit.aggregate.client.permissions.ExternServSummary;
import org.opendatakit.aggregate.client.widgets.PurgeButton;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

/**
 * List all the external services to which forms are published.
 */
public class PublishTable extends FlexTable {

  private static final int HEADER_ROW = 0;
  private static final int STARTING_ROW = HEADER_ROW + 1;
  
  private static final String K_MAILTO = "mailto:";
  private static int PURGE_DATA = 0;
  private static int CREATED_BY = 1;
  private static int STATUS = 2;
  private static int TIME_PUBLISH_START = 3;
  private static int ACTION = 4;
  private static int TYPE = 5;
  private static int NAME = 6;

  public PublishTable() {
    super();
    this.setText(HEADER_ROW, PURGE_DATA, " ");
    this.setText(HEADER_ROW, CREATED_BY, "Created By");
    this.setText(HEADER_ROW, STATUS, "Status");
    this.setText(HEADER_ROW, TIME_PUBLISH_START, "Start Date");
    this.setText(HEADER_ROW, ACTION, "Action");
    this.setText(HEADER_ROW, TYPE, "Type");
    this.setText(HEADER_ROW, NAME, "Name");
    this.addStyleName("exportTable");
    this.getRowFormatter().addStyleName(HEADER_ROW, "titleBar");
  }

  public void updatePublishPanel(String formId, ExternServSummary[] eSS) {
    if (eSS == null)
      return;
    while (this.getRowCount() > STARTING_ROW)
      this.removeRow(STARTING_ROW);
    for (int i = 0; i < eSS.length; i++) {
      ExternServSummary e = eSS[i];
      PurgeButton purgeButton = new PurgeButton(formId, e);
      this.setWidget(i + STARTING_ROW, PURGE_DATA, purgeButton);
      String user = e.getUser();
      String displayName;
      if (user.startsWith(K_MAILTO)) {
        displayName = user.substring(K_MAILTO.length());
      } else if (user.startsWith("uid:")) {
        displayName = user.substring("uid:".length(), user.indexOf("|"));
      } else {
        displayName = user;
      }
      this.setText(i + STARTING_ROW, CREATED_BY, displayName);
      this.setText(i + STARTING_ROW, STATUS, e.getStatus().toString());
      this.setText(i + STARTING_ROW, TIME_PUBLISH_START, e.getTimeEstablished().toString());
      this.setText(i + STARTING_ROW, ACTION, e.getPublicationOption().getDescriptionOfOption());
      this.setText(i + STARTING_ROW, TYPE, e.getExternalServiceTypeName());
      this.setWidget(i + STARTING_ROW, NAME, new HTML(e.getName()));
    }
  }

}
