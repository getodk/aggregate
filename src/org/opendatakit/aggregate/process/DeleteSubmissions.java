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

package org.opendatakit.aggregate.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.InstanceDataBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;

/**
 * Takes a list of submission keys and performs recursive delete on all elements
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class DeleteSubmissions {

  private List<EntityKey> submissionKeys;

  private FormDefinition form;

  private Datastore ds;
  
  private User user;
  
  public DeleteSubmissions(String formId, List<EntityKey> keys, Datastore datastore, User user)
      throws IOException, ODKFormNotFoundException {
    if (keys == null) {
      throw new IOException();
    }
    this.form =  FormDefinition.getFormDefinition(formId, ds, user);
    this.submissionKeys = keys;
    this.ds = datastore;
    this.user = user;
  }

  public void deleteSubmissions() throws ODKDatastoreException{
    List<EntityKey> deleteKeys = new ArrayList<EntityKey>();

    for (EntityKey submissionKey : submissionKeys) {
      try {
    	InstanceDataBase subEntity = (InstanceDataBase) ds.getEntity(submissionKey.getRelation(), submissionKey.getKey(), user);
        Submission sub = new Submission(subEntity, form, ds, user);
        deleteHelper(sub, deleteKeys);
      } catch (ODKEntityNotFoundException e) {
        // just move on
      }
    }
    ds.deleteEntities(deleteKeys, user);
  }

  private void deleteHelper(SubmissionSet sub, List<EntityKey> deleteKeys) throws ODKDatastoreException {
    deleteKeys.add(sub.getKey());
    List<SubmissionValue> values = sub.getSubmissionValues();

    // TODO: change submission so it can add keys to the list of keys to delete.
    
    // iterate through values looking for the two types that contain keys
    for (SubmissionValue v : values) {
    	v.recursivelyAddEntityKeys(deleteKeys);
    }
  }
}
