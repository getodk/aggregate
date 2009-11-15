/*
 * Copyright (C) 2009 University of Washington
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

package org.odk.aggregate.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.persistence.EntityManager;

import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.submission.Submission;
import org.odk.aggregate.submission.SubmissionSet;
import org.odk.aggregate.submission.SubmissionValue;
import org.odk.aggregate.submission.type.BlobSubmissionType;
import org.odk.aggregate.submission.type.RepeatSubmissionType;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;

/**
 * Takes a list of submission keys and performs recursive delete on all elements
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class DeleteSubmissions {

  private List<Key> submissionKeys;

  private Form form;

  public DeleteSubmissions(String odkId, List<Key> keys, EntityManager em)
      throws IOException, ODKFormNotFoundException {
    if (keys == null) {
      throw new IOException();
    }

    this.submissionKeys = keys;
    this.form = Form.retrieveForm(em, odkId);
  }

  public void deleteSubmissions() {

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    List<Key> deleteKeys = new ArrayList<Key>();

    for (Key submissionKey : submissionKeys) {
      try {
        Entity subEntity = ds.get(submissionKey);
        Submission sub = new Submission(subEntity, form);
        deleteHelper(sub, deleteKeys);
      } catch (EntityNotFoundException e) {
        // just move on
      } catch (ODKIncompleteSubmissionData e) {
        // just move on
      }
    }

    ds.delete(deleteKeys);
  }

  private void deleteHelper(SubmissionSet sub, List<Key> deleteKeys) {
    deleteKeys.add(sub.getKey());
    List<SubmissionValue> values = sub.getSubmissionValues();

    // iterate through values looking for the two types that contain keys
    for (SubmissionValue v : values) {
      if (v instanceof BlobSubmissionType) {
        BlobSubmissionType blob = (BlobSubmissionType) v;
        Key key = blob.getValue();
        if (key != null) {
          deleteKeys.add(key);
        }
      } else if (v instanceof RepeatSubmissionType) {
        RepeatSubmissionType repeatElement = (RepeatSubmissionType) v;
        SortedSet<SubmissionSet> repeats = repeatElement.getSubmissionSets();
        for (SubmissionSet repeatRecord : repeats) {
          deleteHelper(repeatRecord, deleteKeys);
        }
      }
    }
  }
}
