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
  
  private List<SubmissionHeader> headers;
  
  public SubmissionUISummary() {
    headers = new ArrayList<SubmissionHeader>();
  }
    
  public List<SubmissionHeader> getHeaders() {
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
    headers.add(new SubmissionHeader(displayHeader, columnName));
  }
  
  public class SubmissionHeader implements Serializable {
    /**
     * Serialization Identifier
     */
    private static final long serialVersionUID = -5276405259406410364L;
    
    private String displayHeader;
    private String columnName;
    
    public SubmissionHeader() {;}
    
    public SubmissionHeader(String displayHeader, String columnName) {
      this.displayHeader = displayHeader;
      this.columnName = columnName;
    }

    public String getDisplayHeader() {
      return displayHeader;
    }

    public String getColumnName() {
      return columnName;
    }
  }
}
