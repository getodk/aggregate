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

package org.opendatakit.aggregate.client.submission;

import java.io.Serializable;
import java.util.ArrayList;

public class SubmissionUI implements Serializable {

  /**
   * Serialization Identifier
   */
  private static final long serialVersionUID = -5614397233493602380L;

  private ArrayList<String> values;

  private String submissionKeyAsString;

  public SubmissionUI() {

  }

  public SubmissionUI(ArrayList<String> values, String submissionKeyAsString) {
    this.values = values;
    this.submissionKeyAsString = submissionKeyAsString;
  }

  public int getNumberOfFields() {
    return values.size();
  }

  public ArrayList<String> getValues() {
    return values;
  }

  public String getSubmissionKeyAsString() {
    return submissionKeyAsString;
  }

}
