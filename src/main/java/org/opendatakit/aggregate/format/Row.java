/*
 * Copyright (C) 2010 University of Washington
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
package org.opendatakit.aggregate.format;

import java.util.ArrayList;
import org.opendatakit.aggregate.submission.SubmissionKey;

/**
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class Row {

  private ArrayList<String> formattedValues;

  private SubmissionKey submissionKey;

  public Row(SubmissionKey key) {
    this.formattedValues = new ArrayList<String>();
    this.submissionKey = key;
  }

  @SuppressWarnings("unchecked")
  public static Row cloneRowValues(Row row) {
    Row newRow = new Row(row.submissionKey);
    newRow.formattedValues = (ArrayList<String>) row.formattedValues.clone();
    return newRow;
  }

  public void addFormattedValue(String formattedValue) {
    formattedValues.add(formattedValue);
  }

  public ArrayList<String> getFormattedValues() {
    return formattedValues;
  }

  public SubmissionKey getSubmissionKey() {
    return submissionKey;
  }

  public void addDataFromRow(Row rowToAdd) {
    formattedValues.addAll(rowToAdd.formattedValues);
  }

  public int size() {
    return formattedValues.size();
  }
}
