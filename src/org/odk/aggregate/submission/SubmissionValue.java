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


import java.util.List;

import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.Form;

import com.google.appengine.api.datastore.Entity;
import com.google.gson.JsonObject;


/**
 * Interface for submission value that can be used to store
 * a submission value in the datastore 
 *
 * @author wbrunette@gmail.com
 */
public interface SubmissionValue {
  
  /**
   * Get Property Name
   *
   * @return
   *    property name
   */
  public String getPropertyName();
  
  /**
   * Get submission field value from database entity
   *
   * @param dbEntity entity to obtain value
   * @param form the form definition object
   * @throws ODKIncompleteSubmissionData
   */
  public void getValueFromEntity(Entity dbEntity, Form form) throws ODKIncompleteSubmissionData;

  /**
   * Add submission field value to database entity
   * 
   * @param dbEntity entity to add value to
   */
  public void addValueToEntity(Entity dbEntity);
  
  /**
   * Add submission field value to JsonObject
   * @param propertyNames TODO
   * @param dbEntity entity to add value to
   */  
  public void addValueToJsonObject(JsonObject jsonObject, List<String> propertyNames);
}
