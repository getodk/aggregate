package org.opendatakit.aggregate.client.submission;

import org.opendatakit.aggregate.client.filter.FilterGroup;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SubmissionServiceAsync {

  void getSubmissions(FilterGroup filter, AsyncCallback<SubmissionUISummary> callback);

}
