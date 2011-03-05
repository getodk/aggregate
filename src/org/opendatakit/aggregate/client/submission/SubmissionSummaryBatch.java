package org.opendatakit.aggregate.client.submission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SubmissionSummaryBatch implements Serializable {

  /**
   * Serialization Identifier
   */
  private static final long serialVersionUID = 4067244808385366754L;

  private List<SubmissionSummary> submissions = new ArrayList<SubmissionSummary>();
  
  private int numberOfFields = 0;
  
  public SubmissionSummaryBatch() {
    
  }
  
  public SubmissionSummaryBatch(int numberOfFields) {
    this.numberOfFields = numberOfFields;
  }
  
  public void addSubmission(SubmissionSummary submission) throws Exception {
    if(submission.getNumberOfFields() == numberOfFields) {
      submissions.add(submission);
    } else {
      throw new Exception("Incorrect number of fields contained in submission");
    }
  }
  
}
