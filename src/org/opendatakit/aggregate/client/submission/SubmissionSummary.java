package org.opendatakit.aggregate.client.submission;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class SubmissionSummary implements Serializable {

  /**
   * Serialization Identifier
   */
  private static final long serialVersionUID = -5614397233493602380L;

  private List<String> values = new ArrayList<String>();
  
  public int getNumberOfFields() {
    return values.size();
  }
  
}
