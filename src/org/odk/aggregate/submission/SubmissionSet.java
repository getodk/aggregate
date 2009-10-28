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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.PersistConsts;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.FormElement;
import org.odk.aggregate.submission.type.RepeatSubmissionType;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.gson.JsonObject;


/**
 * Groups a set of submission values together so they can be
 * stored in a databstore entity
 * 
 * @author wbrunette@gmail.com
 *
 */
public class SubmissionSet implements Comparable<SubmissionSet>{
  /**
   * GAE datastore entity
   */
  protected Entity dbEntity;

  /**
   * GAE datastore key that uniquely identifies the submission
   */
  protected Key key;

  /**
   * ODK identifier that identifies the form used to define the submission
   */
  protected String odkId;

  /**
   * Identifier for this submission set
   */
  protected String setName;

  /**
   * Identifier for the parent submission set if this is a repeat
   */
  protected Key parentSubmissionSetKey;
  
  /**
   * List of submission values that make up the data contained in this
   * submission set
   */
  protected List<SubmissionValue> submissionValues;
 
  protected long order;
  


  /**
   * Construct an empty submission for the ODK ID form
   * 
   * @param formOdkIdentifier the ODK id of the form
   */
  public SubmissionSet(String formOdkIdentifier, String name, Key parentKey) {
    this(formOdkIdentifier, name, parentKey, -1);
  }
  
  /**
   * Construct an empty submission for the ODK ID form
   * 
   * @param formOdkIdentifier the ODK id of the form
   */
  public SubmissionSet(String formOdkIdentifier, String name, Key parentKey, long orderNum) {
    odkId = formOdkIdentifier;
    setName = name;
    parentSubmissionSetKey = parentKey;
    dbEntity = new Entity(getKindId());
    order = orderNum;
    
    // generate key from the datastore
    key = DatastoreServiceFactory.getDatastoreService().put(getEntity());
  }

  /**
   * Construct a submission from an entity from the data store
   * 
   * @param submissionSetEntity submission entity that contains the data
   * @param form TODO
   * @throws ODKIncompleteSubmissionData
   */
 
  public SubmissionSet(Entity submissionSetEntity, Form form) throws
      ODKIncompleteSubmissionData {
    
    dbEntity = submissionSetEntity;
    key = submissionSetEntity.getKey();
    odkId = (String) dbEntity.getProperty(PersistConsts.ODKID_PROPERTY);
    setName = (String) dbEntity.getProperty(PersistConsts.SET_NAME_PROPERTY);
    parentSubmissionSetKey = (Key) dbEntity.getProperty(PersistConsts.PARENT_KEY_PROPERTY);
    order = (Long) dbEntity.getProperty(PersistConsts.ORDER_PROPERTY);
    
    FormElement element = form.getBeginningElement(setName);
    restoreSubmissionFields(form, element);
  }

  /**
   * Recursively use form definition to recreate the submission
   * 
   * @param form persistence manager used to retrieve form elements
   * @param element current element to recreate
   *
   * @throws ODKIncompleteSubmissionData
   */
  private void restoreSubmissionFields(Form form, FormElement element)
      throws ODKIncompleteSubmissionData {
    if (element == null) {
      return;
    }

    String name = element.getElementName();

    if (name == null) {
      return;
    }
    
    if(element.isRepeatable()) {
      SubmissionRepeat submissionRepeat = new RepeatSubmissionType(odkId, name);
      submissionRepeat.getValueFromEntity(dbEntity, form);
      addSubmissionValues(submissionRepeat);
    } else {
      SubmissionField<?> submissionField =
        element.getSubmissionFieldType().createSubmissionField(name);
      submissionField.getValueFromEntity(dbEntity, form);
      addSubmissionValues(submissionField);
    }
    
    if(element.getChildren() == null) {
      return;
    }
    // iterate through all children
    for (FormElement child : element.getChildren()) {
      restoreSubmissionFields(form, child);
    }
  }

