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
package org.opendatakit.aggregate.format.element;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opendatakit.common.persistence.EntityKey;

public class Row {

  private List<String> formattedValues;
  
  private EntityKey entityKey;
  
  private Date submissionDate;
  
  public Row(EntityKey key) {
    this.formattedValues = new ArrayList<String>();
    this.entityKey = key;
  }
  
  public void addFormattedValue(String formattedValue) {
    formattedValues.add(formattedValue);
  }
  
  public List<String> getFormattedValues() {
    return formattedValues;
  }

  public EntityKey getEntityKey() {
    return entityKey;
  }

  public Date getSubmissionDate() {
    return submissionDate;
  }

  public void setSubmissionDate(Date submissionDate) {
    this.submissionDate = submissionDate;
  }
  
}
