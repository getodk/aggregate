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

import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.client.table.SubmissionTable;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;

public class SubmissionPanel extends FlowPanel {

  private SubmissionTable submissionTable;

  public SubmissionPanel() {
    super();
    submissionTable = new SubmissionTable();
    add(submissionTable);
    getElement().setId("submission_container");
  }

  public void update(FilterGroup filterGroup) {
    // Set up the callback object.
    AsyncCallback<SubmissionUISummary> callback = new AsyncCallback<SubmissionUISummary>() {
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      public void onSuccess(SubmissionUISummary summary) {
        AggregateUI.getUI().clearError();
        submissionTable.update(summary);
      }
    };

    if(filterGroup.getFormId() != null) {
    	SecureGWT.getSubmissionService().getSubmissions(filterGroup, callback);
    }
  }
  
  public SubmissionTable getSubmissionTable() {
    return submissionTable;
  }
}
