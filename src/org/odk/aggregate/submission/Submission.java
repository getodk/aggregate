/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.submission;

import java.util.Date;
import java.util.List;

import org.odk.aggregate.constants.PersistConsts;
import org.odk.aggregate.constants.TableConsts;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.Form;

import com.google.appengine.api.datastore.Entity;
import com.google.gson.JsonObject;


/**
 * Defines a form submission that can be converted into a datastore entity. 
 *
 * @author wbrunette@gmail.com
 *
 */
public class Submission extends SubmissionSet {
  
  /**
   * Time submission was created/received
   */
  private Date submittedTime;

  /**
   * Construct an empty submission for the ODK ID form
   * 
   * @param form
   *    the form to base the submission on
   */
  public Submission(Form form) {
    super(form.getOdkId(), form.getElementTreeRoot().getElementName(), null);
    submittedTime = new Date();
  }

  /**
   * Construct a submission from an entity from the data store
   * 
   * @param submission
   *    submission entity that contains the data
   * @throws ODKIncompleteSubmissionData 
   */
  public Submission(Entity submission, Form form) throws ODKIncompleteSubmissionData {
    super(submission, form);
    submittedTime = (Date) dbEntity.getProperty(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG);
  }
  
  /**
   * Get the time that the submission was created/received
   * 
   * @return
   *    date of submission
   */
  public Date getSubmittedTime() {
    return submittedTime;
  }

  /**
   * Get the datastore entity used to store the submission
   * 
   * @return
   *    datastore entity
   */
  @Override
  public Entity getEntity() {    
    dbEntity.setProperty(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG, submittedTime);
    
    // populate the rest of the entity
    return super.getEntity();
  }

  /**
   * Generate a JSON Object that contains the submission data
   * 
   * @return
   *    populated JSON Object
   */
  @Override
  public JsonObject generateJsonObject(List<String> propertyNames) {
    JsonObject data = super.generateJsonObject(propertyNames);
    // TODO: this is a deviation... matching to table instead of property... consider revising
    data.addProperty(TableConsts.SUBMISSION_DATE_HEADER, submittedTime.toString());
    return data;
  }
  
  @Override
  public String getKindId() {
    return odkId;
  }
  
}
