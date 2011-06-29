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
