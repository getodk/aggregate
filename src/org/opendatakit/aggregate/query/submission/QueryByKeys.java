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

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class QueryByKeys {

  private List<SubmissionKey> submissionKeys;

  private Datastore ds;
  
  private User user;
  
  public QueryByKeys(List<SubmissionKey> keys, Datastore datastore, User user) throws ODKFormNotFoundException {
    ds = datastore;
    this.user = user;
    submissionKeys = keys;
  }

  public List<Submission> getResultSubmissions() throws ODKIncompleteSubmissionData, ODKDatastoreException, ODKFormNotFoundException {
    List<Submission> submissions = new ArrayList<Submission>();
    
    for (SubmissionKey submissionKey : submissionKeys) {
      try {
  		List<SubmissionKeyPart> parts = SubmissionKeyPart.splitSubmissionKey(submissionKey);
  		submissions.add( Submission.fetchSubmission(parts, ds, user) );
      } catch (ODKEntityNotFoundException e) {
        // TODO Decide how to handle the exceptions
        throw new ODKIncompleteSubmissionData(e);
      }
    }
    return submissions;

  }
}
