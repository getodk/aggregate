package org.opendatakit.aggregate.client.submission;

import java.io.Serializable;
import java.util.List;


public class SubmissionUI implements Serializable {

  /**
   * Serialization Identifier
   */
  private static final long serialVersionUID = -5614397233493602380L;

  private List<String> values;
  
  public SubmissionUI() {
	  
  }
  
  public SubmissionUI(List<String> values) {
    this.values = values;
  }
  
  public int getNumberOfFields() {
    return values.size();
  }

  public List<String> getValues() {
    return values;
  }
  
  
}
