package org.opendatakit.aggregate.client.submission;

import org.opendatakit.aggregate.client.filter.FilterGroup;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("submissionservice")
public interface SubmissionService extends RemoteService {

  SubmissionSummaryBatch getSubmissions(FilterGroup filter);
  
}
