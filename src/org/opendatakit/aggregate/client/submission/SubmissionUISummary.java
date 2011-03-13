package org.opendatakit.aggregate.client.submission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SubmissionUISummary implements Serializable {

  /**
   * Serialization Identifier
   */
  private static final long serialVersionUID = 4067244808385366754L;

  private List<SubmissionUI> submissions = new ArrayList<SubmissionUI>();
  
  private List<Column> headers;
  
  public SubmissionUISummary() {
    headers = new ArrayList<Column>();
  }
    
  public List<Column> getHeaders() {
    return headers;
  }

  public void addSubmission(SubmissionUI submission) throws Exception {   
    if(submission.getNumberOfFields() == headers.size()) {
      submissions.add(submission);
    } else {
      throw new Exception("Incorrect number of fields contained in submission");
    }
  }

  public void addSubmissionHeader(String displayHeader, String columnName) {
    headers.add(new Column(displayHeader, columnName));
  }
  
  public void addGeopointHeader(String displayHeader, String columnName, Long geopointColumnCode) {
    headers.add(new Column(displayHeader, columnName, geopointColumnCode));
  }
  
  public List<SubmissionUI> getSubmissions() {
    return submissions;
  }
  
}
