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
package org.opendatakit.aggregate.query.submission;

import java.util.List;

import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

public abstract class QueryBase {

  protected Query query;
  protected final Datastore ds;
  protected final User user;
  protected final FormDefinition formDefinition;
  
  private boolean moreRecords;
  private int fetchLimit;
  
  private int numOfRecords;
  
  protected QueryBase(FormDefinition formDefinition, int maxFetchLimit, Datastore datastore, User user) throws ODKFormNotFoundException {
    ds = datastore;
    this.user = user;
    fetchLimit = maxFetchLimit;
    numOfRecords = 0;
    this.formDefinition = formDefinition;
  }
  
  public abstract List<Submission> getResultSubmissions() throws ODKIncompleteSubmissionData, ODKDatastoreException;

  public boolean moreRecordsAvailable() {
    return moreRecords;
  }
  
  public final FormDefinition getFormDefinition(){
    return formDefinition;
  }
  
  /**
   * Generates a result table that contains all the submission data 
   * of the form specified by the ODK ID
   * 
   * @return
   *    a result table containing submission data
   *
   * @throws ODKIncompleteSubmissionData 
   */
  protected List<? extends CommonFieldsBase> getSubmissionEntities() {

    // retrieve submissions
    List<? extends CommonFieldsBase> submissionEntities = null;
    try {
      submissionEntities = query.executeQuery(fetchLimit + 1);
      numOfRecords = submissionEntities.size();
      if(submissionEntities.size() > fetchLimit) {
        moreRecords = true;
        submissionEntities.remove(fetchLimit);
      }    
    } catch (ODKDatastoreException e) {
      // TODO: decide what to do
      e.printStackTrace();
    }

    return submissionEntities;
  }
  

  public int getNumRecords() {
    return numOfRecords;
  }
  
}
