package org.opendatakit.aggregate.server;

import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.submission.SubmissionSummaryBatch;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class SubmissionServiceImpl extends RemoteServiceServlet implements
org.opendatakit.aggregate.client.submission.SubmissionService {

  /**
   * Serialization Identifier
   */
  private static final long serialVersionUID = -7997978505247614945L;

  @Override
  public SubmissionSummaryBatch getSubmissions(FilterGroup filter) {
    // TODO Auto-generated method stub
    return null;
  }

}
