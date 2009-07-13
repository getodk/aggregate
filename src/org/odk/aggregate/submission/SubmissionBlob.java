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

import org.odk.aggregate.constants.PersistConsts;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;

/**
 * Defines a submission blob that can be converted into a data store entity. 
 *
 * @author wbrunette@gmail.com
 *
 */
public class SubmissionBlob {
  
  private Entity dbEntity;
  
  private Key key;
  
  private Blob blob;
  
  private Key parentKey;
  
  /**
   * Construct an blob entity and store it in the data store
   *    
   */
  public SubmissionBlob(Blob blob, Key parent) {
    this.blob = blob;
    this.parentKey = parent;
    
    // construct entity
    dbEntity = new Entity(PersistConsts.BLOB_STORE_KIND);
    dbEntity.setProperty(PersistConsts.BLOB_PROPERTY, blob);
    dbEntity.setProperty(PersistConsts.PARENT_KEY_PROPERTY, parent);    
    
    // put entity in the datastore
    key = DatastoreServiceFactory.getDatastoreService().put(dbEntity);
  }

  public SubmissionBlob(Key blobKey) throws EntityNotFoundException {
    dbEntity = DatastoreServiceFactory.getDatastoreService().get(blobKey);
    key = blobKey;
    blob = (Blob) dbEntity.getProperty(PersistConsts.BLOB_PROPERTY);
    parentKey = (Key) dbEntity.getProperty(PersistConsts.PARENT_KEY_PROPERTY);
  }

  public Key getKey() {
    return key;
  }

  public Blob getBlob() {
    return blob;
  }

  public Key getParentKey() {
    return parentKey;
  }


}