  /**
   * Get a list of submission values that make up the data contained in the
   * submission
   * 
   * @return list of populated submission values
   */
  public List<SubmissionValue> getSubmissionValues() {
    return submissionValues;
  }

  /**
   * Add submission field to the submission
   * 
   * @param submissionValue submission field to be added
   */
  public void addSubmissionValues(SubmissionValue submissionValue) {
    if (submissionValues == null) {
      submissionValues = new ArrayList<SubmissionValue>();
    }
    submissionValues.add(submissionValue);
  }

  /**
   * Get a map of the submission fields with the field/element name as the key
   * 
   * @return map of submission fields
   */
  public Map<String, SubmissionField<?>> getSubmissionFieldsMap() {
    Map<String, SubmissionField<?>> fieldMap = new HashMap<String, SubmissionField<?>>();
    for (SubmissionValue value : submissionValues) {
      if(value instanceof SubmissionField<?>) {
        SubmissionField<?> field = (SubmissionField<?>) value;
        fieldMap.put(field.getPropertyName(), field);
      }
    }
    return fieldMap;
  }
  
  /**
   * Get a map of the submission values with the field/element name as the key
   * 
   * @return map of submission values
   */
  public Map<String, SubmissionValue> getSubmissionValuesMap() {
    Map<String, SubmissionValue> valueMap = new HashMap<String, SubmissionValue>();
    for (SubmissionValue value : submissionValues) {
      valueMap.put(value.getPropertyName(), value);
    }
    return valueMap;
  }
  

  /**
   * Get the GAE datastore key that uniquely identifies the submission
   * 
   * @return datastore key
   */
  public Key getKey() {
    return key;
  }

  /**
   * Set the GAE datastore key that uniquely identifies the submission
   * 
   * @param newKey datastore key
   */
  public void setKey(Key newKey) {
    key = newKey;
  }

  /**
   * Get the ODK identifier for the form used to define the submission
   * 
   * @return odk id
   */
  public String getOdkId() {
    return odkId;
  }


  public String getKindId() {
    return odkId + setName;
  }

  /**
   * Get the datastore entity used to store the submission
   * 
   * @return datastore entity
   */
  public Entity getEntity() {
    dbEntity.setProperty(PersistConsts.SET_NAME_PROPERTY, setName);
    dbEntity.setProperty(PersistConsts.ODKID_PROPERTY, odkId);
    dbEntity.setProperty(PersistConsts.PARENT_KEY_PROPERTY, parentSubmissionSetKey);
    dbEntity.setProperty(PersistConsts.ORDER_PROPERTY, Long.valueOf(order));
    
    if (submissionValues != null) {
      for (SubmissionValue value : submissionValues) {
        value.addValueToEntity(dbEntity);
      }
    }
    return dbEntity;
  }
  
  
  /**
   * Generate a JSON Object that contains the submission set data
   * 
   * @return
   *    populated JSON Object
   */
  public JsonObject generateJsonObject(List<String> propertyNames) {
    JsonObject data = new JsonObject();
    if (submissionValues != null) {
      for (SubmissionValue value : submissionValues) {
        value.addValueToJsonObject(data, propertyNames);
      }
    }  
    return data;
  }
  
  public long getOrder() {
    return order;
  }
  
  public void printSubmission(PrintWriter out) {

    if (submissionValues != null) {
      for (SubmissionValue value : submissionValues) {
        out.println(value.toString());
      }
    }
  }
  
  public int compareTo(SubmissionSet obj) {
    long result = order - obj.order;
    
    // TODO: check if correct... not sure
    if(order != 0) {
      return Long.signum(result);
    }
    return key.compareTo(obj.key);
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = BasicConsts.EMPTY_STRING;
    if (submissionValues != null) {
      for (SubmissionValue value : submissionValues) {
        str += value.toString() + BasicConsts.NEW_LINE;
      }
    }
    return str;
  }

}
