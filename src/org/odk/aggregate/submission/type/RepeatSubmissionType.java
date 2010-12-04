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


package org.odk.aggregate.submission.type;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.submission.SubmissionRepeat;
import org.odk.aggregate.submission.SubmissionSet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.gson.JsonObject;

/**
 * Data Storage type for a repeat type. Store a list of datastore 
 * keys to submission sets in an entity
 *
 * @author wbrunette@gmail.com
 *
 */
public class RepeatSubmissionType implements SubmissionRepeat {

  /**
   * ODK identifier that uniquely identifies the form
   */
  private String odkId;

  /**
   * Identifier for repeat
   */
  private String submissionSetName;

  /**
   * List of submission sets that are a part of this submission set
   */
  private SortedSet<SubmissionSet> submissionSets;

  public RepeatSubmissionType(String odkId, String repeatIdentifier) {
    this.odkId = odkId;
    this.submissionSetName = repeatIdentifier;
    this.submissionSets = new TreeSet<SubmissionSet>();
  }

  public void addSubmissionSet(SubmissionSet submissionSet) {
    submissionSets.add(submissionSet);
  }

  public SortedSet<SubmissionSet> getSubmissionSets() {
    return submissionSets;
  }

  public int getNumberRepeats() {
    return submissionSets.size();
  }

  public String getKindId() {
    return odkId + submissionSetName;
  }
  
  public String getPropertyName() {
    return submissionSetName;
  }

  public void addValueToEntity(Entity dbEntity) {
    if (submissionSets != null) {
      List<Key> keys = new ArrayList<Key>();
      for (SubmissionSet submissionSet : submissionSets) {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Entity setEntity = submissionSet.getEntity();
        Key key = ds.put(setEntity);
        submissionSet.setKey(key);
        keys.add(key);
      }
      dbEntity.setProperty(getPropertyName(), keys);
    }
  }

  
  /**
   * Add submission field value to JsonObject
   * @param JSON Object to add value to
   */  
  public void addValueToJsonObject(JsonObject jsonObject, List<String> propertyNames) {
    if (submissionSets != null) {
      for (SubmissionSet submissionSet : submissionSets) {
        JsonObject repeatJsonObject = submissionSet.generateJsonObject(propertyNames);
        jsonObject.add(submissionSetName + submissionSet.getOrder(), repeatJsonObject);
      }
    }
  }
  
  public void getValueFromEntity(Entity dbEntity, Form form) throws ODKIncompleteSubmissionData {
    @SuppressWarnings("unchecked")
    List<Key> submissionSetKeys = (List<Key>) dbEntity.getProperty(getPropertyName());
    if (submissionSetKeys != null) {
      try {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        for(Key submissionSetKey : submissionSetKeys) {
          Entity entity = ds.get(submissionSetKey);
          SubmissionSet set = new SubmissionSet(entity, form);
          addSubmissionSet(set);
        }
      } catch (EntityNotFoundException e) {
        throw new ODKIncompleteSubmissionData(e);
      }
    }

  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof RepeatSubmissionType)) {
      return false;
    }
    
    RepeatSubmissionType other = (RepeatSubmissionType) obj;
    
    return (odkId == null ? (other.odkId == null) : (odkId.equals(other.odkId)))
      && (submissionSetName == null ? (other.submissionSetName == null) : (submissionSetName.equals(other.submissionSetName)))
      && (submissionSets == null ? (other.submissionSets == null) : (submissionSets.equals(other.submissionSets))); 
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 13;
 
    if(odkId != null) hashCode += odkId.hashCode(); 
    if(submissionSetName != null) hashCode += submissionSetName.hashCode();
    if(submissionSets != null) hashCode += submissionSets.hashCode();

    return hashCode; 
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = ((odkId != null) ? odkId.toString() : BasicConsts.EMPTY_STRING);
    str += BasicConsts.TO_STRING_DELIMITER + ((submissionSetName != null) ? submissionSetName : BasicConsts.EMPTY_STRING) + "\n";
    if(submissionSets != null) {
      for(SubmissionSet set : submissionSets) {
        str += BasicConsts.TO_STRING_DELIMITER + set.toString();
      }
    }
    return str;
  }
  
}
